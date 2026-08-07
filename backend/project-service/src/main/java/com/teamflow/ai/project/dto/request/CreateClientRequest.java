package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Create a client")
public record CreateClientRequest(

        @NotBlank(message = "Client name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Client code is required")
        @Size(max = 30)
        String code,

        @Size(max = 100)
        String contactPerson,

        @Email(message = "Email must be a valid email address")
        @Size(max = 150)
        String email,

        @Size(max = 20)
        String phone,

        @Size(max = 100)
        String country) {
}
