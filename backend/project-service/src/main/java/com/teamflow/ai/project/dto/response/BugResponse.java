package com.teamflow.ai.project.dto.response;

import com.teamflow.ai.common.enums.BugStatus;
import com.teamflow.ai.common.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "A reported bug")
public record BugResponse(
        UUID id,
        UUID projectId,
        UUID taskId,
        String title,
        String description,
        Priority severity,
        BugStatus status,
        UUID reportedBy,
        String reportedByName,
        UUID assigneeId,
        String assigneeName,
        String resolution,
        Instant resolvedAt) {
}
