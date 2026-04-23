package com.offerdyne.strategy;

import com.offerdyne.entity.Account;
import com.offerdyne.entity.Lender;
import com.offerdyne.nlp.NlpAnalysis;
import com.offerdyne.nlp.ObjectionType;
import com.offerdyne.nlp.Sentiment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based strategy selector. Produces an {@link OfferDecision} given the
 * current NegotiationContext. All five strategies — HOLD, LOWER,
 * REFRAME_INSTALLMENTS, BUNDLE, ESCALATE — are implemented with distinct
 * behaviors, satisfying the "&ge; 4 distinct strategies" acceptance criterion.
 *
 * Guardrail is enforced through {@link GuardrailService} inside each branch,
 * and also as a final safety net before return.
 */
@Service
public class StrategyEngine {

    private final GuardrailService guardrail;

    @Value("${offerdyne.negotiation.lower-step-percent:7.5}")
    private double lowerStepPercent;

    @Value("${offerdyne.negotiation.max-turns-before-escalate:10}")
    private int maxTurns;

    public StrategyEngine(GuardrailService guardrail) {
        this.guardrail = guardrail;
    }

    public OfferDecision decide(NegotiationContext ctx) {
        NlpAnalysis sig = ctx.latestSignal;
        BigDecimal outstanding = ctx.isBundled
                ? totalOutstanding(ctx.customerAccounts)
                : ctx.anchorAccount.getOutstandingAmount();

        // Too many turns -> ESCALATE
        if (ctx.turnIndex >= maxTurns) {
            return escalate(ctx.lender, outstanding);
        }

        // Clear accept signal -> hold at last offer (accept flow handled elsewhere)
        if (sig != null && sig.getObjectionType() == ObjectionType.ACCEPTANCE) {
            return holdWithoutMoving(ctx, outstanding);
        }

        // BUNDLE: borrower has multiple delinquent accounts + lender allows + not already bundled
        if (!ctx.isBundled
                && ctx.lender.getBundlingAllowed()
                && countDelinquent(ctx.customerAccounts) >= 2
                && ctx.turnIndex >= 1) {
            return bundle(ctx, outstanding);
        }

        if (sig == null) {
            return hold(ctx, outstanding, "Opening offer");
        }

        switch (sig.getObjectionType()) {
            case JOB_LOSS:
            case AFFORDABILITY:
                // Strong affordability -> LOWER offer by 5-10%
                if (sig.isCapacitySignalConfirmed()) {
                    return lower(ctx, outstanding);
                }
                return hold(ctx, outstanding, "Holding — affordability unconfirmed");

            case WILLINGNESS:
                // wants to pay but rejected lump sum -> REFRAME as installments
                return reframeInstallments(ctx, outstanding);

            case TIMING:
                // offer installments with first payment delayed
                return reframeInstallments(ctx, outstanding);

            case DISPUTE:
                // Hand to supervisor
                return escalate(ctx.lender, outstanding);

            case NONE:
            case ACCEPTANCE:
            default:
                if (sig.getSentiment() == Sentiment.NEGATIVE && ctx.consecutiveRejections >= 2) {
                    return lower(ctx, outstanding);
                }
                return hold(ctx, outstanding, "Wavering — adding urgency");
        }
    }

    // ------------------- Strategy implementations -------------------

    /** HOLD: keep current offer, reframe with urgency. */
    private OfferDecision hold(NegotiationContext ctx, BigDecimal outstanding, String reason) {
        BigDecimal pct = ctx.currentOfferPercent != null
                ? ctx.currentOfferPercent
                : ctx.lender.getCeilingPercent();
        return buildDecision(StrategyType.HOLD, ctx.lender, pct, outstanding,
                "We're holding at " + pct + "%. This offer expires at end of day — can we close now? ("
                        + reason + ")",
                null, false);
    }

    private OfferDecision holdWithoutMoving(NegotiationContext ctx, BigDecimal outstanding) {
        BigDecimal pct = ctx.currentOfferPercent != null
                ? ctx.currentOfferPercent
                : ctx.lender.getCeilingPercent();
        return buildDecision(StrategyType.HOLD, ctx.lender, pct, outstanding,
                "Confirming " + pct + "% settlement. Sending the letter now.", null, false);
    }

    /** LOWER: reduce by configured step percent within guardrail. */
    private OfferDecision lower(NegotiationContext ctx, BigDecimal outstanding) {
        BigDecimal base = ctx.currentOfferPercent != null
                ? ctx.currentOfferPercent
                : ctx.lender.getCeilingPercent();
        BigDecimal target = base.subtract(BigDecimal.valueOf(lowerStepPercent));
        return buildDecision(StrategyType.LOWER, ctx.lender, target, outstanding,
                "I hear the constraint. Dropping to %s%% — this is the best we can do on an unsecured account."
                        .replace("%s", target.setScale(2, RoundingMode.HALF_UP).toPlainString()),
                null, false);
    }

    /** REFRAME_INSTALLMENTS: same total, split into N installments. */
    private OfferDecision reframeInstallments(NegotiationContext ctx, BigDecimal outstanding) {
        int count = Math.min(3, Math.max(2, ctx.lender.getMaxInstallments()));
        BigDecimal pct = ctx.currentOfferPercent != null
                ? ctx.currentOfferPercent
                : ctx.lender.getCeilingPercent();
        // Keep within guardrail
        BigDecimal total = pct.multiply(outstanding).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal each = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        if (each.compareTo(ctx.lender.getMinInstallmentAmt()) < 0) {
            // fall back to lump sum
            return hold(ctx, outstanding, "Installments below min size");
        }
        StringBuilder plan = new StringBuilder("[");
        for (int i = 1; i <= count; i++) {
            plan.append("{\"installment\":").append(i).append(",\"amount\":").append(each).append("}");
            if (i < count) plan.append(",");
        }
        plan.append("]");
        String framing = "Let's split " + total + " into " + count
                + " equal installments of " + each + ". Same total, easier monthly hit.";
        return buildDecision(StrategyType.REFRAME_INSTALLMENTS, ctx.lender, pct, outstanding,
                framing, plan.toString(), false);
    }

    /** BUNDLE: consolidate all customer accounts into a single deal. */
    private OfferDecision bundle(NegotiationContext ctx, BigDecimal totalOutstanding) {
        // Offer ceiling on the combined balance (lender may allow a small bundle discount)
        BigDecimal pct = ctx.lender.getCeilingPercent().subtract(BigDecimal.valueOf(5));
        int n = countDelinquent(ctx.customerAccounts);
        String framing = "You have " + n + " accounts with us. Let me bundle them at "
                + pct.setScale(2, RoundingMode.HALF_UP) + "% of the combined " + totalOutstanding
                + " — one payment, all closed together.";
        return buildDecision(StrategyType.BUNDLE, ctx.lender, pct, totalOutstanding,
                framing, null, false);
    }

    /** ESCALATE: stop offering, hand to supervisor. Terminal. */
    private OfferDecision escalate(Lender lender, BigDecimal outstanding) {
        BigDecimal pct = lender.getCeilingPercent();  // record ceiling for audit
        OfferDecision d = buildDecision(StrategyType.ESCALATE, lender, pct, outstanding,
                "Escalating to supervisor — unable to reach agreement within authority.", null, true);
        d.offerAmount = BigDecimal.ZERO;   // no active offer
        d.offerPercent = BigDecimal.ZERO;
        return d;
    }

    // ------------------- Utility -------------------

    private OfferDecision buildDecision(StrategyType type, Lender lender,
                                        BigDecimal requestedPct, BigDecimal outstanding,
                                        String framing, String installmentPlan, boolean terminal) {
        GuardrailService.Result g = guardrail.enforce(lender, requestedPct);
        OfferDecision d = new OfferDecision();
        d.strategy = type;
        d.offerPercent = g.safePercent.setScale(2, RoundingMode.HALF_UP);
        d.offerAmount  = outstanding.multiply(d.offerPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        d.framingText  = framing;
        d.installmentPlanJson = installmentPlan;
        d.guardrailPassed = g.originallyWithinBounds;
        d.guardrailReason = g.reason;
        d.terminal = terminal;
        return d;
    }

    private BigDecimal totalOutstanding(List<Account> accts) {
        BigDecimal sum = BigDecimal.ZERO;
        if (accts == null) return sum;
        for (Account a : accts) {
            if (a.getOutstandingAmount() != null) sum = sum.add(a.getOutstandingAmount());
        }
        return sum;
    }

    private int countDelinquent(List<Account> accts) {
        if (accts == null) return 0;
        int c = 0;
        for (Account a : accts) {
            if ("DELINQUENT".equalsIgnoreCase(a.getAccountStatus())
                || "IN_NEGOTIATION".equalsIgnoreCase(a.getAccountStatus())) c++;
        }
        return c;
    }

    public List<StrategyType> supportedStrategies() {
        return new ArrayList<>(List.of(
                StrategyType.HOLD, StrategyType.LOWER,
                StrategyType.REFRAME_INSTALLMENTS, StrategyType.BUNDLE,
                StrategyType.ESCALATE));
    }
}
