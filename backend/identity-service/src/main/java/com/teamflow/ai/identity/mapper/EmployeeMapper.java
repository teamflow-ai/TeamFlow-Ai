package com.teamflow.ai.identity.mapper;

import com.teamflow.ai.identity.dto.response.EmployeeResponse;
import com.teamflow.ai.identity.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * Builds the public employee projection.
 *
 * <p>Hand-written for the same reason as {@link UserMapper}: the mapping tolerates
 * null department/team, and {@code managerName} is resolved by the service (which
 * has repository access) rather than here, so a paginated list never pays for an
 * extra lookup per row.
 */
@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee employee) {
        return toResponse(employee, null);
    }

    public EmployeeResponse toResponse(Employee employee, String managerName) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .workEmail(employee.getWorkEmail())
                .phone(employee.getPhone())
                .designation(employee.getDesignation())
                .dateOfJoining(employee.getDateOfJoining())
                .dateOfBirth(employee.getDateOfBirth())
                .profileImageUrl(employee.getProfileImageUrl())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .teamId(primaryTeamId(employee))
                .teamName(primaryTeamName(employee))
                .allTeams(mapTeams(employee))
                .managerId(employee.getManagerId())
                .managerName(managerName)
                .skills(employee.getSkills() != null ? new java.util.LinkedHashSet<>(employee.getSkills()) : null)
                .weeklyCapacityHours(employee.getWeeklyCapacityHours())
                .yearsOfExperience(employee.getYearsOfExperience())
                .annualLeaveBalance(employee.getAnnualLeaveBalance())
                .hourlyRate(employee.getHourlyRate())
                .active(employee.isActive())
                .build();
        }

    private java.util.UUID primaryTeamId(Employee employee) {
        if (employee.getTeams() == null) return null;
        return employee.getTeams().stream()
                .filter(com.teamflow.ai.identity.entity.EmployeeTeamMembership::isPrimary)
                .map(m -> m.getTeam().getId())
                .findFirst().orElse(null);
    }

    private String primaryTeamName(Employee employee) {
        if (employee.getTeams() == null) return null;
        return employee.getTeams().stream()
                .filter(com.teamflow.ai.identity.entity.EmployeeTeamMembership::isPrimary)
                .map(m -> m.getTeam().getName())
                .findFirst().orElse(null);
    }

    private java.util.List<com.teamflow.ai.identity.dto.response.TeamMembershipResponse> mapTeams(Employee employee) {
        if (employee.getTeams() == null) return java.util.List.of();
        return employee.getTeams().stream()
                .map(m -> new com.teamflow.ai.identity.dto.response.TeamMembershipResponse(
                        m.getTeam().getId(), m.getTeam().getName(), m.isPrimary()))
                .toList();
    }
}
