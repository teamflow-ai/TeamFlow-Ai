package com.teamflow.ai.identity.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.DuplicateResourceException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.identity.dto.request.CreateDepartmentRequest;
import com.teamflow.ai.identity.dto.request.UpdateDepartmentRequest;
import com.teamflow.ai.identity.dto.response.DepartmentResponse;
import com.teamflow.ai.identity.entity.Department;
import com.teamflow.ai.identity.entity.Employee;
import com.teamflow.ai.identity.mapper.DepartmentMapper;
import com.teamflow.ai.identity.repository.DepartmentRepository;
import com.teamflow.ai.identity.repository.EmployeeRepository;
import com.teamflow.ai.identity.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        String code = request.code().trim().toUpperCase();
        if (departmentRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
            throw DuplicateResourceException.of("Department", "code", code);
        }

        Department department = new Department();
        department.setName(request.name().trim());
        department.setCode(code);
        department.setDescription(request.description());
        department.setHeadEmployeeId(resolveHead(request.headEmployeeId()));
        department.setAnnualBudget(request.annualBudget());

        Department saved = departmentRepository.save(department);
        log.info("Created department {} ({})", saved.getId(), saved.getCode());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse update(UUID id, UpdateDepartmentRequest request) {
        Department department = findOrThrow(id);
        department.setName(request.name().trim());
        department.setDescription(request.description());
        department.setHeadEmployeeId(resolveHead(request.headEmployeeId()));
        department.setAnnualBudget(request.annualBudget());

        Department saved = departmentRepository.save(department);
        log.info("Updated department {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> list(Pageable pageable) {
        Page<Department> page = departmentRepository.findAllByDeletedFalse(pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Department department = findOrThrow(id);
        if (employeeRepository.countByDepartmentIdAndDeletedFalse(id) > 0) {
            throw new BusinessException("Cannot remove a department that still has employees assigned to it");
        }
        department.setDeleted(true);
        departmentRepository.save(department);
        log.info("Deleted department {}", id);
    }

    // ------------------------------------------------------------------

    private Department findOrThrow(UUID id) {
        return departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", id));
    }

    private UUID resolveHead(UUID headEmployeeId) {
        if (headEmployeeId == null) {
            return null;
        }
        if (employeeRepository.findByIdAndDeletedFalse(headEmployeeId).isEmpty()) {
            throw ResourceNotFoundException.of("Employee", headEmployeeId);
        }
        return headEmployeeId;
    }

    private DepartmentResponse toResponse(Department department) {
        String headName = department.getHeadEmployeeId() == null ? null
                : employeeRepository.findByIdAndDeletedFalse(department.getHeadEmployeeId())
                        .map(Employee::getFullName).orElse(null);
        long employeeCount = employeeRepository.countByDepartmentIdAndDeletedFalse(department.getId());
        return departmentMapper.toResponse(department, headName, employeeCount);
    }
}
