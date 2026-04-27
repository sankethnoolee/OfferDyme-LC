package com.lc.settlement.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AccountDetailDto {
    private Long id;
    private String accountNumber;
    private String productType;
    private Double sanctionedAmount;
    private Double outstandingAmount;
    private Double principalOutstanding;
    private Double interestOutstanding;
    private Double penaltyAmount;
    private String dpdBucket;
    private Integer daysPastDue;
    private LocalDate lastPaymentDate;
    private Double lastPaymentAmount;
    private LocalDate sanctionDate;
    private Double interestRate;
    private String status;
    private Long customerId;
    private String customerName;
    private String assignedAgentName;
    private Long assignedAgentId;
    private Long lenderId;
    private String lenderName;
    private String lenderCode;
    /** True when an ACCEPTED settlement already exists for this account. */
    private Boolean hasAcceptedSettlement;
}
