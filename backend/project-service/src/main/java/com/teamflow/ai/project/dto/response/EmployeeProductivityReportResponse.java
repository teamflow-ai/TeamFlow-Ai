package com.teamflow.ai.project.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record EmployeeProductivityReportResponse(
        UUID employeeId,
        String employeeName,
        LocalDate from,
        LocalDate to,
        long tasksCompleted,
        BigDecimal totalHoursLogged,
        double averageHoursPerDay) {
}
