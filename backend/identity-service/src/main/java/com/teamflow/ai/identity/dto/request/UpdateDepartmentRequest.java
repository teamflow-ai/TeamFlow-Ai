package com.teamflow.ai.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Update a department")
public record UpdateDepartmentRequest(

        @NotBlank(message = "Department name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        UUID headEmployeeId,

        BigDecimal annualBudget) {
}
