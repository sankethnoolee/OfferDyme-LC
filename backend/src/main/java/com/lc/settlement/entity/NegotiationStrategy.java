package com.lc.settlement.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * One negotiation "play" the agent can use — e.g., HOLD, LOWER,
 * REFRAME_INSTALLMENTS, BUNDLE. These are provided to Claude at prompt
 * time so it can pick exactly one per turn and explain why.
 *
 * Add/edit rows in the DB to extend the menu — no code changes needed.
 */
@Entity
@Table(name = "negotiation_strategy")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NegotiationStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lender_id", nullable = false)
    private Lender lender;

    /** Stable code used in Claude prompts & decisions (HOLD, LOWER, REFRAME_INSTALLMENTS, BUNDLE, …). */
    @Column(nullable = false)
    private String code;

    /** Display name shown in the UI. */
    private String name;

    /** When this strategy should be picked. */
    @Column(name = "when_applied", length = 1000)
    private String whenApplied;

    /** What the strategy actually does. */
    @Column(name = "action_template", length = 1500)
    private String actionTemplate;

    /** Sort order in the strategy list. */
    private Integer priority;

    private Boolean active;
}
