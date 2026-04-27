package com.lc.settlement.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;
    private String senderType;
    private String senderName;
    private String content;
    private String sentiment;
    private Double sentimentScore;
    private LocalDateTime createdAt;
}
