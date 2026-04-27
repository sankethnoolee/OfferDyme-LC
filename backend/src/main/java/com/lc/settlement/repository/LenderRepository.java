package com.lc.settlement.repository;

import com.lc.settlement.entity.Lender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LenderRepository extends JpaRepository<Lender, Long> {
}
