package com.lc.settlement.service;

import com.lc.settlement.dto.AccountDetailDto;
import com.lc.settlement.entity.Account;
import com.lc.settlement.entity.Settlement;
import com.lc.settlement.repository.AccountRepository;
import com.lc.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepo;
    private final SettlementRepository settlementRepo;

    public Account getEntity(Long id) {
        return accountRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    public AccountDetailDto getDetail(Long id) {
        Account a = getEntity(id);
        List<Settlement> accepted = settlementRepo.findCoveringAccountWithStatus(id, "ACCEPTED");
        boolean hasAccepted = accepted != null && !accepted.isEmpty();

        return AccountDetailDto.builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .productType(a.getProductType())
                .sanctionedAmount(a.getSanctionedAmount())
                .outstandingAmount(a.getOutstandingAmount())
                .principalOutstanding(a.getPrincipalOutstanding())
                .interestOutstanding(a.getInterestOutstanding())
                .penaltyAmount(a.getPenaltyAmount())
                .dpdBucket(a.getDpdBucket())
                .daysPastDue(a.getDaysPastDue())
                .lastPaymentDate(a.getLastPaymentDate())
                .lastPaymentAmount(a.getLastPaymentAmount())
                .sanctionDate(a.getSanctionDate())
                .interestRate(a.getInterestRate())
                .status(a.getStatus())
                .customerId(a.getCustomer().getId())
                .customerName(a.getCustomer().getFullName())
                .assignedAgentId(a.getAssignedAgent() == null ? null : a.getAssignedAgent().getId())
                .assignedAgentName(a.getAssignedAgent() == null ? null : a.getAssignedAgent().getName())
                .lenderId(a.getLender() == null ? null : a.getLender().getId())
                .lenderName(a.getLender() == null ? null : a.getLender().getName())
                .lenderCode(a.getLender() == null ? null : a.getLender().getCode())
                .hasAcceptedSettlement(hasAccepted)
                .build();
    }
}
