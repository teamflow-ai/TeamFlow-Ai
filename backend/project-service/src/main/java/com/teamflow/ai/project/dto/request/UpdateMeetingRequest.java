package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Update a scheduled meeting")
public record UpdateMeetingRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String agenda,

        @NotNull(message = "Scheduled time is required")
        Instant scheduledAt,

        @Min(5) @Max(480)
        Integer durationMinutes,

        Set<UUID> participantIds) {
}
