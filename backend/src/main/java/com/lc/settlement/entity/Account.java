package com.lc.settlement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    /** e.g. PERSONAL_LOAN, CREDIT_CARD, HOME_LOAN, AUTO_LOAN. */
    private String productType;

    private Double sanctionedAmount;

    private Double outstandingAmount;

    private Double principalOutstanding;

    private Double interestOutstanding;

    private Double penaltyAmount;

    /** Overdue bucket label, e.g. DPD_30, DPD_60, DPD_90, NPA. */
    private String dpdBucket;

    private Integer daysPastDue;

    private LocalDate lastPaymentDate;

    private Double lastPaymentAmount;

    private LocalDate sanctionDate;

    private Double interestRate;

    /** Status: ACTIVE, DELINQUENT, SETTLED, CLOSED. */
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Lender this account belongs to — drives the guardrail / policy lookup. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lender_id")
    private Lender lender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private FieldAgent assignedAgent;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Transcript> transcripts = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Settlement> settlements = new ArrayList<>();
}
