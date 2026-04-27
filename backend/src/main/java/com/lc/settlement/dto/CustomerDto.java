package com.lc.settlement.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CustomerDto {
    private Long id;
    private String fullName;
    private String panNumber;
    private String email;
    private String phone;
    private String city;
    private String state;
    private Double annualIncome;
    private Integer accountCount;
    private Double totalOutstanding;
    private List<AccountSummaryDto> accounts;
    /** When a lender filter was applied, echoes that lenderId here; null otherwise. */
    private Long filteredLenderId;
}
