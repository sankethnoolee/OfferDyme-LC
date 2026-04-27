package com.lc.settlement.repository;

import com.lc.settlement.entity.NegotiationStrategy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NegotiationStrategyRepository extends JpaRepository<NegotiationStrategy, Long> {
    List<NegotiationStrategy> findByLenderIdAndActiveTrueOrderByPriorityAsc(Long lenderId);
    List<NegotiationStrategy> findByLenderIdOrderByPriorityAsc(Long lenderId);
}
