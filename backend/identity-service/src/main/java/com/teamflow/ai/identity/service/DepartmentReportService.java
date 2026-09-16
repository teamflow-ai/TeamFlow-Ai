package com.teamflow.ai.identity.service;

import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.identity.dto.response.DepartmentReportResponse;
import com.teamflow.ai.identity.entity.Department;
import com.teamflow.ai.identity.repository.DepartmentRepository;
import com.teamflow.ai.identity.repository.EmployeeRepository;
import com.teamflow.ai.identity.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Department Report: headcount, active/inactive split and pending leave —
 * identity-service's own data, unlike the Employee Productivity report (task
 * and worklog data), which lives in project-service instead.
 */
@Service
@RequiredArgsConstructor
public class DepartmentReportService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional(readOnly = true)
    public DepartmentReportResponse report(UUID departmentId) {
        Department department = departmentRepository.findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", departmentId));

        long total = employeeRepository.countByDepartmentIdAndDeletedFalse(departmentId);
        long active = employeeRepository.countByDepartmentIdAndActiveAndDeletedFalse(departmentId, true);
        long inactive = total - active;
        long pendingLeave = leaveRequestRepository.countPendingByDepartmentId(departmentId);

        return DepartmentReportResponse.builder()
                .departmentId(department.getId())
                .departmentName(department.getName())
                .totalEmployees(total)
                .activeEmployees(active)
                .inactiveEmployees(inactive)
                .pendingLeaveRequests(pendingLeave)
                .build();
    }
}
