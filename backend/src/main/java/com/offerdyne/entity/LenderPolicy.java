package com.offerdyne.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "LENDER_POLICY")
public class LenderPolicy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id") private Long policyId;

    @Column(name = "lender_id")        private Long lenderId;
    @Column(name = "product_type")     private String productType;
    @Column(name = "floor_percent")    private BigDecimal floorPercent;
    @Column(name = "ceiling_percent")  private BigDecimal ceilingPercent;
    @Column(name = "max_installments") private Integer maxInstallments;
    @Column(name = "bundling_allowed") private Boolean bundlingAllowed;
    @Column(name = "active")           private Boolean active;

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long v) { this.policyId = v; }
    public Long getLenderId() { return lenderId; }
    public void setLenderId(Long v) { this.lenderId = v; }
    public String getProductType() { return productType; }
    public void setProductType(String v) { this.productType = v; }
    public BigDecimal getFloorPercent() { return floorPercent; }
    public void setFloorPercent(BigDecimal v) { this.floorPercent = v; }
    public BigDecimal getCeilingPercent() { return ceilingPercent; }
    public void setCeilingPercent(BigDecimal v) { this.ceilingPercent = v; }
    public Integer getMaxInstallments() { return maxInstallments; }
    public void setMaxInstallments(Integer v) { this.maxInstallments = v; }
    public Boolean getBundlingAllowed() { return bundlingAllowed; }
    public void setBundlingAllowed(Boolean v) { this.bundlingAllowed = v; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean v) { this.active = v; }
}
