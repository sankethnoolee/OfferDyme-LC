package com.offerdyne.repository;

import com.offerdyne.entity.LenderPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LenderPolicyRepository extends JpaRepository<LenderPolicy, Long> {
    List<LenderPolicy> findByLenderIdAndActiveTrue(Long lenderId);
    LenderPolicy findByLenderIdAndProductType(Long lenderId, String productType);
}
