package com.lc.settlement.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TranscriptDto {
    private Long id;
    private Long customerId;
    /** null when the transcript is portfolio-level. */
    private Long accountId;
    private Boolean portfolioLevel;
    private String channel;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String sentiment;
    private Double sentimentScore;
    private String summary;
    private String status;
    private String fieldAgentName;
    private List<ChatMessageDto> messages;
}
