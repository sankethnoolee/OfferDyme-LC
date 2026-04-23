package com.offerdyne.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CUSTOMER")
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id") private Long customerId;

    @Column(name = "customer_code")     private String customerCode;
    @Column(name = "customer_name")     private String customerName;
    @Column(name = "phone")             private String phone;
    @Column(name = "email")             private String email;
    @Column(name = "employment_status") private String employmentStatus;
    @Column(name = "income_band")       private String incomeBand;
    @Column(name = "credit_score")      private Integer creditScore;
    @Column(name = "risk_segment")      private String riskSegment;
    @Column(name = "created_at")        private LocalDateTime createdAt;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { this.customerId = v; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String v) { this.customerCode = v; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { this.customerName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String v) { this.employmentStatus = v; }
    public String getIncomeBand() { return incomeBand; }
    public void setIncomeBand(String v) { this.incomeBand = v; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer v) { this.creditScore = v; }
    public String getRiskSegment() { return riskSegment; }
    public void setRiskSegment(String v) { this.riskSegment = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
