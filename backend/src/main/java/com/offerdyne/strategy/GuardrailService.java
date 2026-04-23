package com.offerdyne.strategy;

import com.offerdyne.entity.Lender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Hard guardrail enforcement. Any offer below floor or above ceiling is
 * clamped and flagged. 100% compliance is a non-negotiable acceptance
 * criterion, so this service is the single source of truth for bounds.
 */
@Service
public class GuardrailService {

    public static final class Result {
        public final BigDecimal safePercent;
        public final boolean originallyWithinBounds;
        public final String reason;
        public Result(BigDecimal p, boolean ok, String why) {
            this.safePercent = p; this.originallyWithinBounds = ok; this.reason = why;
        }
    }

    /** Clamp the requested percent to [floor, ceiling]. Never returns out-of-bound. */
    public Result enforce(Lender lender, BigDecimal requestedPercent) {
        BigDecimal floor = lender.getFloorPercent();
        BigDecimal ceil  = lender.getCeilingPercent();
        if (requestedPercent == null) {
            return new Result(ceil, false, "NULL_REQUEST_CLAMPED_TO_CEILING");
        }
        if (requestedPercent.compareTo(floor) < 0) {
            return new Result(floor, false,
                    "REQUESTED " + requestedPercent + "% BELOW FLOOR " + floor + "% — CLAMPED");
        }
        if (requestedPercent.compareTo(ceil) > 0) {
            return new Result(ceil, false,
                    "REQUESTED " + requestedPercent + "% ABOVE CEILING " + ceil + "% — CLAMPED");
        }
        return new Result(requestedPercent, true, "WITHIN_BOUNDS");
    }
}
