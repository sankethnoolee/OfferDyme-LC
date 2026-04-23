package com.offerdyne.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRANSCRIPT_HISTORY")
public class TranscriptHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id") private Long historyId;

    @Column(name = "transcript_id") private Long transcriptId;
    @Column(name = "session_id")    private Long sessionId;
    @Column(name = "customer_id")   private Long customerId;
    @Column(name = "account_id")    private Long accountId;
    @Column(name = "turn_index")    private Integer turnIndex;
    @Column(name = "speaker")       private String speaker;
    @Lob @Column(name = "utterance") private String utterance;
    @Column(name = "sentiment")     private String sentiment;
    @Column(name = "sentiment_score") private BigDecimal sentimentScore;
    @Column(name = "objection_type") private String objectionType;
    @Lob @Column(name = "signals_json") private String signalsJson;
    @Column(name = "operation")     private String operation;
    @Column(name = "changed_by_agent") private Long changedByAgent;
    @Column(name = "archived_at")   private LocalDateTime archivedAt;

    public Long getHistoryId() { return historyId; }
    public void setHistoryId(Long v) { this.historyId = v; }
    public Long getTranscriptId() { return transcriptId; }
    public void setTranscriptId(Long v) { this.transcriptId = v; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long v) { this.sessionId = v; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { this.customerId = v; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { this.accountId = v; }
    public Integer getTurnIndex() { return turnIndex; }
    public void setTurnIndex(Integer v) { this.turnIndex = v; }
    public String getSpeaker() { return speaker; }
    public void setSpeaker(String v) { this.speaker = v; }
    public String getUtterance() { return utterance; }
    public void setUtterance(String v) { this.utterance = v; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String v) { this.sentiment = v; }
    public BigDecimal getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(BigDecimal v) { this.sentimentScore = v; }
    public String getObjectionType() { return objectionType; }
    public void setObjectionType(String v) { this.objectionType = v; }
    public String getSignalsJson() { return signalsJson; }
    public void setSignalsJson(String v) { this.signalsJson = v; }
    public String getOperation() { return operation; }
    public void setOperation(String v) { this.operation = v; }
    public Long getChangedByAgent() { return changedByAgent; }
    public void setChangedByAgent(Long v) { this.changedByAgent = v; }
    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime v) { this.archivedAt = v; }
}
