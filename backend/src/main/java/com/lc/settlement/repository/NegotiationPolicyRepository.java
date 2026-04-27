package com.lc.settlement.repository;

import com.lc.settlement.entity.NegotiationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NegotiationPolicyRepository extends JpaRepository<NegotiationPolicy, Long> {
    List<NegotiationPolicy> findByLenderIdAndActiveTrue(Long lenderId);
    List<NegotiationPolicy> findByLenderId(Long lenderId);
}
