package com.teamflow.ai.project.dto.response;

import com.teamflow.ai.common.enums.MeetingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "A scheduled meeting")
public record MeetingResponse(
        UUID id,
        UUID projectId,
        String title,
        String agenda,
        Instant scheduledAt,
        int durationMinutes,
        UUID organizerId,
        String organizerName,
        MeetingStatus status,
        String notes,
        List<MeetingParticipantResponse> participants) {
}
