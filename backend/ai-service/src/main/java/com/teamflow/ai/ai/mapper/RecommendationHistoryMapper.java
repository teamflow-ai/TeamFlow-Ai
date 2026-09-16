package com.teamflow.ai.ai.mapper;

import com.teamflow.ai.ai.document.RecommendationHistory;
import com.teamflow.ai.ai.dto.response.RecommendationHistoryResponse;
import org.springframework.stereotype.Component;

@Component
public class RecommendationHistoryMapper {

    public RecommendationHistoryResponse toResponse(RecommendationHistory history) {
        return RecommendationHistoryResponse.builder()
                .id(history.getId())
                .taskId(history.getTaskId())
                .aiProvider(history.getAiProvider())
                .promptMetadata(history.getPromptMetadata())
                .recommendedEmployeeIds(history.getRecommendedEmployeeIds())
                .topRecommendationEmployeeId(history.getTopRecommendationEmployeeId())
                .accepted(history.getAccepted())
                .estimatedHours(history.getEstimatedHours())
                .actualHours(history.getActualHours())
                .predictionAccuracy(history.getPredictionAccuracy())
                .timestamp(history.getTimestamp())
                .build();
    }
}
