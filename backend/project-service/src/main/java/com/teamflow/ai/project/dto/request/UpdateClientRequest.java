package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Update a client")
public record UpdateClientRequest(

        @NotBlank(message = "Client name is required")
        @Size(max = 150)
        String name,

        @Size(max = 100)
        String contactPerson,

        @Email(message = "Email must be a valid email address")
        @Size(max = 150)
        String email,

        @Size(max = 20)
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid (e.g. +14155552671 or 1234567890)")
        String phone,

        @Size(max = 100)
        String country,

        boolean active) {
}
