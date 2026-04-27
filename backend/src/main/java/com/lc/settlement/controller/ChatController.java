package com.lc.settlement.controller;

import com.lc.settlement.dto.*;
import com.lc.settlement.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ---- Listing ----

    @GetMapping("/accounts/{accountId}/transcripts")
    public List<TranscriptDto> listForAccount(@PathVariable Long accountId) {
        return chatService.listForAccount(accountId);
    }

    @GetMapping("/customers/{customerId}/transcripts")
    public List<TranscriptDto> listForCustomer(@PathVariable Long customerId) {
        return chatService.listForCustomer(customerId);
    }

    @GetMapping("/customers/{customerId}/portfolio-transcripts")
    public List<TranscriptDto> listPortfolioForCustomer(@PathVariable Long customerId) {
        return chatService.listPortfolioForCustomer(customerId);
    }

    @GetMapping("/transcripts/{transcriptId}")
    public TranscriptDto getTranscript(@PathVariable Long transcriptId) {
        return chatService.getTranscript(transcriptId);
    }

    @GetMapping("/transcripts/{transcriptId}/history")
    public List<TranscriptHistoryDto> history(@PathVariable Long transcriptId) {
        return chatService.history(transcriptId);
    }

    // ---- Turn-based (customer speaks → Claude auto-replies as agent) ----

    /** Account-level turn: send a single customer utterance, get the auto agent reply + decision. */
    @PostMapping("/accounts/turn")
    public TurnResultDto accountTurn(@RequestBody SendMessageRequest req) {
        return chatService.takeAccountTurn(req);
    }

    /** Portfolio-level turn: bundle all customer accounts into one conversation. */
    @PostMapping("/customers/turn")
    public TurnResultDto customerTurn(@RequestBody SendMessageRequest req) {
        return chatService.takeCustomerTurn(req);
    }

    // ---- Legacy raw append + analyze endpoints ----

    @PostMapping("/messages")
    public TranscriptDto sendMessage(@RequestBody SendMessageRequest req) {
        return chatService.appendMessage(req);
    }

    @PostMapping("/transcripts/{transcriptId}/analyze")
    public ClaudeDecisionDto analyze(@PathVariable Long transcriptId) {
        return chatService.analyze(transcriptId);
    }
}
