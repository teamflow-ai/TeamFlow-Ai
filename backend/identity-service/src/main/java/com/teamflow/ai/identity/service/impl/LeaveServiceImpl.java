package com.teamflow.ai.identity.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.LeaveStatus;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.identity.dto.request.CreateLeaveRequest;
import com.teamflow.ai.identity.dto.request.LeaveDecisionRequest;
import com.teamflow.ai.identity.dto.response.LeaveResponse;
import com.teamflow.ai.identity.entity.Employee;
import com.teamflow.ai.identity.entity.LeaveRequest;
import com.teamflow.ai.identity.mapper.LeaveMapper;
import com.teamflow.ai.identity.messaging.EmployeeEventPublisher;
import com.teamflow.ai.identity.repository.EmployeeRepository;
import com.teamflow.ai.identity.repository.LeaveRequestRepository;
import com.teamflow.ai.identity.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private static final List<LeaveStatus> ACTIVE_STATUSES =
            List.of(LeaveStatus.PENDING, LeaveStatus.MANAGER_APPROVED, LeaveStatus.APPROVED);

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;
    private final EmployeeEventPublisher eventPublisher;

    @Override
    @Transactional
    public LeaveResponse request(UUID employeeId, CreateLeaveRequest request) {
        Employee employee = findEmployeeOrThrow(employeeId);

        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException("End date cannot be before the start date");
        }
        if (leaveRequestRepository.existsOverlapping(
                employeeId, request.startDate(), request.endDate(), ACTIVE_STATUSES)) {
            throw new BusinessException("This employee already has a leave request covering part of that period");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(request.leaveType());
        leaveRequest.setStartDate(request.startDate());
        leaveRequest.setEndDate(request.endDate());
        leaveRequest.setReason(request.reason().trim());
        
        if (employee.getManagerId() == null) {
            leaveRequest.setStatus(LeaveStatus.MANAGER_APPROVED);
        } else {
            leaveRequest.setStatus(LeaveStatus.PENDING);
        }

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Employee {} requested {} day(s) of {} leave", employeeId, saved.getTotalDays(), saved.getLeaveType());
        return leaveMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LeaveResponse managerDecision(UUID id, UUID approverId, LeaveDecisionRequest request) {
        LeaveRequest leaveRequest = findOrThrow(id);
        requireStatus(leaveRequest, LeaveStatus.PENDING);

        if (leaveRequest.getEmployee().getManagerId() == null || 
            !leaveRequest.getEmployee().getManagerId().equals(approverId)) {
            throw new BusinessException("Only the direct manager can approve the first stage of a leave request");
        }

        leaveRequest.setManagerApproverId(approverId);
        leaveRequest.setManagerApprovedAt(Instant.now());
        leaveRequest.setDecisionComment(request.comment());
        leaveRequest.setStatus(request.approve() ? LeaveStatus.MANAGER_APPROVED : LeaveStatus.REJECTED);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request {} received manager decision: {}", id, saved.getStatus());
        return leaveMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LeaveResponse hrDecision(UUID id, UUID approverId, LeaveDecisionRequest request) {
        LeaveRequest leaveRequest = findOrThrow(id);
        requireStatus(leaveRequest, LeaveStatus.MANAGER_APPROVED);

        var currentUser = com.teamflow.ai.common.security.SecurityUtils.requireCurrentUser();
        if (!currentUser.hasRole("HR") && !currentUser.hasRole("SUPER_ADMIN")) {
            throw new BusinessException("Only HR or Super Admin can approve the final stage of a leave request");
        }

        leaveRequest.setHrApproverId(approverId);
        leaveRequest.setHrApprovedAt(Instant.now());
        leaveRequest.setDecisionComment(request.comment());

        if (request.approve()) {
            leaveRequest.setStatus(LeaveStatus.APPROVED);
            Employee employee = leaveRequest.getEmployee();
            int remaining = Math.max(0, employee.getAnnualLeaveBalance() - leaveRequest.getTotalDays());
            employee.setAnnualLeaveBalance(remaining);
            employeeRepository.save(employee);
        } else {
            leaveRequest.setStatus(LeaveStatus.REJECTED);
        }

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request {} received HR decision: {}", id, saved.getStatus());
        if (saved.getStatus() == LeaveStatus.APPROVED) {
            eventPublisher.leaveApproved(saved);
        }
        return leaveMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LeaveResponse cancel(UUID id, UUID employeeId) {
        LeaveRequest leaveRequest = findOrThrow(id);
        if (!leaveRequest.getEmployee().getId().equals(employeeId)) {
            throw new BusinessException("You can only cancel your own leave requests");
        }
        if (leaveRequest.getStatus() == LeaveStatus.APPROVED
                || leaveRequest.getStatus() == LeaveStatus.REJECTED
                || leaveRequest.getStatus() == LeaveStatus.CANCELLED) {
            throw new BusinessException("A %s leave request cannot be cancelled".formatted(leaveRequest.getStatus()));
        }
        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request {} cancelled by employee {}", id, employeeId);
        return leaveMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse get(UUID id) {
        return leaveMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> listForEmployee(UUID employeeId, Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.findAllByEmployeeIdAndDeletedFalse(employeeId, pageable);
        return PageResponse.from(page, leaveMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> listPending(UUID approverId, Pageable pageable) {
        var currentUser = com.teamflow.ai.common.security.SecurityUtils.requireCurrentUser();
        Page<LeaveRequest> page;
        
        if (currentUser.hasRole("HR") || currentUser.hasRole("SUPER_ADMIN")) {
            page = leaveRequestRepository.findAllByStatusAndDeletedFalse(LeaveStatus.MANAGER_APPROVED, pageable);
        } else {
            page = leaveRequestRepository.findAllByStatusAndEmployeeManagerIdAndDeletedFalse(LeaveStatus.PENDING, approverId, pageable);
        }
        
        return PageResponse.from(page, leaveMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmployeeOnLeave(UUID employeeId, java.time.LocalDate date) {
        return leaveRequestRepository.existsOverlapping(
                employeeId, 
                date, 
                date, 
                List.of(LeaveStatus.APPROVED)
        );
    }

    // ------------------------------------------------------------------

    private LeaveRequest findOrThrow(UUID id) {
        return leaveRequestRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("LeaveRequest", id));
    }

    private Employee findEmployeeOrThrow(UUID employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Employee", employeeId));
    }

    private void requireStatus(LeaveRequest leaveRequest, LeaveStatus expected) {
        if (leaveRequest.getStatus() != expected) {
            throw new BusinessException(
                    "Leave request must be %s for this decision; it is currently %s"
                            .formatted(expected, leaveRequest.getStatus()));
        }
    }
}
