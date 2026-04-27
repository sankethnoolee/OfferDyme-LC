package com.lc.settlement.repository;

import com.lc.settlement.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
    List<Transcript> findByAccountIdOrderByStartedAtDesc(Long accountId);
    List<Transcript> findByCustomerIdOrderByStartedAtDesc(Long customerId);
    Optional<Transcript> findFirstByAccountIdAndStatusOrderByStartedAtDesc(Long accountId, String status);

    /** Latest OPEN portfolio-level transcript (account is null) for a customer. */
    Optional<Transcript> findFirstByCustomerIdAndAccountIsNullAndStatusOrderByStartedAtDesc(Long customerId, String status);

    /** All portfolio-level transcripts for a customer. */
    List<Transcript> findByCustomerIdAndAccountIsNullOrderByStartedAtDesc(Long customerId);
}
