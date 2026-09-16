package com.teamflow.ai.project.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.project.dto.request.AddMeetingNotesRequest;
import com.teamflow.ai.project.dto.request.CreateMeetingRequest;
import com.teamflow.ai.project.dto.request.UpdateMeetingRequest;
import com.teamflow.ai.project.dto.response.MeetingResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MeetingService {

    MeetingResponse create(CreateMeetingRequest request, UUID organizerId);

    MeetingResponse update(UUID id, UpdateMeetingRequest request);

    MeetingResponse get(UUID id);

    PageResponse<MeetingResponse> listForProject(UUID projectId, Pageable pageable);

    MeetingResponse addNotes(UUID id, AddMeetingNotesRequest request);

    MeetingResponse cancel(UUID id);

    void delete(UUID id);
}
