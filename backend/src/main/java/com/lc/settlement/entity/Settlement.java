package com.lc.settlement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "settlement")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * All accounts covered by this settlement proposal.
     * For single-account offers this is just [account]; for BUNDLE strategy
     * it's every customer account of the same lender being bundled.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "settlement_accounts",
            joinColumns = @JoinColumn(name = "settlement_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    @JsonIgnore
    @Builder.Default
    private List<Account> linkedAccounts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id")
    private Transcript transcript;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by_agent_id")
    private FieldAgent proposedByAgent;

    private Double outstandingAtOffer;

    private Double offeredAmount;

    private Double discountPercent;

    /** ONE_TIME | EMI_3 | EMI_6 | EMI_12 | CUSTOM */
    private String paymentPlan;

    private Integer numberOfInstallments;

    private LocalDate proposedPaymentDate;

    /** PROPOSED | ACCEPTED | REJECTED | COUNTERED | PAID | EXPIRED */
    private String status;

    /** Who proposed it — AGENT or CLAUDE_AI. */
    private String source;

    /** Strategy code picked at the time of the offer (HOLD, LOWER, REFRAME_INSTALLMENTS, BUNDLE, …). */
    private String strategyCode;

    @Column(length = 2000)
    private String rationale;

    @Column(length = 2000)
    private String customerResponse;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime decidedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "PROPOSED";
    }
}
