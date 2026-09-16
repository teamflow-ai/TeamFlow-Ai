package com.teamflow.ai.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Schema(description = "Department")
public record DepartmentResponse(

        UUID id,
        String name,
        String code,
        String description,
        UUID headEmployeeId,
        String headEmployeeName,
        BigDecimal annualBudget,
        long employeeCount) {
}
