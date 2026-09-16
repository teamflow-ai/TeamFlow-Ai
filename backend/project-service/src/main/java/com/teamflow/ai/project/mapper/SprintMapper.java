package com.teamflow.ai.project.mapper;

import com.teamflow.ai.project.dto.response.SprintResponse;
import com.teamflow.ai.project.entity.Sprint;
import org.springframework.stereotype.Component;

@Component
public class SprintMapper {

    public SprintResponse toResponse(Sprint sprint, long taskCount, long completedTaskCount) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .projectId(sprint.getProject().getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .taskCount(taskCount)
                .completedTaskCount(completedTaskCount)
                .build();
    }
}
