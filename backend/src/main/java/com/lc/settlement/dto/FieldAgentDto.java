package com.lc.settlement.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FieldAgentDto {
    private Long id;
    private String name;
    private String employeeCode;
    private String email;
    private String region;
    private String lenderName;
}
