package com.teamflow.ai.project.mapper;

import com.teamflow.ai.project.dto.response.BugResponse;
import com.teamflow.ai.project.entity.Bug;
import org.springframework.stereotype.Component;

@Component
public class BugMapper {

    public BugResponse toResponse(Bug bug, String reportedByName, String assigneeName) {
        return BugResponse.builder()
                .id(bug.getId())
                .projectId(bug.getProject().getId())
                .taskId(bug.getTask() != null ? bug.getTask().getId() : null)
                .title(bug.getTitle())
                .description(bug.getDescription())
                .severity(bug.getSeverity())
                .status(bug.getStatus())
                .reportedBy(bug.getReportedBy())
                .reportedByName(reportedByName)
                .assigneeId(bug.getAssigneeId())
                .assigneeName(assigneeName)
                .resolution(bug.getResolution())
                .resolvedAt(bug.getResolvedAt())
                .build();
    }
}
