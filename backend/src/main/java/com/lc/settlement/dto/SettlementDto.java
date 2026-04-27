package com.lc.settlement.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SettlementDto {
    private Long id;
    private Long customerId;
    private Long accountId;
    private String accountNumber;
    private String customerName;
    private Double outstandingAtOffer;
    private Double offeredAmount;
    private Double discountPercent;
    private String paymentPlan;
    private Integer numberOfInstallments;
    private LocalDate proposedPaymentDate;
    private String status;
    private String source;
    private String strategyCode;
    private String rationale;
    private String customerResponse;
    private String proposedByAgentName;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
    private Long lenderId;
    private String lenderName;
    /** Every account this settlement covers — a single entry for normal offers, multiple for BUNDLE. */
    @Builder.Default
    private List<LinkedAccount> linkedAccounts = java.util.Collections.emptyList();

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class LinkedAccount {
        private Long id;
        private String accountNumber;
        private String productType;
        private Double outstandingAmount;
        private String dpdBucket;
    }
}
