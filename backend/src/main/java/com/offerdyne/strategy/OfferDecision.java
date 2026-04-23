package com.offerdyne.strategy;

import java.math.BigDecimal;

/** Strategy engine output: next offer + supporting metadata. */
public class OfferDecision {
    public StrategyType strategy;
    public BigDecimal offerPercent;       // of outstanding
    public BigDecimal offerAmount;
    public String framingText;
    public String installmentPlanJson;    // null unless installments
    public boolean guardrailPassed;
    public String guardrailReason;
    public boolean terminal;              // e.g. ESCALATE
}
