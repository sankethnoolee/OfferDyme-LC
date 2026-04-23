package com.offerdyne.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "NEGOTIATION_SESSION")
public class NegotiationSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id") private Long sessionId;

    @Column(name = "customer_id")           private Long customerId;
    @Column(name = "account_id")            private Long accountId;
    @Column(name = "agent_id")              private Long agentId;
    @Column(name = "lender_id")             private Long lenderId;
    @Column(name = "session_status")        private String sessionStatus;
    @Column(name = "started_at")            private LocalDateTime startedAt;
    @Column(name = "ended_at")              private LocalDateTime endedAt;
    @Column(name = "initial_offer_percent") private BigDecimal initialOfferPercent;
    @Column(name = "final_offer_percent")   private BigDecimal finalOfferPercent;
    @Column(name = "final_offer_amount")    private BigDecimal finalOfferAmount;
    @Column(name = "strategy_sequence")     private String strategySequence;
    @Column(name = "bundle_flag")           private Boolean bundleFlag;
    @Column(name = "turn_count")            private Integer turnCount;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long v) { this.sessionId = v; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { this.customerId = v; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { this.accountId = v; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long v) { this.agentId = v; }
    public Long getLenderId() { return lenderId; }
    public void setLenderId(Long v) { this.lenderId = v; }
    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String v) { this.sessionStatus = v; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime v) { this.startedAt = v; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime v) { this.endedAt = v; }
    public BigDecimal getInitialOfferPercent() { return initialOfferPercent; }
    public void setInitialOfferPercent(BigDecimal v) { this.initialOfferPercent = v; }
    public BigDecimal getFinalOfferPercent() { return finalOfferPercent; }
    public void setFinalOfferPercent(BigDecimal v) { this.finalOfferPercent = v; }
    public BigDecimal getFinalOfferAmount() { return finalOfferAmount; }
    public void setFinalOfferAmount(BigDecimal v) { this.finalOfferAmount = v; }
    public String getStrategySequence() { return strategySequence; }
    public void setStrategySequence(String v) { this.strategySequence = v; }
    public Boolean getBundleFlag() { return bundleFlag; }
    public void setBundleFlag(Boolean v) { this.bundleFlag = v; }
    public Integer getTurnCount() { return turnCount; }
    public void setTurnCount(Integer v) { this.turnCount = v; }
}
