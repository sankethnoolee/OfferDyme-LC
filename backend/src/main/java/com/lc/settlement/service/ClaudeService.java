package com.lc.settlement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lc.settlement.dto.ClaudeDecisionDto;
import com.lc.settlement.entity.Account;
import com.lc.settlement.entity.ChatMessage;
import com.lc.settlement.entity.Customer;
import com.lc.settlement.entity.NegotiationPolicy;
import com.lc.settlement.entity.NegotiationStrategy;
import com.lc.settlement.entity.Transcript;
import com.lc.settlement.repository.AccountRepository;
import com.lc.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calls Anthropic's Messages API using the JDK's built-in HttpClient (JDK 11+).
 *
 * Critically, Claude is NEVER allowed to invent the negotiation floor/ceiling.
 * Those numbers come from the DB-configured {@link NegotiationPolicy} resolved
 * by {@link PolicyService}. Claude only picks a CURRENT point within that
 * fixed range, and picks exactly one of the DB-configured
 * {@link NegotiationStrategy} rows. The service then clamps the response so a
 * misbehaving LLM can never violate bank policy.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClaudeService {

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    @Value("${claude.api.model}")
    private String model;

    @Value("${claude.api.max-tokens}")
    private int maxTokens;

    private final PolicyService policyService;
    private final AccountRepository accountRepository;
    private final SettlementRepository settlementRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    /** Shared HttpClient — reuses connections and respects system proxy. */
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20))
            .proxy(ProxySelector.getDefault())
            .build();

    /** Analyze a single-account conversation. */
    public ClaudeDecisionDto analyze(Customer customer, Account account, Transcript transcript, List<ChatMessage> messages) {
        return doAnalyze(customer, List.of(account), transcript, messages, false);
    }

    /** Analyze a customer-level (portfolio) conversation that spans all of their accounts. */
    public ClaudeDecisionDto analyzePortfolio(Customer customer, List<Account> accounts, Transcript transcript, List<ChatMessage> messages) {
        return doAnalyze(customer, accounts, transcript, messages, true);
    }

    private ClaudeDecisionDto doAnalyze(Customer customer, List<Account> accounts, Transcript transcript,
                                        List<ChatMessage> messages, boolean portfolio) {

        // Portfolio scope must mirror what Save Settlement will do:
        // skip accounts that already have an ACCEPTED settlement, so the offer
        // numbers the agent sees in the right panel match the persisted bundle.
        List<Account> scopedAccounts = portfolio
                ? filterAcceptedOut(accounts)
                : accounts;

        // 1) Resolve the bank's policy for this scope.
        NegotiationPolicy policy = portfolio
                ? policyService.resolveForPortfolio(scopedAccounts)
                : (scopedAccounts.isEmpty() ? null : policyService.resolveForAccount(scopedAccounts.get(0)));

        // 2) Fetch the strategy menu for this lender.
        Long lenderId = lenderIdFrom(scopedAccounts);
        List<NegotiationStrategy> strategies = lenderId == null
                ? Collections.emptyList()
                : policyService.activeStrategies(lenderId);

        // Turn = # of customer utterances so far. Drives the concession ladder:
        // turn 1 anchors near ceiling, concessions only accrue as the customer
        // pushes back with real objections.
        int turnNumber = deriveTurnNumber(messages);

        String systemPrompt = buildSystemPrompt(portfolio, policy, strategies);
        String userPrompt   = buildUserPrompt(customer, scopedAccounts, transcript, messages, portfolio, policy, strategies, turnNumber);

        double totalOut = scopedAccounts.stream()
                .mapToDouble(a -> a == null || a.getOutstandingAmount() == null ? 0.0 : a.getOutstandingAmount())
                .sum();

        ClaudeDecisionDto decision;
        try {
            String responseText = callClaude(systemPrompt, userPrompt);
            decision = finalize(parseDecision(responseText, scopedAccounts, policy, strategies),
                    policy, strategies, totalOut, turnNumber);
        } catch (Exception ex) {
            log.error("Claude API call failed: {}", ex.getMessage(), ex);
            decision = finalize(fallbackDecision(scopedAccounts, messages, ex.getMessage(), policy, strategies),
                    policy, strategies, totalOut, turnNumber);
        }

        // Re-scope floor/ceiling/offer based on the chosen strategy:
        //   - BUNDLE     → aggregate across eligible accounts
        //   - non-BUNDLE → a single account (selected one, or highest-outstanding
        //                  eligible in portfolio mode), matching what Save
        //                  Settlement will actually persist.
        decision = applyStrategyContext(decision, customer, scopedAccounts, portfolio, strategies, turnNumber);

        return decision;
    }

    /**
     * Drop accounts that already have an ACCEPTED settlement — matches
     * SettlementService.saveSettlement's BUNDLE filter.
     */
    private List<Account> filterAcceptedOut(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) return accounts;
        List<Account> eligible = new ArrayList<>();
        for (Account a : accounts) {
            if (a == null || a.getId() == null) continue;
            List<com.lc.settlement.entity.Settlement> accepted =
                    settlementRepository.findCoveringAccountWithStatus(a.getId(), "ACCEPTED");
            if (accepted == null || accepted.isEmpty()) eligible.add(a);
        }
        // If filtering removed everything, fall back to the original list —
        // Claude will still say something, and the save path will block the
        // actual bundle with a clear error.
        return eligible.isEmpty() ? accounts : eligible;
    }

    /**
     * Final pass on the decision that aligns floor/ceiling/offer with the
     * CHOSEN strategy:
     *
     *   - BUNDLE     → aggregate eligible accounts at the same lender,
     *                  re-resolve policy, re-clamp offer against the aggregate,
     *                  publish bundle preview.
     *   - non-BUNDLE → narrow to a single account. In account scope this is the
     *                  selected account (initial finalize already did the work).
     *                  In portfolio scope we pick the highest-outstanding
     *                  eligible account, re-resolve policy for it, and
     *                  re-finalize against that one account's outstanding.
     */
    private ClaudeDecisionDto applyStrategyContext(ClaudeDecisionDto decision, Customer customer,
                                                   List<Account> currentScope, boolean portfolio,
                                                   List<NegotiationStrategy> strategies, int turnNumber) {
        if (decision == null) return decision;
        String code = decision.getSelectedStrategyCode();
        boolean isBundle = code != null && code.equalsIgnoreCase("BUNDLE");

        if (isBundle) {
            applyBundleScope(decision, customer, currentScope, portfolio, strategies, turnNumber);
        } else {
            applySingleAccountScope(decision, currentScope, portfolio, strategies, turnNumber);
        }
        return decision;
    }

    /** BUNDLE: aggregate across eligible accounts. */
    private void applyBundleScope(ClaudeDecisionDto decision, Customer customer,
                                  List<Account> currentScope, boolean portfolio,
                                  List<NegotiationStrategy> strategies, int turnNumber) {

        Long lenderId = lenderIdFrom(currentScope);
        if (lenderId == null || customer == null || customer.getId() == null) {
            decision.setBundle(Boolean.FALSE);
            return;
        }

        List<Account> bundleScope;
        if (portfolio) {
            // currentScope is already filtered to eligible accounts.
            bundleScope = currentScope;
        } else {
            // Account scope — fetch all of the customer's accounts at this lender,
            // then drop the ones with an ACCEPTED settlement.
            List<Account> atLender = accountRepository
                    .findByCustomerIdAndLenderId(customer.getId(), lenderId);
            bundleScope = filterAcceptedOut(atLender);
        }

        // Collapse duplicates defensively (the repo shouldn't return dupes but be safe).
        Set<Long> ids = new LinkedHashSet<>();
        List<Account> deduped = new ArrayList<>();
        for (Account a : bundleScope) {
            if (a != null && a.getId() != null && ids.add(a.getId())) deduped.add(a);
        }
        bundleScope = deduped;

        if (bundleScope.size() < 2) {
            // Not enough accounts to actually bundle — behave like single-account.
            decision.setBundle(Boolean.FALSE);
            decision.setBundleNote(bundleScope.isEmpty()
                    ? "No eligible accounts at this lender to bundle."
                    : "Only one eligible account — BUNDLE would behave like a single-account offer.");
            // Still scope to a single account so the displayed numbers are right.
            applySingleAccountScope(decision, bundleScope.isEmpty() ? currentScope : bundleScope,
                    portfolio, strategies, turnNumber);
            return;
        }

        // Re-resolve policy against the bundle (worst DPD, product=null).
        NegotiationPolicy bundlePolicy = policyService.resolveForPortfolio(bundleScope);
        double bundleTotal = bundleScope.stream()
                .mapToDouble(a -> a.getOutstandingAmount() == null ? 0.0 : a.getOutstandingAmount())
                .sum();

        // Clear single-account numbers so finalize() re-derives against the
        // aggregate — keeps offer% and offer INR consistent.
        decision.setOfferFloor(null);
        decision.setOfferCeiling(null);
        decision.setRecommendedOfferAmount(null);
        finalize(decision, bundlePolicy, strategies, bundleTotal, turnNumber);
        log.info("BUNDLE scope: {} eligible accounts, aggregate outstanding = {}, floor = {}, ceiling = {}, offer = {}",
                bundleScope.size(), bundleTotal,
                decision.getOfferFloor(), decision.getOfferCeiling(), decision.getRecommendedOfferAmount());

        // Attach bundle metadata for the right-panel chips.
        List<ClaudeDecisionDto.BundleAccount> chips = bundleScope.stream()
                .map(a -> ClaudeDecisionDto.BundleAccount.builder()
                        .id(a.getId())
                        .accountNumber(a.getAccountNumber())
                        .productType(a.getProductType())
                        .outstandingAmount(a.getOutstandingAmount())
                        .dpdBucket(a.getDpdBucket())
                        .build())
                .collect(Collectors.toList());

        decision.setBundle(Boolean.TRUE);
        decision.setBundleOutstanding(bundleTotal);
        decision.setBundleAccounts(chips);
        decision.setPrimaryAccountId(null);
        decision.setPrimaryAccountNumber(null);
    }

    /**
     * Non-BUNDLE: offer applies to ONE account. In account scope that's the
     * selected account (already what Claude saw). In portfolio scope, pick the
     * highest-outstanding eligible account so floor/ceiling/offer are NOT the
     * aggregate.
     */
    private void applySingleAccountScope(ClaudeDecisionDto decision, List<Account> scope,
                                         boolean portfolio, List<NegotiationStrategy> strategies, int turnNumber) {
        decision.setBundle(Boolean.FALSE);
        decision.setBundleAccounts(null);
        decision.setBundleOutstanding(null);

        if (scope == null || scope.isEmpty()) return;

        Account primary = scope.get(0);
        if (portfolio) {
            // Pick the highest-outstanding account — that's where a non-bundle
            // offer has the most recovery impact and what Save will use too.
            for (Account a : scope) {
                if (a == null || a.getOutstandingAmount() == null) continue;
                double current = primary.getOutstandingAmount() == null ? 0.0 : primary.getOutstandingAmount();
                if (a.getOutstandingAmount() > current) primary = a;
            }

            // Only re-finalize in portfolio mode — account mode already ran
            // finalize against exactly this account in doAnalyze.
            NegotiationPolicy singlePolicy = policyService.resolveForAccount(primary);
            double singleTotal = primary.getOutstandingAmount() == null ? 0.0 : primary.getOutstandingAmount();

            decision.setOfferFloor(null);
            decision.setOfferCeiling(null);
            decision.setRecommendedOfferAmount(null);
            finalize(decision, singlePolicy, strategies, singleTotal, turnNumber);
            log.info("Single-account scope (portfolio + non-BUNDLE): account #{} outstanding = {}, floor = {}, ceiling = {}, offer = {}",
                    primary.getAccountNumber(), singleTotal,
                    decision.getOfferFloor(), decision.getOfferCeiling(), decision.getRecommendedOfferAmount());
        }

        decision.setPrimaryAccountId(primary.getId());
        decision.setPrimaryAccountNumber(primary.getAccountNumber());
    }

    // ----------------------------------------------------------------
    // Prompt building
    // ----------------------------------------------------------------

    private String buildSystemPrompt(boolean portfolio, NegotiationPolicy policy, List<NegotiationStrategy> strategies) {
        String scopeLine = portfolio
                ? "The conversation is PORTFOLIO-LEVEL — the customer has MULTIPLE accounts and the agent is trying to resolve the whole relationship in one go. Treat the recommended offer amount as covering the TOTAL outstanding across all accounts shown."
                : "The conversation is about ONE specific account between the customer and the bank.";

        String strategyBlock;
        if (strategies == null || strategies.isEmpty()) {
            strategyBlock = "No strategy menu is configured — return HOLD as the default selected_strategy_code.";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("NEGOTIATION STRATEGIES (you MUST pick exactly one `code` from this list):\n");
            for (NegotiationStrategy s : strategies) {
                sb.append("  - code=").append(s.getCode())
                  .append("  name=\"").append(nullToEmpty(s.getName())).append("\"\n");
                if (s.getWhenApplied() != null && !s.getWhenApplied().isBlank()) {
                    sb.append("      WHEN: ").append(s.getWhenApplied()).append("\n");
                }
                if (s.getActionTemplate() != null && !s.getActionTemplate().isBlank()) {
                    sb.append("      ACTION: ").append(s.getActionTemplate()).append("\n");
                }
            }
            strategyBlock = sb.toString();
        }

        return """
               You are a senior debt-recovery negotiation advisor for a LENDER. %s

               === YOUR PRIME DIRECTIVE ===
               Maximize recovery amount for the lender while staying 100%% within POLICY LIMITS.
               You are NOT the customer's friend. Discounts are EARNED by the customer through
               credible objections — never given up front. Every rupee you concede early is money
               the lender permanently loses. Defend margin like it is your own.

               === ANCHORING + STAGED CONCESSION LADDER (CRITICAL) ===
               The user message contains TURN_NUMBER. Use it to control how much you concede:
                 - TURN 1: Open at or very near the CEILING (top of the allowed offer range).
                           Never open low. Never jump to floor on turn 1, even with hardship.
                 - TURN 2: Only concede if customer gave a SPECIFIC, CREDIBLE objection
                           (hardship evidence, affordability detail). Max concession ≈ 25%% of
                           the (ceiling − floor) gap. Vague "I can't pay" = HOLD, not LOWER.
                 - TURN 3: Max cumulative concession ≈ 50%% of the gap.
                 - TURN 4: Max cumulative concession ≈ 75%% of the gap.
                 - TURN 5+: Floor may be approached ONLY if objection_confidence ≥ 0.85 AND
                           hardship is confirmed (job loss, medical, verified income drop).
               A Java-side clamp also enforces this ladder — but you must internalize it so your
               script_line and proposed_reply match the offer you suggest.

               === CONCESSION HIERARCHY (try these IN ORDER before lowering the amount) ===
                 1. HOLD + urgency framing ("this offer is valid today only")
                 2. HOLD + social proof ("most customers in your situation take this")
                 3. INSTALLMENTS — same total INR, split into EMIs. Zero margin loss.
                 4. BUNDLE — if customer has multiple accounts at this lender, package them
                    together for a single settlement (volume, not a deeper discount).
                 5. Structural sweetener — waive a late fee, extend due date by 7 days.
                 6. LOWER the amount — LAST RESORT. Only if steps 1–5 have failed AND objection
                    is credible. Even then, move in small steps inside the turn's allowed band.

               === RECIPROCITY (mandatory when you LOWER) ===
               If you select strategy LOWER, your script_line MUST ask the customer for something
               in return: same-day or 48-hour payment, 20–30%% upfront, auto-debit mandate, or
               closure of a dispute. Never concede for free.

               === OBJECTION DECODING ===
                 - AFFORDABILITY vague ("I can't pay") → test push. HOLD. Do NOT lower.
                 - HARDSHIP specific (lost job, medical, income drop) → real. Try INSTALLMENTS
                   first; LOWER only after that if turn permits.
                 - PARTIAL_WILLINGNESS ("too much in one shot") → INSTALLMENTS, same total.
                 - AVOIDANCE ("let me think") → add urgency, HOLD. Never lower to chase.
                 - DISPUTE ("not my debt") → do not negotiate; escalate, HOLD at status quo.
                 - Repeated "no" with no new info → anchoring tactic. HOLD; a discount will not
                   fix this and will only train the customer to push harder.

               === HISTORY-AWARE RULES ===
                 - If a prior offer at amount X was rejected → next offer may drop at most 5%% below X
                   UNLESS new hardship evidence appeared on this turn.
                 - If customer previously ACCEPTED installments → lead with INSTALLMENTS, not LOWER.
                 - If DPD ≥ 90 → the lender has leverage; anchor higher, not lower.
                 - If this is a serial settlement attempt (3rd+ cycle) → hold firmer.

               === TONE ===
               Empathetic but firm. Never say "best we can do" on turns 1–2. Never apologize for
               price. Prefer "Here's what I can arrange…" over "I'm so sorry, let me try…".

               === OUTPUT ===
               Reply ONLY with a single JSON object — no prose, no markdown.

               SCHEMA (all fields required):
               {"sentiment":"POSITIVE|NEUTRAL|NEGATIVE|MIXED","sentiment_score":<-1.0..1.0>,\
               "summary":"<1 sentence>","proposed_reply":"<2 sentences max, empathetic but firm>",\
               "script_line":"<≤25 words agent reads now; if strategy=LOWER include reciprocity ask>",\
               "objection_type":"AFFORDABILITY|HARDSHIP|PARTIAL_WILLINGNESS|AVOIDANCE|DISPUTE|NONE",\
               "objection_confidence":<0.0..1.0>,"customer_history_summary":"<1 sentence>",\
               "should_offer_settlement":<true|false>,"recommended_discount_percent":<within POLICY>,\
               "recommended_offer_amount":<INR within POLICY and within turn's concession cap>,\
               "recommended_payment_plan":"ONE_TIME|EMI_3|EMI_6|EMI_12",\
               "recommended_installments":<int within POLICY>,\
               "reasoning":"<2 sentences — explain WHY this amount and NOT lower>",\
               "risk_level":"LOW|MEDIUM|HIGH","selected_strategy_code":"<from STRATEGIES>",\
               "strategy_rationale":"<1 sentence, reference the turn + concession ladder>"}

               OBJECTION TYPES: AFFORDABILITY=can't afford now; HARDSHIP=life event (job/medical/divorce); PARTIAL_WILLINGNESS=willing but not full amount; AVOIDANCE=deflecting/delaying; DISPUTE=challenges debt validity; NONE=cooperative.

               HARD GUARDRAIL: recommended_offer_amount and recommended_discount_percent MUST stay
               within the POLICY LIMITS shown in the user message AND must respect the turn's
               concession cap. Never invent a range. When in doubt, HOLD higher.

               %s
               """.formatted(scopeLine, strategyBlock);
    }

    private String buildUserPrompt(Customer c, List<Account> accounts, Transcript t, List<ChatMessage> messages,
                                   boolean portfolio, NegotiationPolicy policy, List<NegotiationStrategy> strategies,
                                   int turnNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("CUSTOMER PROFILE:\n");
        sb.append("  Name: ").append(c.getFullName()).append("\n");
        sb.append("  City: ").append(c.getCity()).append(", ").append(c.getState()).append("\n");
        sb.append("  Annual income (INR): ").append(c.getAnnualIncome()).append("\n\n");

        double totalOut = 0.0;
        if (portfolio) {
            sb.append("CONVERSATION SCOPE: PORTFOLIO (all ").append(accounts.size()).append(" accounts of this customer).\n\n");
            sb.append("ACCOUNTS IN SCOPE:\n");
            for (Account a : accounts) {
                appendAccount(sb, a);
                if (a.getOutstandingAmount() != null) totalOut += a.getOutstandingAmount();
            }
            sb.append(String.format("  TOTAL OUTSTANDING across portfolio: INR %.2f%n%n", totalOut));
        } else if (!accounts.isEmpty()) {
            sb.append("ACCOUNT UNDER DISCUSSION:\n");
            Account a = accounts.get(0);
            appendAccount(sb, a);
            if (a.getOutstandingAmount() != null) totalOut = a.getOutstandingAmount();
            sb.append("\n");
        }

        // --- POLICY BLOCK (compact) ---
        sb.append("POLICY LIMITS (HARD — DO NOT VIOLATE):\n");
        double of, oc;
        if (policy == null) {
            of = 60.0; oc = 100.0;
            sb.append("  discount 0-40% | offer 60-100% of outstanding | installments 1-6\n\n");
        } else {
            double df = nz(policy.getDiscountFloorPct(), 0.0);
            double dc = nz(policy.getDiscountCeilingPct(), 40.0);
            of = nz(policy.getOfferFloorPctOfOutstanding(), 60.0);
            oc = nz(policy.getOfferCeilingPctOfOutstanding(), 100.0);
            int mi = policy.getMinInstallments() == null ? 1 : policy.getMinInstallments();
            int mx = policy.getMaxInstallments() == null ? 6 : policy.getMaxInstallments();
            sb.append(String.format(
                "  discount: %.0f%%–%.0f%% | offer_floor: INR %.0f (%.0f%%) | offer_ceiling: INR %.0f (%.0f%%) | installments: %d–%d%n%n",
                df, dc, totalOut * of / 100.0, of, totalOut * oc / 100.0, oc, mi, mx));
        }

        // --- NEGOTIATION TURN CONTEXT (CRITICAL: drives the concession ladder) ---
        double offerFloorInr   = totalOut * of / 100.0;
        double offerCeilingInr = totalOut * oc / 100.0;
        double maxConcessionPct = concessionCapForTurn(turnNumber);
        double minAllowedThisTurn = offerCeilingInr - (offerCeilingInr - offerFloorInr) * maxConcessionPct;
        sb.append("NEGOTIATION STATE:\n");
        sb.append(String.format("  TURN_NUMBER: %d  (this is the customer's %s utterance)%n",
                turnNumber, ordinal(Math.max(1, turnNumber))));
        sb.append(String.format("  CONCESSION_CAP_THIS_TURN: %.0f%% of (ceiling-floor) gap may be conceded%n",
                maxConcessionPct * 100.0));
        sb.append(String.format("  MIN_OFFER_YOU_MAY_PROPOSE_THIS_TURN: INR %.0f (anchor high, never below this)%n",
                minAllowedThisTurn));
        sb.append("  RULE: Opening turns MUST anchor near ceiling. Lower ONLY with credible objection.\n");
        sb.append("  RULE: Prefer INSTALLMENTS / BUNDLE / HOLD before LOWER. If you LOWER, DEMAND reciprocity.\n\n");

        // Last 6 messages only — keeps prompt lean under high call volume
        sb.append("TRANSCRIPT (last 6 messages, chronological):\n");
        if (messages == null || messages.isEmpty()) {
            sb.append("  (first touch — no messages yet)\n");
        } else {
            List<ChatMessage> recent = messages.size() > 6
                    ? messages.subList(messages.size() - 6, messages.size())
                    : messages;
            for (ChatMessage m : recent) {
                sb.append("  ").append(m.getSenderType())
                  .append(": ").append(m.getContent()).append("\n");
            }
        }
        return sb.toString();
    }

    private void appendAccount(StringBuilder sb, Account a) {
        sb.append("  ").append(a.getAccountNumber())
          .append(" [").append(a.getProductType()).append("]")
          .append(" | outstanding INR ").append(a.getOutstandingAmount())
          .append(" (principal ").append(a.getPrincipalOutstanding())
          .append(", interest ").append(a.getInterestOutstanding())
          .append(", penalty ").append(a.getPenaltyAmount()).append(")")
          .append(" | DPD ").append(a.getDpdBucket()).append(" (").append(a.getDaysPastDue()).append("d)")
          .append(" | last pmt INR ").append(a.getLastPaymentAmount())
          .append(" on ").append(a.getLastPaymentDate()).append("\n");
    }

    // ----------------------------------------------------------------
    // HTTP call
    // ----------------------------------------------------------------

    private String callClaude(String systemPrompt, String userPrompt) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "AI assistant API key is not configured. Set claude.api.key in application.properties.");
        }

        // ── Bedrock Converse API request body ─────────────────────────────
        // POST https://bedrock-runtime.<region>.amazonaws.com/model/<modelId>/converse
        //
        // {
        //   "system": [ { "text": "<system prompt>" } ],
        //   "messages": [ { "role": "user", "content": [ { "text": "<user prompt>" } ] } ],
        //   "inferenceConfig": { "maxTokens": 1500 }
        // }
        // ──────────────────────────────────────────────────────────────────

        ObjectNode body = mapper.createObjectNode();

        // system block — Bedrock expects an array of content objects
        ArrayNode systemNode = body.putArray("system");
        systemNode.addObject().put("text", systemPrompt);

        // messages block — each content item is also an array
        ArrayNode messagesNode = body.putArray("messages");
        ObjectNode userMsg = messagesNode.addObject();
        userMsg.put("role", "user");
        ArrayNode contentNode = userMsg.putArray("content");
        contentNode.addObject().put("text", userPrompt);

        // inferenceConfig — temperature=0 for deterministic, fastest generation
        ObjectNode inferenceConfig = body.putObject("inferenceConfig");
        inferenceConfig.put("maxTokens", maxTokens);
        inferenceConfig.put("temperature", 0.0);

        String payload = body.toString();
        log.info("Calling Bedrock Claude at {} (payload {} bytes)", apiUrl, payload.length());

        // Auth: Bearer token (Bedrock API key format)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(90))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Bedrock response status: {}", resp.statusCode());

        if (resp.statusCode() >= 400) {
            throw new IllegalStateException(
                "Bedrock API returned HTTP " + resp.statusCode() + ": " + resp.body());
        }

        // ── Bedrock Converse API response shape ────────────────────────────
        // {
        //   "output": {
        //     "message": {
        //       "role": "assistant",
        //       "content": [ { "text": "..." } ]
        //     }
        //   },
        //   "stopReason": "end_turn"
        // }
        // ──────────────────────────────────────────────────────────────────
        JsonNode root = mapper.readTree(resp.body());
        JsonNode text = root.path("output").path("message").path("content").path(0).path("text");
        if (!text.isMissingNode() && !text.asText().isBlank()) {
            return text.asText();
        }
        throw new IllegalStateException("Unexpected Bedrock response shape: " + resp.body());
    }

    // ----------------------------------------------------------------
    // Parsing + enforcement
    // ----------------------------------------------------------------

    private ClaudeDecisionDto parseDecision(String text, List<Account> accounts,
                                            NegotiationPolicy policy, List<NegotiationStrategy> strategies) throws Exception {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > -1) trimmed = trimmed.substring(firstNewline + 1);
            if (trimmed.endsWith("```")) trimmed = trimmed.substring(0, trimmed.length() - 3);
            trimmed = trimmed.trim();
        }
        int brace = trimmed.indexOf('{');
        if (brace > 0) trimmed = trimmed.substring(brace);

        JsonNode n = mapper.readTree(trimmed);

        double recPct = n.path("recommended_discount_percent").asDouble(0.0);
        double recOffer = n.path("recommended_offer_amount").asDouble(0.0);
        int recInst = n.path("recommended_installments").asInt(1);

        return ClaudeDecisionDto.builder()
                .sentiment(n.path("sentiment").asText("NEUTRAL"))
                .sentimentScore(n.path("sentiment_score").asDouble(0.0))
                .summary(n.path("summary").asText(""))
                .proposedReply(n.path("proposed_reply").asText(""))
                .scriptLine(n.path("script_line").asText(""))
                .objectionType(n.path("objection_type").asText("NONE"))
                .objectionConfidence(n.path("objection_confidence").asDouble(0.0))
                .customerHistorySummary(n.path("customer_history_summary").asText(""))
                .shouldOfferSettlement(n.path("should_offer_settlement").asBoolean(false))
                .recommendedDiscountPercent(recPct)
                .recommendedOfferAmount(recOffer)
                .recommendedPaymentPlan(n.path("recommended_payment_plan").asText("ONE_TIME"))
                .recommendedInstallments(recInst)
                .reasoning(n.path("reasoning").asText(""))
                .riskLevel(n.path("risk_level").asText("MEDIUM"))
                .selectedStrategyCode(n.path("selected_strategy_code").asText(defaultStrategy(strategies)))
                .strategyRationale(n.path("strategy_rationale").asText(""))
                .build();
    }

    /**
     * Overlay the policy's floor/ceiling onto the decision and clamp the
     * recommended point so it can never leave the configured range. Also
     * fills in policy metadata + the strategy display name.
     */
    private ClaudeDecisionDto finalize(ClaudeDecisionDto d, NegotiationPolicy policy,
                                       List<NegotiationStrategy> strategies, double totalOutstanding,
                                       int turnNumber) {
        double df = policy == null ? 0.0  : nz(policy.getDiscountFloorPct(),       0.0);
        double dc = policy == null ? 40.0 : nz(policy.getDiscountCeilingPct(),    40.0);
        double of = policy == null ? 60.0 : nz(policy.getOfferFloorPctOfOutstanding(),  60.0);
        double oc = policy == null ? 100.0: nz(policy.getOfferCeilingPctOfOutstanding(),100.0);
        int mi = policy == null || policy.getMinInstallments() == null ? 1 : policy.getMinInstallments();
        int mx = policy == null || policy.getMaxInstallments() == null ? 6 : policy.getMaxInstallments();

        // Clamp the discount % and installments to the policy range.
        double recPct = clamp(nz(d.getRecommendedDiscountPercent(), (df + dc) / 2.0), df, dc);
        int    recIn  = clampInt(d.getRecommendedInstallments() == null ? mi : d.getRecommendedInstallments(), mi, mx);

        // The offer-amount range comes directly from the policy's %-of-outstanding
        // bounds applied to the scope's total outstanding. This is a hard bound —
        // Claude cannot widen or narrow it.
        double offerFloorInr   = totalOutstanding * of / 100.0;
        double offerCeilingInr = totalOutstanding * oc / 100.0;
        if (offerFloorInr > offerCeilingInr) {
            double tmp = offerFloorInr; offerFloorInr = offerCeilingInr; offerCeilingInr = tmp;
        }
        double rawOffer = nz(d.getRecommendedOfferAmount(), 0.0);
        if (rawOffer <= 0) {
            // Derive from recommended discount % if Claude didn't give a number.
            rawOffer = totalOutstanding * (1 - recPct / 100.0);
        }

        // PS#8: compute guardrail status BEFORE clamping so we can report what happened.
        String guardrailStatus;
        if (rawOffer < offerFloorInr)        guardrailStatus = "FLOORED";
        else if (rawOffer > offerCeilingInr) guardrailStatus = "CAPPED";
        else                                  guardrailStatus = "WITHIN_LIMITS";

        double recOffer = clamp(rawOffer, offerFloorInr, offerCeilingInr);

        // ── Turn-based concession cap (anchor-high margin defense) ─────────
        // Even if Claude tries to give away the store on turn 1, the Java clamp
        // keeps the offer above (ceiling − gap × turnConcessionCap). This is the
        // hard server-side enforcement of the concession ladder. It prevents the
        // lender from losing money to early, unearned discounts.
        double concessionCap = concessionCapForTurn(turnNumber);
        double minAllowedThisTurn = offerCeilingInr
                - (offerCeilingInr - offerFloorInr) * concessionCap;
        if (recOffer < minAllowedThisTurn) {
            log.info("Concession-cap clamp engaged: turn={} rawOffer={} → clamped to {} (cap {}% of gap, ceiling={}, floor={})",
                    turnNumber, recOffer, minAllowedThisTurn, concessionCap * 100.0, offerCeilingInr, offerFloorInr);
            recOffer = minAllowedThisTurn;
            if ("WITHIN_LIMITS".equals(guardrailStatus)) {
                guardrailStatus = "TURN_CAPPED";
            }
        }
        // Re-derive discount % after any concession clamp so the two stay in sync.
        if (totalOutstanding > 0) {
            recPct = clamp((1.0 - recOffer / totalOutstanding) * 100.0, df, dc);
        }

        // Look up the strategy display name from the code.
        String code = d.getSelectedStrategyCode();
        String name = null;
        if (code != null && strategies != null) {
            for (NegotiationStrategy s : strategies) {
                if (code.equalsIgnoreCase(s.getCode())) { name = s.getName(); break; }
            }
        }
        if (name == null && strategies != null && !strategies.isEmpty() && code == null) {
            NegotiationStrategy first = strategies.get(0);
            code = first.getCode();
            name = first.getName();
        }

        d.setDiscountFloor(df);
        d.setDiscountCeiling(dc);
        d.setRecommendedDiscountPercent(recPct);
        d.setOfferFloor(offerFloorInr);
        d.setOfferCeiling(offerCeilingInr);
        d.setRecommendedOfferAmount(recOffer);
        d.setRecommendedInstallments(recIn);
        d.setSelectedStrategyCode(code);
        d.setSelectedStrategyName(name);
        d.setPolicyId(policy == null ? null : policy.getId());
        d.setPolicyLabel(policy == null ? "Default (no policy row)" : policyLabel(policy));
        // PS#8 fields
        d.setGuardrailStatus(guardrailStatus);
        d.setSuggestedOfferPercent(totalOutstanding > 0 ? (recOffer / totalOutstanding) * 100.0 : 0.0);
        return d;
    }

    private String policyLabel(NegotiationPolicy p) {
        String prod = p.getProductType() == null ? "ANY PRODUCT" : p.getProductType();
        String dpd  = p.getDpdBucket()   == null ? "ANY DPD"     : p.getDpdBucket();
        return prod + " · " + dpd;
    }

    /** Used when the API call fails — keeps the UI usable. */
    private ClaudeDecisionDto fallbackDecision(List<Account> accounts, List<ChatMessage> messages, String errMsg,
                                               NegotiationPolicy policy, List<NegotiationStrategy> strategies) {
        if (accounts == null) accounts = Collections.emptyList();

        int worstDpd = accounts.stream()
                .mapToInt(a -> a.getDaysPastDue() == null ? 0 : a.getDaysPastDue())
                .max().orElse(0);

        double totalOut = accounts.stream()
                .mapToDouble(a -> a.getOutstandingAmount() == null ? 0.0 : a.getOutstandingAmount())
                .sum();

        double df = policy == null ? 0.0  : nz(policy.getDiscountFloorPct(),    0.0);
        double dc = policy == null ? 40.0 : nz(policy.getDiscountCeilingPct(), 40.0);

        // Anchor HIGH on fallback — if the AI is down, we protect the lender by
        // defaulting near the ceiling (smallest discount). Heavier DPD widens the
        // discount slightly but never opens low.
        double bias;
        if (worstDpd >= 180)     bias = 0.40;
        else if (worstDpd >= 90) bias = 0.25;
        else if (worstDpd >= 60) bias = 0.15;
        else                     bias = 0.05;
        double pct = df + (dc - df) * bias;

        double recOffer = totalOut * (1 - pct / 100.0);

        // Fallback strategy: default to HOLD (defend margin when AI is down).
        // Only escalate to LOWER for deep DPD where recovery risk is high enough
        // to justify the discount even without AI-confirmed objection.
        String code = defaultStrategy(strategies);
        if (worstDpd >= 180 && strategies != null) {
            for (NegotiationStrategy s : strategies) {
                if ("LOWER".equalsIgnoreCase(s.getCode())) { code = s.getCode(); break; }
            }
        }

        return ClaudeDecisionDto.builder()
                .sentiment("NEUTRAL")
                .sentimentScore(0.0)
                .summary("(Fallback — AI assistant unavailable) " + (errMsg == null ? "" : errMsg))
                .proposedReply("Thank you for speaking with us. Based on your account status, we'd like to explore a settlement of approximately INR "
                        + String.format("%.0f", recOffer) + ". May we proceed with that discussion?")
                .shouldOfferSettlement(true)
                .recommendedDiscountPercent(pct)
                .recommendedOfferAmount(recOffer)
                .recommendedPaymentPlan("ONE_TIME")
                .recommendedInstallments(1)
                .reasoning("Heuristic based on worst DPD across scope (AI call failed: "
                          + (errMsg == null ? "unknown" : errMsg) + ").")
                .riskLevel(worstDpd >= 90 ? "HIGH" : "MEDIUM")
                .selectedStrategyCode(code)
                .strategyRationale("Fallback heuristic based on DPD.")
                .build();
    }

    // ----------------------------------------------------------------
    // Small helpers
    // ----------------------------------------------------------------

    /**
     * Count the customer's utterances so far — this is the "turn" number that
     * drives the concession ladder. Turn 1 = first customer objection.
     * If no customer messages exist yet (first touch), returns 1.
     */
    private static int deriveTurnNumber(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return 1;
        int count = 0;
        for (ChatMessage m : messages) {
            if (m != null && "CUSTOMER".equalsIgnoreCase(m.getSenderType())) count++;
        }
        return Math.max(1, count);
    }

    /**
     * Concession ladder — what fraction of the (ceiling − floor) gap the model
     * is allowed to concede by this turn. Anchors near ceiling on turn 1 and
     * steps down only as the customer pushes back across turns.
     *
     *   turn 1 → 10%   (basically ceiling-only)
     *   turn 2 → 30%
     *   turn 3 → 55%
     *   turn 4 → 80%
     *   turn 5+ → 100% (floor reachable only here, and only with high confidence)
     */
    private static double concessionCapForTurn(int turn) {
        if (turn <= 1) return 0.10;
        if (turn == 2) return 0.30;
        if (turn == 3) return 0.55;
        if (turn == 4) return 0.80;
        return 1.00;
    }

    private static String ordinal(int n) {
        int v = n % 100;
        if (v >= 11 && v <= 13) return n + "th";
        return switch (n % 10) {
            case 1 -> n + "st";
            case 2 -> n + "nd";
            case 3 -> n + "rd";
            default -> n + "th";
        };
    }

    private static String defaultStrategy(List<NegotiationStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) return "HOLD";
        // Prefer HOLD if present, else first active strategy.
        for (NegotiationStrategy s : strategies) {
            if ("HOLD".equalsIgnoreCase(s.getCode())) return s.getCode();
        }
        return strategies.get(0).getCode();
    }

    private static Long lenderIdFrom(List<Account> accounts) {
        if (accounts == null) return null;
        for (Account a : accounts) {
            if (a == null) continue;
            // Prefer the lender DIRECTLY attached to the account.
            if (a.getLender() != null && a.getLender().getId() != null) {
                return a.getLender().getId();
            }
            if (a.getAssignedAgent() != null && a.getAssignedAgent().getLender() != null) {
                return a.getAssignedAgent().getLender().getId();
            }
        }
        // Fallback for legacy demo rows.
        return 1L;
    }

    private static double nz(Double v, double fallback) { return v == null ? fallback : v; }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
    private static int    clampInt(int v, int lo, int hi)       { return Math.max(lo, Math.min(hi, v)); }
    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
