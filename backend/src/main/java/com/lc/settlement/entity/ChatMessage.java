package com.lc.settlement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One line in a transcript — either the agent or the customer speaking.
 * Carries per-message sentiment so we can track how the conversation evolves.
 */
@Entity
@Table(name = "chat_message")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", nullable = false)
    @JsonIgnore
    private Transcript transcript;

    /** AGENT | CUSTOMER | SYSTEM */
    @Column(nullable = false)
    private String senderType;

    /** Display name of the sender. */
    private String senderName;

    @Column(length = 4000, nullable = false)
    private String content;

    /** POSITIVE | NEUTRAL | NEGATIVE */
    private String sentiment;

    private Double sentimentScore;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
