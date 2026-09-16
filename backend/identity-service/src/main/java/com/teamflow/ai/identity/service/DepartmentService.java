package com.teamflow.ai.identity.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.identity.dto.request.CreateDepartmentRequest;
import com.teamflow.ai.identity.dto.request.UpdateDepartmentRequest;
import com.teamflow.ai.identity.dto.response.DepartmentResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DepartmentService {

    DepartmentResponse create(CreateDepartmentRequest request);

    DepartmentResponse update(UUID id, UpdateDepartmentRequest request);

    DepartmentResponse get(UUID id);

    PageResponse<DepartmentResponse> list(Pageable pageable);

    void delete(UUID id);
}
