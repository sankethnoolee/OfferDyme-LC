package com.offerdyne.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SETTLEMENT")
public class Settlement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id") private Long settlementId;

    @Column(name = "session_id")  private Long sessionId;
    @Column(name = "customer_id") private Long customerId;
    @Column(name = "account_id")  private Long accountId;
    @Column(name = "agent_id")    private Long agentId;
    @Column(name = "lender_id")   private Long lenderId;
    @Column(name = "settled_percent")   private BigDecimal settledPercent;
    @Column(name = "settled_amount")    private BigDecimal settledAmount;
    @Column(name = "settlement_type")   private String settlementType;
    @Column(name = "installment_count") private Integer installmentCount;
    @Lob @Column(name = "installment_plan") private String installmentPlan;
    @Column(name = "bundle_group_id")   private String bundleGroupId;
    @Column(name = "status")            private String status;
    @Column(name = "settled_at")        private LocalDateTime settledAt;

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long v) { this.settlementId = v; }
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
    public BigDecimal getSettledPercent() { return settledPercent; }
    public void setSettledPercent(BigDecimal v) { this.settledPercent = v; }
    public BigDecimal getSettledAmount() { return settledAmount; }
    public void setSettledAmount(BigDecimal v) { this.settledAmount = v; }
    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String v) { this.settlementType = v; }
    public Integer getInstallmentCount() { return installmentCount; }
    public void setInstallmentCount(Integer v) { this.installmentCount = v; }
    public String getInstallmentPlan() { return installmentPlan; }
    public void setInstallmentPlan(String v) { this.installmentPlan = v; }
    public String getBundleGroupId() { return bundleGroupId; }
    public void setBundleGroupId(String v) { this.bundleGroupId = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime v) { this.settledAt = v; }
}
