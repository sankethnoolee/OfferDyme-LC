package com.offerdyne.repository;

import com.offerdyne.entity.Lender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LenderRepository extends JpaRepository<Lender, Long> {
    Lender findByLenderCode(String code);
}
