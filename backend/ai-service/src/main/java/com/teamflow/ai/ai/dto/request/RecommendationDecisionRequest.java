package com.teamflow.ai.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Record whether a manager accepted the top recommendation, and optionally the actual hours once known")
public record RecommendationDecisionRequest(
        @NotNull(message = "accepted is required")
        Boolean accepted,

        Double actualHours) {
}
