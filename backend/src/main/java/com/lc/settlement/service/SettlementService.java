package com.lc.settlement.service;

import com.lc.settlement.dto.ClaudeDecisionDto;
import com.lc.settlement.dto.SettlementDto;
import com.lc.settlement.entity.*;
import com.lc.settlement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepo;
    private final AccountRepository accountRepo;
    private final FieldAgentRepository agentRepo;
    private final TranscriptRepository transcriptRepo;
    private final PolicyService policyService;

    // ----------------------------------------------------------------
    // Read APIs
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SettlementDto> listForAccount(Long accountId) {
        // Include settlements that cover this account as primary OR linked (bundle).
        return settlementRepo.findAllCoveringAccount(accountId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SettlementDto> listForCustomer(Long customerId) {
        return settlementRepo.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // Writes
    // ----------------------------------------------------------------

    @Transactional
    public SettlementDto createFromClaudeDecision(Long accountId, Long agentId, Long transcriptId, ClaudeDecisionDto decision) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        FieldAgent agent = agentId == null ? null : agentRepo.findById(agentId).orElse(null);
        Transcript transcript = transcriptId == null ? null : transcriptRepo.findById(transcriptId).orElse(null);

        String strategyCode = decision.getSelectedStrategyCode();
        boolean isBundle = "BUNDLE".equalsIgnoreCase(strategyCode);

        return saveSettlement(account, agent, transcript, decision, isBundle, "CLAUDE_AI");
    }

    @Transactional
    public SettlementDto createManual(Long accountId, Long agentId, Long transcriptId,
                                      Double offeredAmount, Double discountPercent,
                                      String plan, Integer installments, String rationale,
                                      String strategyCode) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        FieldAgent agent = agentId == null ? null : agentRepo.findById(agentId).orElse(null);
        Transcript transcript = transcriptId == null ? null : transcriptRepo.findById(transcriptId).orElse(null);

        ClaudeDecisionDto synthetic = ClaudeDecisionDto.builder()
                .recommendedOfferAmount(offeredAmount)
                .recommendedDiscountPercent(discountPercent)
                .recommendedPaymentPlan(plan)
                .recommendedInstallments(installments == null ? 1 : installments)
                .reasoning(rationale)
                .selectedStrategyCode(strategyCode)
                .build();

        boolean isBundle = "BUNDLE".equalsIgnoreCase(strategyCode);
        return saveSettlement(account, agent, transcript, synthetic, isBundle, "AGENT");
    }

    @Transactional
    public SettlementDto updateStatus(Long settlementId, String status, String customerResponse) {
        Settlement s = settlementRepo.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + settlementId));
        s.setStatus(status.toUpperCase());
        s.setCustomerResponse(customerResponse);
        s.setDecidedAt(LocalDateTime.now());
        return toDto(settlementRepo.save(s));
    }

    // ----------------------------------------------------------------
    // Core save logic (handles guardrail + duplicate + BUNDLE)
    // ----------------------------------------------------------------

    private SettlementDto saveSettlement(Account account, FieldAgent agent, Transcript transcript,
                                         ClaudeDecisionDto decision, boolean bundle, String source) {

        // Scope: which accounts are we covering?
        List<Account> covered;
        double totalOutstanding;

        if (bundle) {
            Long lenderId = policyService.lenderIdForAccount(account);
            Long customerId = account.getCustomer().getId();

            List<Account> customerAccountsAtLender = accountRepo
                    .findByCustomerIdAndLenderId(customerId, lenderId);

            if (customerAccountsAtLender == null || customerAccountsAtLender.isEmpty()) {
                throw new IllegalStateException(
                        "No accounts found for this customer at the same lender to bundle.");
            }

            // Filter out accounts whose ACCEPTED settlement already covers them.
            List<Account> eligible = new ArrayList<>();
            for (Account a : customerAccountsAtLender) {
                if (!hasAcceptedSettlement(a.getId())) {
                    eligible.add(a);
                }
            }

            if (eligible.isEmpty()) {
                throw new IllegalStateException(
                        "All of this customer's accounts at this lender already have accepted settlements — nothing left to bundle.");
            }

            covered = eligible;
            totalOutstanding = eligible.stream()
                    .mapToDouble(a -> a.getOutstandingAmount() == null ? 0.0 : a.getOutstandingAmount())
                    .sum();

            // Re-pin the primary account to an eligible one (in case the clicked account
            // is already settled). Prefer the passed-in one if it's eligible.
            if (!eligible.contains(account)) {
                account = eligible.get(0);
            }
        } else {
            // Single-account path. Block if an accepted settlement exists.
            if (hasAcceptedSettlement(account.getId())) {
                throw new IllegalStateException(
                        "Settlement already accepted for this account — cannot create a new proposal.");
            }
            covered = List.of(account);
            totalOutstanding = account.getOutstandingAmount() == null ? 0.0 : account.getOutstandingAmount();
        }

        // Apply the lender's policy guardrails against the aggregate outstanding.
        NegotiationPolicy policy = bundle
                ? policyService.resolveForPortfolio(covered)
                : policyService.resolveForAccount(account);

        double offered = decision.getRecommendedOfferAmount() == null ? 0.0 : decision.getRecommendedOfferAmount();
        double discountPct = decision.getRecommendedDiscountPercent() == null
                ? 0.0
                : decision.getRecommendedDiscountPercent();

        if (policy != null) {
            double floorPct = nz(policy.getOfferFloorPctOfOutstanding(), 60.0);
            double ceilPct  = nz(policy.getOfferCeilingPctOfOutstanding(), 100.0);
            double floorInr = totalOutstanding * floorPct / 100.0;
            double ceilInr  = totalOutstanding * ceilPct  / 100.0;
            if (floorInr > ceilInr) { double tmp = floorInr; floorInr = ceilInr; ceilInr = tmp; }

            // If Claude forgot a value or it drifted, derive / clamp.
            if (offered <= 0) {
                double midPct = (floorPct + ceilPct) / 2.0;
                offered = totalOutstanding * midPct / 100.0;
            }
            offered = Math.max(floorInr, Math.min(ceilInr, offered));

            double dFloor = nz(policy.getDiscountFloorPct(), 0.0);
            double dCeil  = nz(policy.getDiscountCeilingPct(), 40.0);
            discountPct = Math.max(dFloor, Math.min(dCeil, discountPct));
        }

        Settlement s = Settlement.builder()
                .account(account)
                .customer(account.getCustomer())
                .transcript(transcript)
                .proposedByAgent(agent)
                .outstandingAtOffer(totalOutstanding)
                .offeredAmount(offered)
                .discountPercent(discountPct)
                .paymentPlan(decision.getRecommendedPaymentPlan())
                .numberOfInstallments(decision.getRecommendedInstallments() == null
                        ? 1 : decision.getRecommendedInstallments())
                .proposedPaymentDate(LocalDate.now().plusDays(15))
                .status("PROPOSED")
                .source(source)
                .strategyCode(decision.getSelectedStrategyCode())
                .rationale(decision.getReasoning())
                .createdAt(LocalDateTime.now())
                .linkedAccounts(new ArrayList<>(covered))
                .build();

        return toDto(settlementRepo.save(s));
    }

    private boolean hasAcceptedSettlement(Long accountId) {
        List<Settlement> accepted = settlementRepo.findCoveringAccountWithStatus(accountId, "ACCEPTED");
        return accepted != null && !accepted.isEmpty();
    }

    // ----------------------------------------------------------------
    // Mapping
    // ----------------------------------------------------------------

    private SettlementDto toDto(Settlement s) {
        List<Account> covered = s.getLinkedAccounts();
        if (covered == null || covered.isEmpty()) covered = List.of(s.getAccount());

        // De-duplicate in case primary + linked overlap.
        Set<Long> seen = new HashSet<>();
        List<SettlementDto.LinkedAccount> linked = covered.stream()
                .filter(a -> a != null && seen.add(a.getId()))
                .map(a -> SettlementDto.LinkedAccount.builder()
                        .id(a.getId())
                        .accountNumber(a.getAccountNumber())
                        .productType(a.getProductType())
                        .outstandingAmount(a.getOutstandingAmount())
                        .dpdBucket(a.getDpdBucket())
                        .build())
                .collect(Collectors.toList());

        Lender lender = s.getAccount().getLender();

        return SettlementDto.builder()
                .id(s.getId())
                .customerId(s.getCustomer().getId())
                .customerName(s.getCustomer().getFullName())
                .accountId(s.getAccount().getId())
                .accountNumber(s.getAccount().getAccountNumber())
                .outstandingAtOffer(s.getOutstandingAtOffer())
                .offeredAmount(s.getOfferedAmount())
                .discountPercent(s.getDiscountPercent())
                .paymentPlan(s.getPaymentPlan())
                .numberOfInstallments(s.getNumberOfInstallments())
                .proposedPaymentDate(s.getProposedPaymentDate())
                .status(s.getStatus())
                .source(s.getSource())
                .strategyCode(s.getStrategyCode())
                .rationale(s.getRationale())
                .customerResponse(s.getCustomerResponse())
                .proposedByAgentName(s.getProposedByAgent() == null ? null : s.getProposedByAgent().getName())
                .createdAt(s.getCreatedAt())
                .decidedAt(s.getDecidedAt())
                .lenderId(lender == null ? null : lender.getId())
                .lenderName(lender == null ? null : lender.getName())
                .linkedAccounts(linked == null ? Collections.emptyList() : linked)
                .build();
    }

    private static double nz(Double v, double fallback) { return v == null ? fallback : v; }
}
