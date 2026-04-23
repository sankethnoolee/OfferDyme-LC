package com.offerdyne.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SESSION_ACCOUNT_MAP")
public class SessionAccountMap {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id") private Long mapId;

    @Column(name = "session_id") private Long sessionId;
    @Column(name = "account_id") private Long accountId;
    @Column(name = "added_at")   private LocalDateTime addedAt;

    public Long getMapId() { return mapId; }
    public void setMapId(Long v) { this.mapId = v; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long v) { this.sessionId = v; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { this.accountId = v; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime v) { this.addedAt = v; }
}
