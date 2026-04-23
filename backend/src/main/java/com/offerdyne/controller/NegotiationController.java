package com.offerdyne.controller;

import com.offerdyne.dto.ApiDtos;
import com.offerdyne.entity.Offer;
import com.offerdyne.entity.Transcript;
import com.offerdyne.repository.OfferRepository;
import com.offerdyne.repository.TranscriptRepository;
import com.offerdyne.service.NegotiationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/negotiations")
public class NegotiationController {

    private final NegotiationService service;
    private final TranscriptRepository transcripts;
    private final OfferRepository offers;

    public NegotiationController(NegotiationService service,
                                 TranscriptRepository transcripts,
                                 OfferRepository offers) {
        this.service = service;
        this.transcripts = transcripts;
        this.offers = offers;
    }

    /** Pull current agent id from the AgentAuthFilter. Default to 1 for demos. */
    private Long agentId(HttpServletRequest req) {
        Object a = req.getAttribute("agentId");
        if (a instanceof Long l) return l;
        return 1L;
    }

    @PostMapping("/start")
    public ApiDtos.SessionResponse start(HttpServletRequest req,
                                         @RequestBody ApiDtos.StartSessionRequest body) {
        return service.startSession(agentId(req), body);
    }

    @PostMapping("/{sessionId}/turn")
    public ApiDtos.TurnResponse turn(HttpServletRequest req,
                                     @PathVariable Long sessionId,
                                     @RequestBody ApiDtos.TurnRequest body) {
        return service.processTurn(sessionId, agentId(req), body);
    }

    @PostMapping("/{sessionId}/accept")
    public ApiDtos.SettlementDto accept(HttpServletRequest req,
                                        @PathVariable Long sessionId) {
        return service.acceptLastOffer(sessionId, agentId(req));
    }

    @PostMapping("/{sessionId}/reject")
    public void reject(@PathVariable Long sessionId) {
        service.rejectAndEnd(sessionId);
    }

    @GetMapping("/{sessionId}/transcript")
    public List<Transcript> transcript(@PathVariable Long sessionId) {
        return transcripts.findBySessionIdOrderByTurnIndexAsc(sessionId);
    }

    @GetMapping("/{sessionId}/offers")
    public List<Offer> offers(@PathVariable Long sessionId) {
        return offers.findBySessionIdOrderByMadeAtAsc(sessionId);
    }
}
