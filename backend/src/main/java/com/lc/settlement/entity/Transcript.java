package com.lc.settlement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A Transcript is the overall conversation record for one interaction
 * (call/chat) between a field agent and a customer. It is ALWAYS tied to
 * a customer, and OPTIONALLY tied to a specific account.
 *   - account != null  → conversation about one specific account
 *   - account == null  → customer-level / portfolio conversation that
 *                        covers all of the customer's accounts
 * Each individual message is stored as a ChatMessage. A transcript also
 * keeps historical snapshots via TranscriptHistory.
 */
@Entity
@Table(name = "transcript")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Transcript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Nullable: null means the conversation is portfolio-level (all accounts). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = true)
    private Account account;

    /** Convenience flag mirroring (account == null). */
    @Column(name = "portfolio_level")
    private Boolean portfolioLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_agent_id")
    private FieldAgent fieldAgent;

    /** e.g. CHAT, CALL, EMAIL, VISIT. */
    private String channel;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    /** Aggregated sentiment across the conversation: POSITIVE | NEUTRAL | NEGATIVE | MIXED. */
    private String sentiment;

    /** Sentiment score from -1.0 (very negative) to +1.0 (very positive). */
    private Double sentimentScore;

    /** A one-line summary of the conversation. */
    @Column(length = 2000)
    private String summary;

    /** Status: OPEN, CLOSED, ESCALATED. */
    private String status;

    @OneToMany(mappedBy = "transcript", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "transcript", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @OrderBy("capturedAt ASC")
    @Builder.Default
    private List<TranscriptHistory> history = new ArrayList<>();
}
