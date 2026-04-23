package com.offerdyne.repository;

import com.offerdyne.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findBySessionIdOrderByMadeAtAsc(Long sessionId);
    long countByGuardrailCheckPassed(Boolean passed);
}
