package com.teamflow.ai.project.dto.response;

import com.teamflow.ai.common.enums.MilestoneStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record MilestoneResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        LocalDate dueDate,
        MilestoneStatus status) {
}
