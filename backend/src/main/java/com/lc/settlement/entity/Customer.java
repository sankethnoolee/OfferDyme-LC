package com.lc.settlement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true)
    private String panNumber;

    private String aadhaarMasked;

    private String email;

    private String phone;

    private String address;

    private String city;

    private String state;

    private String pincode;

    private LocalDate dateOfBirth;

    /** Annual income in INR for risk context. */
    private Double annualIncome;

    /** A customer can have multiple accounts. */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Account> accounts = new ArrayList<>();
}
