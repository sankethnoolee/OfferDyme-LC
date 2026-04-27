package com.lc.settlement.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LenderDto {
    private Long id;
    private String name;
    private String code;
    private String contactEmail;
    private String contactPhone;
    private String address;
}
