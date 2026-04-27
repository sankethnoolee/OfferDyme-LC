package com.lc.settlement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable snapshot of a transcript's state at a point in time
 * (e.g. after each message, or each time sentiment/summary is recomputed).
 * Gives a reliable audit trail of how the conversation and its analysis evolved.
 */
@Entity
@Table(name = "transcript_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TranscriptHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", nullable = false)
    @JsonIgnore
    private Transcript transcript;

    /** Full transcript text snapshot at this moment. */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String fullText;

    /** Aggregated sentiment at this point. */
    private String sentiment;

    private Double sentimentScore;

    @Column(length = 2000)
    private String summary;

    /** What event caused this snapshot — MESSAGE_ADDED, SETTLEMENT_PROPOSED, CLOSED, etc. */
    private String changeReason;

    @Column(nullable = false)
    private LocalDateTime capturedAt;

    @PrePersist
    void onCreate() {
        if (capturedAt == null) capturedAt = LocalDateTime.now();
    }
}
