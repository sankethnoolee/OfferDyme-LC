package com.offerdyne.repository;

import com.offerdyne.entity.FieldAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FieldAgentRepository extends JpaRepository<FieldAgent, Long> {
    List<FieldAgent> findByLenderIdAndActiveTrue(Long lenderId);
}
