package com.teamflow.ai.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "A task that could be moved off an overloaded employee, with the best alternative assignee")
public record ReassignmentSuggestionResponse(
        UUID taskId,
        String taskTitle,
        TaskAssignmentRecommendationResponse suggestedAssignee) {
}
