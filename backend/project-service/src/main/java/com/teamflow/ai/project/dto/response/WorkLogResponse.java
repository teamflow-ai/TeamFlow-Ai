package com.teamflow.ai.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Schema(description = "A single day's logged effort")
public record WorkLogResponse(
        UUID id,
        UUID employeeId,
        String employeeName,
        UUID taskId,
        String taskTitle,
        UUID projectId,
        String projectName,
        LocalDate logDate,
        BigDecimal hours,
        String notes) {
}
