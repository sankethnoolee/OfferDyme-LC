package com.lc.settlement.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Bank-configured negotiation range. One row defines the FLOOR and CEILING
 * that any settlement offer on a matching (lender, product, DPD bucket)
 * combination must stay within.
 *
 * Resolution is "most specific wins":
 *   - Both product and DPD set → only matches that product + DPD
 *   - Only product set          → matches any DPD for that product
 *   - Only DPD set              → matches any product at that DPD
 *   - Both null                 → the lender's default fallback
 *
 * Ties are broken with `priority` (higher wins).
 *
 * Claude's recommendation always ends up clamped to [discountFloorPct, discountCeilingPct]
 * on the server, so a misbehaving LLM response can never violate bank policy.
 */
@Entity
@Table(name = "negotiation_policy")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NegotiationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lender_id", nullable = false)
    private Lender lender;

    /** Null = applies to all products. */
    @Column(name = "product_type")
    private String productType;

    /** Null = applies to all DPD buckets. */
    @Column(name = "dpd_bucket")
    private String dpdBucket;

    /** Minimum discount % (customer's best case is typically the ceiling, but we keep it as "the smallest discount we will accept"). */
    @Column(name = "discount_floor_pct")
    private Double discountFloorPct;

    /** Maximum discount % the bank will stretch to. */
    @Column(name = "discount_ceiling_pct")
    private Double discountCeilingPct;

    /** Minimum offer amount expressed as a %-of-outstanding (e.g., 65.0 means we must recover at least 65%). */
    @Column(name = "offer_floor_pct_of_outstanding")
    private Double offerFloorPctOfOutstanding;

    /** Maximum/ideal offer amount as a %-of-outstanding (e.g., 95.0 means ideally 95% recovered). */
    @Column(name = "offer_ceiling_pct_of_outstanding")
    private Double offerCeilingPctOfOutstanding;

    @Column(name = "min_installments")
    private Integer minInstallments;

    @Column(name = "max_installments")
    private Integer maxInstallments;

    /** Higher priority wins if multiple rows tie on specificity. */
    private Integer priority;

    private Boolean active;

    @Column(length = 500)
    private String notes;
}
