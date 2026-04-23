package com.offerdyne.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ACCOUNT")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id") private Long accountId;

    @Column(name = "account_number")      private String accountNumber;
    @Column(name = "customer_id")         private Long customerId;
    @Column(name = "lender_id")           private Long lenderId;
    @Column(name = "product_type")        private String productType;
    @Column(name = "principal_amount")    private BigDecimal principalAmount;
    @Column(name = "outstanding_amount")  private BigDecimal outstandingAmount;
    @Column(name = "dpd")                 private Integer dpd;
    @Column(name = "account_status")      private String accountStatus;
    @Column(name = "opened_at")           private LocalDateTime openedAt;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { this.accountId = v; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String v) { this.accountNumber = v; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { this.customerId = v; }
    public Long getLenderId() { return lenderId; }
    public void setLenderId(Long v) { this.lenderId = v; }
    public String getProductType() { return productType; }
    public void setProductType(String v) { this.productType = v; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal v) { this.principalAmount = v; }
    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal v) { this.outstandingAmount = v; }
    public Integer getDpd() { return dpd; }
    public void setDpd(Integer v) { this.dpd = v; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String v) { this.accountStatus = v; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime v) { this.openedAt = v; }
}
