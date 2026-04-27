package com.lc.settlement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lender")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Lender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String code;

    private String contactEmail;

    private String contactPhone;

    private String address;
}
