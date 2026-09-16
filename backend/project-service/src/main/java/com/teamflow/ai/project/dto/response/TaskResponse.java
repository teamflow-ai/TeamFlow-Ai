package com.teamflow.ai.project.dto.response;

import com.teamflow.ai.common.enums.AssignmentMode;
import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Builder
@Schema(description = "Task")
public record TaskResponse(
        UUID id,
        UUID projectId,
        UUID sprintId,
        String title,
        String description,
        UUID assigneeId,
        String assigneeName,
        UUID reporterId,
        String reporterName,
        TaskStatus status,
        Priority priority,
        LocalDate dueDate,
        BigDecimal estimatedHours,
        BigDecimal actualHours,
        AssignmentMode assignmentMode,
        Set<String> requiredSkills,
        boolean overdue) {
}
