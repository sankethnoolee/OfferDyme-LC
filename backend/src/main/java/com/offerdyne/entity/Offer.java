package com.offerdyne.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "OFFER")
public class Offer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id") private Long offerId;

    @Column(name = "session_id")      private Long sessionId;
    @Column(name = "turn_index")      private Integer turnIndex;
    @Column(name = "strategy")        private String strategy;
    @Column(name = "offer_percent")   private BigDecimal offerPercent;
    @Column(name = "offer_amount")    private BigDecimal offerAmount;
    @Lob @Column(name = "framing_text")           private String framingText;
    @Lob @Column(name = "installment_plan_json") private String installmentPlanJson;
    @Column(name = "guardrail_check_passed") private Boolean guardrailCheckPassed;
    @Column(name = "guardrail_reason")       private String guardrailReason;
    @Column(name = "accepted")        private Boolean accepted;
    @Column(name = "made_at")         private LocalDateTime madeAt;

    public Long getOfferId() { return offerId; }
    public void setOfferId(Long v) { this.offerId = v; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long v) { this.sessionId = v; }
    public Integer getTurnIndex() { return turnIndex; }
    public void setTurnIndex(Integer v) { this.turnIndex = v; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String v) { this.strategy = v; }
    public BigDecimal getOfferPercent() { return offerPercent; }
    public void setOfferPercent(BigDecimal v) { this.offerPercent = v; }
    public BigDecimal getOfferAmount() { return offerAmount; }
    public void setOfferAmount(BigDecimal v) { this.offerAmount = v; }
    public String getFramingText() { return framingText; }
    public void setFramingText(String v) { this.framingText = v; }
    public String getInstallmentPlanJson() { return installmentPlanJson; }
    public void setInstallmentPlanJson(String v) { this.installmentPlanJson = v; }
    public Boolean getGuardrailCheckPassed() { return guardrailCheckPassed; }
    public void setGuardrailCheckPassed(Boolean v) { this.guardrailCheckPassed = v; }
    public String getGuardrailReason() { return guardrailReason; }
    public void setGuardrailReason(String v) { this.guardrailReason = v; }
    public Boolean getAccepted() { return accepted; }
    public void setAccepted(Boolean v) { this.accepted = v; }
    public LocalDateTime getMadeAt() { return madeAt; }
    public void setMadeAt(LocalDateTime v) { this.madeAt = v; }
}
