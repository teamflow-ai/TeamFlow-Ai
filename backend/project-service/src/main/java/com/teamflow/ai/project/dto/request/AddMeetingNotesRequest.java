package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Record notes for a meeting, typically after it concludes")
public record AddMeetingNotesRequest(
        @NotBlank(message = "Notes are required")
        @Size(max = 4000)
        String notes) {
}
