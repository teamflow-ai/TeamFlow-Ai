package com.teamflow.ai.project.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.ProjectStatus;
import com.teamflow.ai.project.approval.dto.ApprovalResponse;
import com.teamflow.ai.project.dto.request.AddProjectMemberRequest;
import com.teamflow.ai.project.dto.request.CreateProjectRequest;
import com.teamflow.ai.project.dto.request.UpdateProjectRequest;
import com.teamflow.ai.project.dto.request.UpdateProjectStatusRequest;
import com.teamflow.ai.project.dto.response.ProjectResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectService {

    ProjectResponse create(CreateProjectRequest request);

    ProjectResponse update(UUID id, UpdateProjectRequest request);

    ProjectResponse get(UUID id);

    PageResponse<ProjectResponse> search(String query, ProjectStatus status, UUID managerId, Pageable pageable);

    ProjectResponse updateStatus(UUID id, UpdateProjectStatusRequest request);

    ProjectResponse addMember(UUID id, AddProjectMemberRequest request);

    ProjectResponse removeMember(UUID id, UUID employeeId);

    /** Opens a PROJECT_CLOSURE approval request; an admin decides via {@code ApprovalController}. */
    ApprovalResponse requestClosure(UUID id, UUID requestedBy, String remarks);

    void delete(UUID id);
}
