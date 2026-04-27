package com.lc.settlement.dto;

import lombok.*;

import java.util.List;

/**
 * The structured result Claude returns after reviewing an account + transcript.
 * Each numeric recommendation comes with a floor + ceiling so the UI can render
 * a slider: agent sees the negotiating room, not just a single point value.
 *
 * The floor/ceiling values come from the DB-configured `negotiation_policy`
 * (bank-set, stable, auditable). Claude only picks the CURRENT point within
 * that fixed range, and picks one of the DB-configured strategies.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ClaudeDecisionDto {

    /** Account breakdown rendered in the right panel for BUNDLE proposals. */
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class BundleAccount {
        private Long id;
        private String accountNumber;
        private String productType;
        private Double outstandingAmount;
        private String dpdBucket;
    }

    /** POSITIVE | NEUTRAL | NEGATIVE | MIXED */
    private String sentiment;

    /** Score between -1.0 and 1.0 — used to position the sentiment gauge. */
    private Double sentimentScore;

    /** Short summary of where the conversation stands. */
    private String summary;

    /** The agent's next reply to send to the customer, in their voice. */
    private String proposedReply;

    /**
     * PS#8 — One tight persuasion line (≤25 words) the agent reads verbatim right now.
     * E.g. "We can split this into ₹14,000 now + ₹14,000 in 30 days — would that work?"
     */
    private String scriptLine;

    // ---- PS#8 Objection Classification ----
    /** PS#8: AFFORDABILITY | HARDSHIP | PARTIAL_WILLINGNESS | AVOIDANCE | DISPUTE */
    private String objectionType;

    /** PS#8: Confidence of the objection classification, 0.0 – 1.0 */
    private Double objectionConfidence;

    // ---- PS#8 Guardrail Status ----
    /**
     * PS#8: WITHIN_LIMITS | FLOORED | CAPPED
     * Computed server-side in finalize() — proof that guardrails are working.
     * FLOORED  = raw AI offer was below floor, clamped UP.
     * CAPPED   = raw AI offer was above ceiling, clamped DOWN.
     * WITHIN_LIMITS = no clamping needed.
     */
    private String guardrailStatus;

    /**
     * PS#8: offer ÷ outstanding × 100 — recovery % (NOT discount %).
     * E.g. 56.0 means agent is offering to recover 56% of outstanding.
     */
    private Double suggestedOfferPercent;

    /**
     * PS#8: One-sentence summary of this customer's past offer behaviour.
     * E.g. "Rejected lump sum twice. Responded positively to installment offer in Feb 2024."
     */
    private String customerHistorySummary;

    /** Whether Claude recommends proposing a settlement now. */
    private Boolean shouldOfferSettlement;

    // ---- Discount range (percent) — floor/ceiling come from policy ----
    private Double discountFloor;         // minimum acceptable discount %
    private Double recommendedDiscountPercent;
    private Double discountCeiling;       // maximum defensible discount %

    // ---- Offer range (INR) — floor/ceiling come from policy ----
    private Double offerFloor;            // lowest amount the bank would accept
    private Double recommendedOfferAmount;
    private Double offerCeiling;          // highest amount we could realistically collect

    /** ONE_TIME | EMI_3 | EMI_6 | EMI_12 */
    private String recommendedPaymentPlan;
    private Integer recommendedInstallments;

    /** Plain-English reasoning the agent can read. */
    private String reasoning;

    /** LOW | MEDIUM | HIGH risk level of the customer defaulting further. */
    private String riskLevel;

    // ---- Strategy (picked from the DB-configured strategy list) ----
    private String selectedStrategyCode;
    private String selectedStrategyName;
    private String strategyRationale;

    // ---- Policy metadata (for transparency in the UI) ----
    private Long   policyId;
    private String policyLabel;

    // ---- Bundle preview (populated when selectedStrategyCode == BUNDLE) ----
    /** True when the displayed offer is an aggregate across multiple accounts. */
    private Boolean bundle;

    /** Aggregated outstanding across {@link #bundleAccounts} — matches offer base. */
    private Double bundleOutstanding;

    /**
     * The accounts that would be covered if the agent clicks "Save settlement".
     * Already filtered to exclude accounts with an ACCEPTED settlement — i.e.
     * the same set the save path will persist as linkedAccounts.
     */
    private List<BundleAccount> bundleAccounts;

    /** Human-readable note if bundle had to be downgraded (e.g. only 1 eligible account). */
    private String bundleNote;

    // ---- Single-account context (non-BUNDLE strategies) ----
    /**
     * The account the current offer applies to when strategy is NOT BUNDLE.
     * For account-scope chats this is the selected account. For portfolio
     * chats we pick the highest-outstanding eligible account so the floor /
     * ceiling / offer below are derived from that one account — not the
     * portfolio aggregate.
     */
    private Long primaryAccountId;
    private String primaryAccountNumber;
}
