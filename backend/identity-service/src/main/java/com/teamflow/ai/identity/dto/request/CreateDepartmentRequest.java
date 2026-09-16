package com.teamflow.ai.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Create a department")
public record CreateDepartmentRequest(

        @NotBlank(message = "Department name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Department code is required")
        @Size(max = 20)
        String code,

        @Size(max = 500)
        String description,

        UUID headEmployeeId,

        BigDecimal annualBudget) {
}
