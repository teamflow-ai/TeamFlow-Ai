package com.teamflow.ai.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "One ranked, explainable assignment candidate")
public record TaskAssignmentRecommendationResponse(
        UUID employeeId,
        String employeeName,
        double workloadScore,
        List<String> reasons) {
}
