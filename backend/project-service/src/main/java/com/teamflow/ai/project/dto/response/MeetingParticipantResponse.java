package com.teamflow.ai.project.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record MeetingParticipantResponse(UUID employeeId, String employeeName) {
}
