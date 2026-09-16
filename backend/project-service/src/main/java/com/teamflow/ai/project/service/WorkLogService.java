package com.teamflow.ai.project.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.project.dto.request.CreateWorkLogRequest;
import com.teamflow.ai.project.dto.request.UpdateWorkLogRequest;
import com.teamflow.ai.project.dto.response.WorkLogResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WorkLogService {

    WorkLogResponse create(CreateWorkLogRequest request, UUID employeeId);

    WorkLogResponse update(UUID id, UpdateWorkLogRequest request, UUID employeeId);

    WorkLogResponse get(UUID id);

    PageResponse<WorkLogResponse> listForEmployee(UUID employeeId, Pageable pageable);

    PageResponse<WorkLogResponse> listForProject(UUID projectId, Pageable pageable);

    PageResponse<WorkLogResponse> listForTask(UUID taskId, Pageable pageable);

    void delete(UUID id, UUID employeeId);
}
