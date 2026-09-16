package com.teamflow.ai.project.dto.response;

import com.teamflow.ai.common.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "A recorded status transition")
public record TaskHistoryResponse(
        UUID id,
        TaskStatus fromStatus,
        TaskStatus toStatus,
        UUID changedBy,
        String changedByName,
        Instant createdAt) {
}
