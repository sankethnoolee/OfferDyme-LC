package com.lc.settlement.controller;

import com.lc.settlement.entity.Settlement;
import com.lc.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PS#8 acceptance-rate analytics — "demonstrated improvement vs static baseline".
 *
 * GET /api/analytics/acceptance-rate
 *   Returns per-strategy and per-source (CLAUDE_AI vs AGENT) acceptance rates.
 *   Used in the demo to show dynamic AI suggestions beat the static baseline.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final SettlementRepository settlementRepo;

    // ── Response shape ────────────────────────────────────────────────

    public record StrategyStats(
        String strategyCode,
        String source,
        long   total,
        long   accepted,
        long   rejected,
        double acceptancePct
    ) {}

    public record AcceptanceRateResponse(
        List<StrategyStats> byStrategy,
        StrategyStats       overall,
        StrategyStats       claudeAiTotal,
        StrategyStats       agentTotal,
        String              verdict           // "AI_BETTER" | "EQUAL" | "AGENT_BETTER"
    ) {}

    // ── Endpoint ─────────────────────────────────────────────────────

    @GetMapping("/acceptance-rate")
    public AcceptanceRateResponse acceptanceRate() {
        List<Settlement> all = settlementRepo.findAll();

        // Breakdown by strategy × source
        Map<String, List<Settlement>> grouped = all.stream()
            .collect(Collectors.groupingBy(s ->
                nullSafe(s.getStrategyCode()) + "|" + nullSafe(s.getSource())));

        List<StrategyStats> byStrategy = grouped.entrySet().stream()
            .map(e -> {
                String[] parts  = e.getKey().split("\\|");
                String strategy = parts[0];
                String source   = parts.length > 1 ? parts[1] : "UNKNOWN";
                return toStats(strategy, source, e.getValue());
            })
            .sorted(Comparator.comparing(StrategyStats::strategyCode))
            .collect(Collectors.toList());

        // Overall
        StrategyStats overall     = toStats("ALL", "ALL", all);

        // CLAUDE_AI subtotal
        List<Settlement> aiRows   = all.stream()
            .filter(s -> "CLAUDE_AI".equalsIgnoreCase(s.getSource()))
            .collect(Collectors.toList());
        StrategyStats claudeTotals = toStats("ALL", "CLAUDE_AI", aiRows);

        // AGENT (manual) subtotal
        List<Settlement> agentRows = all.stream()
            .filter(s -> "AGENT".equalsIgnoreCase(s.getSource()))
            .collect(Collectors.toList());
        StrategyStats agentTotals  = toStats("ALL", "AGENT", agentRows);

        // Verdict — was AI better than the static (AGENT) baseline?
        String verdict;
        if (agentRows.isEmpty()) {
            verdict = "NO_BASELINE_YET";
        } else if (claudeTotals.acceptancePct() > agentTotals.acceptancePct() + 1.0) {
            verdict = "AI_BETTER";
        } else if (claudeTotals.acceptancePct() < agentTotals.acceptancePct() - 1.0) {
            verdict = "AGENT_BETTER";
        } else {
            verdict = "EQUAL";
        }

        return new AcceptanceRateResponse(byStrategy, overall, claudeTotals, agentTotals, verdict);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private StrategyStats toStats(String strategy, String source, List<Settlement> rows) {
        long total    = rows.size();
        long accepted = rows.stream().filter(s -> "ACCEPTED".equalsIgnoreCase(s.getStatus())).count();
        long rejected = rows.stream().filter(s -> "REJECTED".equalsIgnoreCase(s.getStatus())).count();
        double pct    = total == 0 ? 0.0 : Math.round((accepted * 1000.0 / total)) / 10.0;
        return new StrategyStats(strategy, source, total, accepted, rejected, pct);
    }

    private static String nullSafe(String s) { return s == null ? "UNKNOWN" : s; }
}
