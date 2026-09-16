package com.teamflow.ai.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Builder
@Schema(description = "Employee HR record")
public record EmployeeResponse(

        UUID id,
        String employeeCode,
        String firstName,
        String lastName,
        String fullName,
        String workEmail,
        String phone,
        String designation,
        LocalDate dateOfJoining,
        LocalDate dateOfBirth,
        String profileImageUrl,

        UUID departmentId,
        String departmentName,
        UUID teamId,
        String teamName,
        UUID managerId,
        String managerName,

        Set<String> skills,
        Integer weeklyCapacityHours,
        Integer yearsOfExperience,
        Integer annualLeaveBalance,
        BigDecimal hourlyRate,
        boolean active,
        
        java.util.List<TeamMembershipResponse> allTeams) {
}
