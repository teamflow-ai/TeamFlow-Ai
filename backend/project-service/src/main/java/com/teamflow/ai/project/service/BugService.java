package com.teamflow.ai.project.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.project.dto.request.AssignBugRequest;
import com.teamflow.ai.project.dto.request.CreateBugRequest;
import com.teamflow.ai.project.dto.request.UpdateBugRequest;
import com.teamflow.ai.project.dto.request.UpdateBugStatusRequest;
import com.teamflow.ai.project.dto.response.BugResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BugService {

    BugResponse create(CreateBugRequest request, UUID reportedBy);

    BugResponse update(UUID id, UpdateBugRequest request);

    BugResponse get(UUID id);

    PageResponse<BugResponse> listForProject(UUID projectId, Pageable pageable);

    BugResponse assign(UUID id, AssignBugRequest request);

    BugResponse updateStatus(UUID id, UpdateBugStatusRequest request);

    void delete(UUID id);
}
