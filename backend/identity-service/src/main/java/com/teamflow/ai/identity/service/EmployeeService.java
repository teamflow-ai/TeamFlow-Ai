package com.teamflow.ai.identity.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.identity.dto.request.CreateEmployeeRequest;
import com.teamflow.ai.identity.dto.request.UpdateEmployeeRequest;
import com.teamflow.ai.identity.dto.response.EmployeeResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmployeeService {

    EmployeeResponse create(CreateEmployeeRequest request);

    EmployeeResponse update(UUID id, UpdateEmployeeRequest request);

    EmployeeResponse get(UUID id);

    PageResponse<EmployeeResponse> search(String query, UUID departmentId, UUID teamId, UUID managerId,
                                          Boolean active, String skill, Pageable pageable);

    EmployeeResponse setActive(UUID id, boolean active);

    void delete(UUID id);
}
