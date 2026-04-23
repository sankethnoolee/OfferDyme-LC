package com.offerdyne.repository;

import com.offerdyne.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByCustomerId(Long customerId);
    List<Settlement> findByAccountId(Long accountId);
    List<Settlement> findBySessionId(Long sessionId);
    List<Settlement> findByBundleGroupId(String bundleGroupId);
}
