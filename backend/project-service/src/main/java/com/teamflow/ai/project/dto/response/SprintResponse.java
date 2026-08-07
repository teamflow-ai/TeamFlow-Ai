package com.teamflow.ai.project.dto.response;

import com.teamflow.ai.common.enums.SprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Schema(description = "Sprint")
public record SprintResponse(
        UUID id,
        UUID projectId,
        String name,
        String goal,
        SprintStatus status,
        LocalDate startDate,
        LocalDate endDate,
        long taskCount,
        long completedTaskCount) {
}
