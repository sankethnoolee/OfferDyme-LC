package com.lc.settlement.repository;

import com.lc.settlement.entity.FieldAgent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FieldAgentRepository extends JpaRepository<FieldAgent, Long> {
    Optional<FieldAgent> findByEmployeeCode(String employeeCode);
}
