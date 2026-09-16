package com.teamflow.ai.project.mapper;

import com.teamflow.ai.project.dto.response.ProjectMemberResponse;
import com.teamflow.ai.project.dto.response.ProjectResponse;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.ProjectMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project, String clientName, String managerName,
                                      Function<java.util.UUID, String> memberNameResolver, int progressPercent) {
        List<ProjectMemberResponse> members = project.getMembers().stream()
                .map(member -> toMemberResponse(member, memberNameResolver.apply(member.getEmployeeId())))
                .toList();

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .code(project.getCode())
                .description(project.getDescription())
                .clientId(project.getClient() != null ? project.getClient().getId() : null)
                .clientName(clientName)
                .managerId(project.getManagerId())
                .managerName(managerName)
                .status(project.getStatus())
                .priority(project.getPriority())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .budget(project.getBudget())
                .progressPercent(progressPercent)
                .members(members)
                .build();
    }

    private ProjectMemberResponse toMemberResponse(ProjectMember member, String employeeName) {
        return ProjectMemberResponse.builder()
                .employeeId(member.getEmployeeId())
                .employeeName(employeeName)
                .roleOnProject(member.getRoleOnProject())
                .addedAt(member.getAddedAt())
                .build();
    }
}
