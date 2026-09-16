package com.teamflow.ai.project.mapper;

import com.teamflow.ai.project.dto.response.WorkLogResponse;
import com.teamflow.ai.project.entity.WorkLog;
import org.springframework.stereotype.Component;

@Component
public class WorkLogMapper {

    public WorkLogResponse toResponse(WorkLog workLog, String employeeName) {
        return WorkLogResponse.builder()
                .id(workLog.getId())
                .employeeId(workLog.getEmployeeId())
                .employeeName(employeeName)
                .taskId(workLog.getTask() != null ? workLog.getTask().getId() : null)
                .taskTitle(workLog.getTask() != null ? workLog.getTask().getTitle() : null)
                .projectId(workLog.getProject().getId())
                .projectName(workLog.getProject().getName())
                .logDate(workLog.getLogDate())
                .hours(workLog.getHours())
                .notes(workLog.getNotes())
                .build();
    }
}
