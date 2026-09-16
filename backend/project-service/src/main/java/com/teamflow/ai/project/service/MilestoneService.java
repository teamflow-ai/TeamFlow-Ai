package com.teamflow.ai.project.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.project.approval.dto.ApprovalResponse;
import com.teamflow.ai.project.dto.request.CreateMilestoneRequest;
import com.teamflow.ai.project.dto.request.UpdateMilestoneRequest;
import com.teamflow.ai.project.dto.response.MilestoneResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MilestoneService {

    MilestoneResponse create(CreateMilestoneRequest request);

    MilestoneResponse update(UUID id, UpdateMilestoneRequest request);

    MilestoneResponse get(UUID id);

    PageResponse<MilestoneResponse> listForProject(UUID projectId, Pageable pageable);

    /** PM marks the milestone ready; opens a MILESTONE approval request for an admin. */
    ApprovalResponse submitForReview(UUID id, UUID requestedBy, String remarks);

    void delete(UUID id);
}
