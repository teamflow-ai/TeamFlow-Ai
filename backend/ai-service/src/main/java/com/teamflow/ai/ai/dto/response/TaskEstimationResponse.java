package com.teamflow.ai.ai.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TaskEstimationResponse(
        double estimatedHours,
        LocalDate estimatedStartDate,
        LocalDate estimatedCompletionDate,
        String complexityRating,
        String risk,
        int confidencePercent,
        String reasoning) {
}
