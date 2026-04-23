package com.offerdyne.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request / response DTOs. Lombok-generated getters, setters, equals,
 * hashCode, toString, and no-arg / all-args constructors.
 */
public class ApiDtos {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class StartSessionRequest {
        private Long customerId;
        private Long accountId;
        private Boolean attemptBundle;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SessionResponse {
        private Long sessionId;
        private Long customerId;
        private Long accountId;
        private Long agentId;
        private String status;
        private BigDecimal initialOfferPercent;
        private BigDecimal currentOfferPercent;
        private Integer turnCount;
        private Boolean bundleFlag;
        private List<Long> bundledAccountIds;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TurnRequest {
        private String borrowerUtterance;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TurnResponse {
        private Long sessionId;
        private Integer turnIndex;
        private String borrowerUtterance;
        private String sentiment;
        private BigDecimal sentimentScore;
        private String objectionType;
        private String strategyChosen;
        private BigDecimal offerPercent;
        private BigDecimal offerAmount;
        private String framingText;
        private String installmentPlanJson;
        private Boolean guardrailPassed;
        private String guardrailReason;
        private Boolean terminal;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TranscriptDto {
        private Long transcriptId;
        private Long sessionId;
        private Long customerId;
        private Long accountId;
        private Integer turnIndex;
        private String speaker;
        private String utterance;
        private String sentiment;
        private BigDecimal sentimentScore;
        private String objectionType;
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class AcceptRequest {
        private Long offerId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SettlementDto {
        private Long settlementId;
        private Long sessionId;
        private Long customerId;
        private Long accountId;
        private BigDecimal settledPercent;
        private BigDecimal settledAmount;
        private String settlementType;
        private Integer installmentCount;
        private String bundleGroupId;
        private String status;
        private LocalDateTime settledAt;
    }
}
