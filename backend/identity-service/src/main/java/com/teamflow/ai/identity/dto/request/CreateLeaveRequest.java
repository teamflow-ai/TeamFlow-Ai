package com.teamflow.ai.identity.dto.request;

import com.teamflow.ai.identity.entity.LeaveRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request time off")
public record CreateLeaveRequest(

        @NotNull(message = "Leave type is required")
        LeaveRequest.LeaveType leaveType,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date cannot be in the past")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotBlank(message = "A reason is required")
        @Size(max = 500)
        String reason) {
}
