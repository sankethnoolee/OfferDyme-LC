package com.offerdyne.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LENDER")
public class Lender {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lender_id") private Long lenderId;

    @Column(name = "lender_name")     private String lenderName;
    @Column(name = "lender_code")     private String lenderCode;
    @Column(name = "floor_percent")   private BigDecimal floorPercent;
    @Column(name = "ceiling_percent") private BigDecimal ceilingPercent;
    @Column(name = "max_installments")private Integer maxInstallments;
    @Column(name = "min_installment_amt") private BigDecimal minInstallmentAmt;
    @Column(name = "bundling_allowed")private Boolean bundlingAllowed;
    @Column(name = "active")          private Boolean active;
    @Column(name = "created_at")      private LocalDateTime createdAt;

    public Long getLenderId() { return lenderId; }
    public void setLenderId(Long v) { this.lenderId = v; }
    public String getLenderName() { return lenderName; }
    public void setLenderName(String v) { this.lenderName = v; }
    public String getLenderCode() { return lenderCode; }
    public void setLenderCode(String v) { this.lenderCode = v; }
    public BigDecimal getFloorPercent() { return floorPercent; }
    public void setFloorPercent(BigDecimal v) { this.floorPercent = v; }
    public BigDecimal getCeilingPercent() { return ceilingPercent; }
    public void setCeilingPercent(BigDecimal v) { this.ceilingPercent = v; }
    public Integer getMaxInstallments() { return maxInstallments; }
    public void setMaxInstallments(Integer v) { this.maxInstallments = v; }
    public BigDecimal getMinInstallmentAmt() { return minInstallmentAmt; }
    public void setMinInstallmentAmt(BigDecimal v) { this.minInstallmentAmt = v; }
    public Boolean getBundlingAllowed() { return bundlingAllowed; }
    public void setBundlingAllowed(Boolean v) { this.bundlingAllowed = v; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean v) { this.active = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
