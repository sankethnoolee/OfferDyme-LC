package com.lc.settlement.controller;

import com.lc.settlement.dto.ClaudeDecisionDto;
import com.lc.settlement.dto.SettlementDto;
import com.lc.settlement.service.SettlementService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/accounts/{accountId}")
    public List<SettlementDto> listForAccount(@PathVariable Long accountId) {
        return settlementService.listForAccount(accountId);
    }

    @GetMapping("/customers/{customerId}")
    public List<SettlementDto> listForCustomer(@PathVariable Long customerId) {
        return settlementService.listForCustomer(customerId);
    }

    @PostMapping("/from-claude")
    public SettlementDto createFromClaude(@RequestBody CreateFromClaudeRequest req) {
        return settlementService.createFromClaudeDecision(
                req.getAccountId(), req.getAgentId(), req.getTranscriptId(), req.getDecision());
    }

    @PostMapping("/manual")
    public SettlementDto createManual(@RequestBody CreateManualRequest req) {
        return settlementService.createManual(
                req.getAccountId(), req.getAgentId(), req.getTranscriptId(),
                req.getOfferedAmount(), req.getDiscountPercent(),
                req.getPaymentPlan(), req.getInstallments(), req.getRationale(),
                req.getStrategyCode());
    }

    @PostMapping("/{id}/status")
    public SettlementDto updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        return settlementService.updateStatus(id, req.getStatus(), req.getCustomerResponse());
    }

    // ----------------------------------------------------------------
    // Error handling — surfaces guardrail / duplicate errors to the UI.
    // ----------------------------------------------------------------

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage() == null ? "Conflict" : ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage() == null ? "Bad request" : ex.getMessage()));
    }

    @Getter @Setter @NoArgsConstructor
    public static class CreateFromClaudeRequest {
        private Long accountId;
        private Long agentId;
        private Long transcriptId;
        private ClaudeDecisionDto decision;
    }

    @Getter @Setter @NoArgsConstructor
    public static class CreateManualRequest {
        private Long accountId;
        private Long agentId;
        private Long transcriptId;
        private Double offeredAmount;
        private Double discountPercent;
        private String paymentPlan;
        private Integer installments;
        private String rationale;
        private String strategyCode;
    }

    @Getter @Setter @NoArgsConstructor
    public static class StatusUpdateRequest {
        private String status;
        private String customerResponse;
    }
}
