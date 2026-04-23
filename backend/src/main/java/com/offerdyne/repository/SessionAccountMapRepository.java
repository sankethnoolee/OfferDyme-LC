package com.offerdyne.repository;

import com.offerdyne.entity.SessionAccountMap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionAccountMapRepository extends JpaRepository<SessionAccountMap, Long> {
    List<SessionAccountMap> findBySessionId(Long sessionId);
}
