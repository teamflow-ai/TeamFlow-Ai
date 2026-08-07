package com.teamflow.ai.identity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Update an existing employee's profile")
public record UpdateEmployeeRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 50)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50)
        String lastName,

        @NotBlank(message = "Work email is required")
        @Email(message = "Work email must be a valid email address")
        @Size(max = 150)
        String workEmail,

        @Size(max = 20)
        String phone,

        @Size(max = 100)
        String designation,

        LocalDate dateOfJoining,

        LocalDate dateOfBirth,

        UUID departmentId,

        UUID teamId,

        UUID managerId,

        Set<String> skills,

        @Min(1) @Max(80)
        Integer weeklyCapacityHours,

        @Min(0)
        Integer yearsOfExperience) {
}
