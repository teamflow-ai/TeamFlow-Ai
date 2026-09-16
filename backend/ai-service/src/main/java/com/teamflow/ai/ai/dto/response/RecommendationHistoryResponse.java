package com.teamflow.ai.ai.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record RecommendationHistoryResponse(
        String id,
        String taskId,
        String aiProvider,
        Map<String, Object> promptMetadata,
        List<String> recommendedEmployeeIds,
        String topRecommendationEmployeeId,
        Boolean accepted,
        Double estimatedHours,
        Double actualHours,
        Double predictionAccuracy,
        Instant timestamp) {
}
