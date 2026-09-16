package com.teamflow.ai.project.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TaskDependencyResponse(
        UUID id,
        UUID taskId,
        UUID dependsOnTaskId,
        String dependsOnTaskTitle,
        String dependsOnTaskStatus
) {
}
