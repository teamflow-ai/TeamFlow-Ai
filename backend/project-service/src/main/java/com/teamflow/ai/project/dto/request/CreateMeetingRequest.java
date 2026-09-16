package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Schedule a meeting")
public record CreateMeetingRequest(

        UUID projectId,

        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String agenda,

        @NotNull(message = "Scheduled time is required")
        @FutureOrPresent(message = "Scheduled time cannot be in the past")
        Instant scheduledAt,

        @Min(5) @Max(480)
        Integer durationMinutes,

        Set<UUID> participantIds) {
}
