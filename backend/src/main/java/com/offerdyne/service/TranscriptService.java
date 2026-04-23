package com.offerdyne.service;

import com.offerdyne.entity.Transcript;
import com.offerdyne.entity.TranscriptHistory;
import com.offerdyne.repository.TranscriptHistoryRepository;
import com.offerdyne.repository.TranscriptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Wraps every Transcript mutation in an audit write to TRANSCRIPT_HISTORY.
 * Keeps a full timeline per session for replay / debug / compliance.
 */
@Service
public class TranscriptService {

    private final TranscriptRepository transcripts;
    private final TranscriptHistoryRepository history;

    public TranscriptService(TranscriptRepository transcripts, TranscriptHistoryRepository history) {
        this.transcripts = transcripts;
        this.history = history;
    }

    @Transactional
    public Transcript save(Transcript t, Long changedByAgent, String operation) {
        if (t.getCreatedAt() == null) t.setCreatedAt(LocalDateTime.now());
        Transcript saved = transcripts.save(t);
        archive(saved, changedByAgent, operation);
        return saved;
    }

    private void archive(Transcript t, Long agentId, String op) {
        TranscriptHistory h = new TranscriptHistory();
        h.setTranscriptId(t.getTranscriptId());
        h.setSessionId(t.getSessionId());
        h.setCustomerId(t.getCustomerId());
        h.setAccountId(t.getAccountId());
        h.setTurnIndex(t.getTurnIndex());
        h.setSpeaker(t.getSpeaker());
        h.setUtterance(t.getUtterance());
        h.setSentiment(t.getSentiment());
        h.setSentimentScore(t.getSentimentScore());
        h.setObjectionType(t.getObjectionType());
        h.setSignalsJson(t.getSignalsJson());
        h.setOperation(op);
        h.setChangedByAgent(agentId);
        h.setArchivedAt(LocalDateTime.now());
        history.save(h);
    }
}
