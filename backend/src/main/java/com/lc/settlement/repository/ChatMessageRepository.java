package com.lc.settlement.repository;

import com.lc.settlement.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByTranscriptIdOrderByCreatedAtAsc(Long transcriptId);
}
