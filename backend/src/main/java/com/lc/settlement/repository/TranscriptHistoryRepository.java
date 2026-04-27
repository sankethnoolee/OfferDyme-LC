package com.lc.settlement.repository;

import com.lc.settlement.entity.TranscriptHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranscriptHistoryRepository extends JpaRepository<TranscriptHistory, Long> {
    List<TranscriptHistory> findByTranscriptIdOrderByCapturedAtAsc(Long transcriptId);
}
