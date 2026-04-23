package com.offerdyne.repository;

import com.offerdyne.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByCustomerCode(String code);
}
