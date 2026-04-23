package com.offerdyne.repository;

import com.offerdyne.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
    List<Transcript> findBySessionIdOrderByTurnIndexAsc(Long sessionId);
    List<Transcript> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Transcript> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
