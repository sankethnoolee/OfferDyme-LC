package com.lc.settlement.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AccountSummaryDto {
    private Long id;
    private String accountNumber;
    private String productType;
    private Double outstandingAmount;
    private String dpdBucket;
    private Integer daysPastDue;
    private String status;
    private String assignedAgentName;
    private Long lenderId;
    private String lenderName;
    private String lenderCode;
    /** True when an ACCEPTED settlement already exists for this account. */
    private Boolean hasAcceptedSettlement;
}
