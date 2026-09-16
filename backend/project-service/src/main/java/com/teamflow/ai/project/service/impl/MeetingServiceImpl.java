package com.teamflow.ai.project.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.MeetingStatus;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.dto.request.AddMeetingNotesRequest;
import com.teamflow.ai.project.dto.request.CreateMeetingRequest;
import com.teamflow.ai.project.dto.request.UpdateMeetingRequest;
import com.teamflow.ai.project.dto.response.MeetingResponse;
import com.teamflow.ai.project.entity.Meeting;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.mapper.MeetingMapper;
import com.teamflow.ai.project.messaging.ProjectEventPublisher;
import com.teamflow.ai.project.repository.MeetingRepository;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.service.EmployeeLookupService;
import com.teamflow.ai.project.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeLookupService employeeLookupService;
    private final MeetingMapper meetingMapper;
    private final ProjectEventPublisher eventPublisher;

    @Override
    @Transactional
    public MeetingResponse create(CreateMeetingRequest request, UUID organizerId) {
        Project project = resolveProject(request.projectId());
        Set<UUID> participants = validateParticipants(request.participantIds());

        Meeting meeting = new Meeting();
        meeting.setProject(project);
        meeting.setTitle(request.title().trim());
        meeting.setAgenda(request.agenda());
        meeting.setScheduledAt(request.scheduledAt());
        meeting.setDurationMinutes(request.durationMinutes() != null ? request.durationMinutes() : 30);
        meeting.setOrganizerId(organizerId);
        meeting.setParticipantIds(participants);

        Meeting saved = meetingRepository.save(meeting);
        log.info("Scheduled meeting {} for {}", saved.getId(), saved.getScheduledAt());
        eventPublisher.meetingCreated(saved);
        for (UUID participantId : participants) {
            eventPublisher.notify(participantId, "Meeting scheduled",
                    "\"%s\" was scheduled".formatted(saved.getTitle()), "MEETING_SCHEDULED", "/meetings/" + saved.getId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MeetingResponse update(UUID id, UpdateMeetingRequest request) {
        Meeting meeting = findOrThrow(id);
        Set<UUID> participants = validateParticipants(request.participantIds());

        meeting.setTitle(request.title().trim());
        meeting.setAgenda(request.agenda());
        meeting.setScheduledAt(request.scheduledAt());
        meeting.setDurationMinutes(request.durationMinutes() != null ? request.durationMinutes() : meeting.getDurationMinutes());
        meeting.setParticipantIds(participants);

        Meeting saved = meetingRepository.save(meeting);
        log.info("Updated meeting {}", saved.getId());
        eventPublisher.meetingUpdated(saved);
        for (UUID participantId : participants) {
            eventPublisher.notify(participantId, "Meeting updated",
                    "\"%s\" was updated".formatted(saved.getTitle()), "MEETING_UPDATED", "/meetings/" + saved.getId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MeetingResponse> listForProject(UUID projectId, Pageable pageable) {
        Page<Meeting> page = meetingRepository.findAllByProjectIdAndDeletedFalse(projectId, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public MeetingResponse addNotes(UUID id, AddMeetingNotesRequest request) {
        Meeting meeting = findOrThrow(id);
        meeting.setNotes(request.notes().trim());
        if (meeting.getStatus() == MeetingStatus.SCHEDULED || meeting.getStatus() == MeetingStatus.IN_PROGRESS) {
            meeting.setStatus(MeetingStatus.COMPLETED);
        }
        Meeting saved = meetingRepository.save(meeting);
        log.info("Notes recorded for meeting {}", id);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MeetingResponse cancel(UUID id) {
        Meeting meeting = findOrThrow(id);
        if (meeting.getStatus() == MeetingStatus.COMPLETED || meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new BusinessException("A %s meeting cannot be cancelled".formatted(meeting.getStatus()));
        }
        meeting.setStatus(MeetingStatus.CANCELLED);
        Meeting saved = meetingRepository.save(meeting);
        log.info("Cancelled meeting {}", id);
        eventPublisher.meetingCancelled(saved);
        for (UUID participantId : saved.getParticipantIds()) {
            eventPublisher.notify(participantId, "Meeting cancelled",
                    "\"%s\" was cancelled".formatted(saved.getTitle()), "MEETING_CANCELLED", "/meetings/" + saved.getId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Meeting meeting = findOrThrow(id);
        meeting.setDeleted(true);
        meetingRepository.save(meeting);
        log.info("Deleted meeting {}", id);
    }

    // ------------------------------------------------------------------

    private Meeting findOrThrow(UUID id) {
        return meetingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Meeting", id));
    }

    private Project resolveProject(UUID projectId) {
        if (projectId == null) {
            return null;
        }
        return projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", projectId));
    }

    private Set<UUID> validateParticipants(Set<UUID> participantIds) {
        if (participantIds == null) {
            return new LinkedHashSet<>();
        }
        participantIds.forEach(employeeLookupService::requireExisting);
        return new LinkedHashSet<>(participantIds);
    }

    private MeetingResponse toResponse(Meeting meeting) {
        String organizerName = employeeLookupService.tryResolveName(meeting.getOrganizerId());
        return meetingMapper.toResponse(meeting, organizerName, employeeLookupService::tryResolveName);
    }
}
