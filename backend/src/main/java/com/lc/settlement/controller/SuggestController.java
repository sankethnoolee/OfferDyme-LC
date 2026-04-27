package com.lc.settlement.controller;

import com.lc.settlement.dto.SendMessageRequest;
import com.lc.settlement.dto.TurnResultDto;
import com.lc.settlement.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * PS#8 API contract endpoint.
 *
 * POST /api/settlement/suggest
 * Input  : { account_id, customer_utterance, session_id, turn_number }
 * Output : TurnResultDto (contains ClaudeDecisionDto with all PS#8 fields:
 *           objection_type, objection_confidence, suggested_strategy,
 *           suggested_offer_amount, suggested_offer_percent, script_line,
 *           guardrail_status, lender_floor, lender_ceiling,
 *           customer_history_summary)
 *
 * This is a thin adapter over the existing ChatService.takeAccountTurn().
 * All business logic, guardrails, and AI calls live in ChatService / ClaudeService.
 */
@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
@Slf4j
public class SuggestController {

    private final ChatService chatService;

    /**
     * Maps session_id (string from PS#8 contract) → internal transcript_id (Long).
     * In-memory for the hackathon — survives the demo session.
     */
    private final ConcurrentHashMap<String, Long> sessionToTranscript = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // PS#8 request body
    // -------------------------------------------------------------------------

    public record SuggestRequest(
            Long   account_id,
            String customer_utterance,
            String session_id,
            int    turn_number
    ) {}

    // -------------------------------------------------------------------------
    // Main endpoint — POST /api/settlement/suggest
    // -------------------------------------------------------------------------

    @PostMapping("/suggest")
    public TurnResultDto suggest(@RequestBody SuggestRequest req) {
        log.info("PS#8 /suggest → account={} session={} turn={}", req.account_id(), req.session_id(), req.turn_number());

        // Delegate to existing turn-based logic (builds full context, calls Claude, enforces guardrails)
        SendMessageRequest smr = SendMessageRequest.builder()
                .accountId(req.account_id())
                .content(req.customer_utterance())
                .senderType("CUSTOMER")
                .build();

        TurnResultDto result = chatService.takeAccountTurn(smr);

        // Track the session → transcript mapping for future turns
        if (result.getTranscript() != null && result.getTranscript().getId() != null) {
            sessionToTranscript.put(req.session_id(), result.getTranscript().getId());
        }

        return result;
    }

}
