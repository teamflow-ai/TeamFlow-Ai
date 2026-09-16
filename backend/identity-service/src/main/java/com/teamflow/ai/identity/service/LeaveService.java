package com.teamflow.ai.identity.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.identity.dto.request.CreateLeaveRequest;
import com.teamflow.ai.identity.dto.request.LeaveDecisionRequest;
import com.teamflow.ai.identity.dto.response.LeaveResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LeaveService {

    LeaveResponse request(UUID employeeId, CreateLeaveRequest request);

    LeaveResponse managerDecision(UUID id, UUID approverId, LeaveDecisionRequest request);

    LeaveResponse hrDecision(UUID id, UUID approverId, LeaveDecisionRequest request);

    LeaveResponse cancel(UUID id, UUID employeeId);

    LeaveResponse get(UUID id);

    PageResponse<LeaveResponse> listForEmployee(UUID employeeId, Pageable pageable);

    PageResponse<LeaveResponse> listPending(UUID approverId, Pageable pageable);

    boolean isEmployeeOnLeave(UUID employeeId, java.time.LocalDate date);
}
