package com.offerdyne.repository;

import com.offerdyne.entity.NegotiationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NegotiationSessionRepository extends JpaRepository<NegotiationSession, Long> {
    List<NegotiationSession> findByAgentIdOrderByStartedAtDesc(Long agentId);
    List<NegotiationSession> findByCustomerIdOrderByStartedAtDesc(Long customerId);
}
