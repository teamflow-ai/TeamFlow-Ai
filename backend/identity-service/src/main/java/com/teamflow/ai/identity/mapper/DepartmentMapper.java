package com.teamflow.ai.identity.mapper;

import com.teamflow.ai.identity.dto.response.DepartmentResponse;
import com.teamflow.ai.identity.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department department, String headEmployeeName, long employeeCount) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .headEmployeeId(department.getHeadEmployeeId())
                .headEmployeeName(headEmployeeName)
                .annualBudget(department.getAnnualBudget())
                .employeeCount(employeeCount)
                .build();
    }
}
