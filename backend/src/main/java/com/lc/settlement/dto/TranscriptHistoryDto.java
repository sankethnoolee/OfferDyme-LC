package com.lc.settlement.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TranscriptHistoryDto {
    private Long id;
    private String fullText;
    private String sentiment;
    private Double sentimentScore;
    private String summary;
    private String changeReason;
    private LocalDateTime capturedAt;
}
