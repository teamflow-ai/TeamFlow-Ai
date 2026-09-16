package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Update a work log entry")
public record UpdateWorkLogRequest(

        @NotNull(message = "Log date is required")
        LocalDate logDate,

        @NotNull(message = "Hours is required")
        @DecimalMin(value = "0.01", message = "Hours must be greater than zero")
        @DecimalMax(value = "24.0", message = "Hours cannot exceed 24 in a single day")
        BigDecimal hours,

        @Size(max = 1000)
        String notes) {
}
