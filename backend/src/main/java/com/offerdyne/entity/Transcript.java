package com.offerdyne.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRANSCRIPT")
public class Transcript {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transcript_id") private Long transcriptId;

    @Column(name = "session_id")      private Long sessionId;
    @Column(name = "customer_id")     private Long customerId;
    @Column(name = "account_id")      private Long accountId;
    @Column(name = "turn_index")      private Integer turnIndex;
    @Column(name = "speaker")         private String speaker;
    @Lob @Column(name = "utterance")  private String utterance;
    @Column(name = "sentiment")       private String sentiment;
    @Column(name = "sentiment_score") private BigDecimal sentimentScore;
    @Column(name = "objection_type")  private String objectionType;
    @Lob @Column(name = "signals_json") private String signalsJson;
    @Column(name = "created_at")      private LocalDateTime createdAt;

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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
