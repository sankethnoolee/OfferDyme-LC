package com.offerdyne.controller;

import com.offerdyne.entity.Transcript;
import com.offerdyne.entity.TranscriptHistory;
import com.offerdyne.repository.TranscriptHistoryRepository;
import com.offerdyne.repository.TranscriptRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transcripts")
public class TranscriptController {

    private final TranscriptRepository transcripts;
    private final TranscriptHistoryRepository history;

    public TranscriptController(TranscriptRepository t, TranscriptHistoryRepository h) {
        this.transcripts = t; this.history = h;
    }

    @GetMapping("/by-customer/{id}")
    public List<Transcript> byCustomer(@PathVariable Long id) {
        return transcripts.findByCustomerIdOrderByCreatedAtDesc(id);
    }

    @GetMapping("/by-account/{id}")
    public List<Transcript> byAccount(@PathVariable Long id) {
        return transcripts.findByAccountIdOrderByCreatedAtDesc(id);
    }

    @GetMapping("/by-session/{id}")
    public List<Transcript> bySession(@PathVariable Long id) {
        return transcripts.findBySessionIdOrderByTurnIndexAsc(id);
    }

    @GetMapping("/history/by-session/{id}")
    public List<TranscriptHistory> historyBySession(@PathVariable Long id) {
        return history.findBySessionIdOrderByArchivedAtAsc(id);
    }

    @GetMapping("/history/by-transcript/{id}")
    public List<TranscriptHistory> historyByTranscript(@PathVariable Long id) {
        return history.findByTranscriptIdOrderByArchivedAtAsc(id);
    }
}
