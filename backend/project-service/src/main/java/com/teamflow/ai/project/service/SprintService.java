package com.teamflow.ai.project.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.project.dto.request.CreateSprintRequest;
import com.teamflow.ai.project.dto.request.UpdateSprintRequest;
import com.teamflow.ai.project.dto.request.UpdateSprintStatusRequest;
import com.teamflow.ai.project.dto.response.SprintResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SprintService {

    SprintResponse create(CreateSprintRequest request);

    SprintResponse update(UUID id, UpdateSprintRequest request);

    SprintResponse get(UUID id);

    PageResponse<SprintResponse> listForProject(UUID projectId, Pageable pageable);

    SprintResponse updateStatus(UUID id, UpdateSprintStatusRequest request);

    com.teamflow.ai.project.dto.response.SprintCapacityReport getCapacityReport(UUID id);

    void delete(UUID id);
}
