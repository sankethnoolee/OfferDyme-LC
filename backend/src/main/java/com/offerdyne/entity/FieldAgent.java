package com.offerdyne.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FIELD_AGENT")
public class FieldAgent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id") private Long agentId;

    @Column(name = "lender_id")   private Long lenderId;
    @Column(name = "agent_code")  private String agentCode;
    @Column(name = "agent_name")  private String agentName;
    @Column(name = "agent_email") private String agentEmail;
    @Column(name = "agent_phone") private String agentPhone;
    @Column(name = "role")        private String role;
    @Column(name = "active")      private Boolean active;
    @Column(name = "created_at")  private LocalDateTime createdAt;

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long v) { this.agentId = v; }
    public Long getLenderId() { return lenderId; }
    public void setLenderId(Long v) { this.lenderId = v; }
    public String getAgentCode() { return agentCode; }
    public void setAgentCode(String v) { this.agentCode = v; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String v) { this.agentName = v; }
    public String getAgentEmail() { return agentEmail; }
    public void setAgentEmail(String v) { this.agentEmail = v; }
    public String getAgentPhone() { return agentPhone; }
    public void setAgentPhone(String v) { this.agentPhone = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean v) { this.active = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
