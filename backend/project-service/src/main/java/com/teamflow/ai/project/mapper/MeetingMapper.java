package com.teamflow.ai.project.mapper;

import com.teamflow.ai.project.dto.response.MeetingParticipantResponse;
import com.teamflow.ai.project.dto.response.MeetingResponse;
import com.teamflow.ai.project.entity.Meeting;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Function;

@Component
public class MeetingMapper {

    public MeetingResponse toResponse(Meeting meeting, String organizerName, Function<UUID, String> nameResolver) {
        return MeetingResponse.builder()
                .id(meeting.getId())
                .projectId(meeting.getProject() != null ? meeting.getProject().getId() : null)
                .title(meeting.getTitle())
                .agenda(meeting.getAgenda())
                .scheduledAt(meeting.getScheduledAt())
                .durationMinutes(meeting.getDurationMinutes())
                .organizerId(meeting.getOrganizerId())
                .organizerName(organizerName)
                .status(meeting.getStatus())
                .notes(meeting.getNotes())
                .participants(meeting.getParticipantIds().stream()
                        .map(employeeId -> MeetingParticipantResponse.builder()
                                .employeeId(employeeId)
                                .employeeName(nameResolver.apply(employeeId))
                                .build())
                        .toList())
                .build();
    }
}
