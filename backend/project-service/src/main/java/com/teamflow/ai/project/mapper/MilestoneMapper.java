package com.teamflow.ai.project.mapper;

import com.teamflow.ai.project.dto.response.MilestoneResponse;
import com.teamflow.ai.project.entity.Milestone;
import org.springframework.stereotype.Component;

@Component
public class MilestoneMapper {

    public MilestoneResponse toResponse(Milestone milestone) {
        return MilestoneResponse.builder()
                .id(milestone.getId())
                .projectId(milestone.getProject().getId())
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .dueDate(milestone.getDueDate())
                .status(milestone.getStatus())
                .build();
    }
}
