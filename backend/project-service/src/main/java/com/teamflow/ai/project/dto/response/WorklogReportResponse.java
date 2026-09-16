package com.teamflow.ai.project.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record WorklogReportResponse(
        UUID projectId,
        LocalDate from,
        LocalDate to,
        BigDecimal totalHours,
        List<Entry> entries) {

    @Builder
    public record Entry(UUID employeeId, String employeeName, UUID taskId, String taskTitle,
                         LocalDate logDate, BigDecimal hours, String notes) {
    }
}
