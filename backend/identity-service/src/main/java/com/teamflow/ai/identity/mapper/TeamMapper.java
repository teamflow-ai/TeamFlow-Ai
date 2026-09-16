package com.teamflow.ai.identity.mapper;

import com.teamflow.ai.identity.dto.response.TeamResponse;
import com.teamflow.ai.identity.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

    public TeamResponse toResponse(Team team, String leadEmployeeName, long memberCount) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .departmentId(team.getDepartment().getId())
                .departmentName(team.getDepartment().getName())
                .leadEmployeeId(team.getLeadEmployeeId())
                .leadEmployeeName(leadEmployeeName)
                .memberCount(memberCount)
                .build();
    }
}
