package com.lc.settlement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field_agent")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FieldAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String employeeCode;

    private String email;

    private String phone;

    private String region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id")
    private Lender lender;
}
