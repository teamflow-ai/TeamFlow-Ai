package com.teamflow.ai.project.mapper;

import com.teamflow.ai.project.dto.response.TaskAttachmentResponse;
import com.teamflow.ai.project.dto.response.TaskCommentResponse;
import com.teamflow.ai.project.dto.response.TaskHistoryResponse;
import com.teamflow.ai.project.dto.response.TaskResponse;
import com.teamflow.ai.project.entity.Task;
import com.teamflow.ai.project.entity.TaskAttachment;
import com.teamflow.ai.project.entity.TaskComment;
import com.teamflow.ai.project.entity.TaskStatusHistory;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task, String assigneeName, String reporterName) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .assigneeId(task.getAssigneeId())
                .assigneeName(assigneeName)
                .reporterId(task.getReporterId())
                .reporterName(reporterName)
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .assignmentMode(task.getAssignmentMode())
                .requiredSkills(task.getRequiredSkills())
                .overdue(task.isOverdue())
                .build();
    }

    public TaskCommentResponse toResponse(TaskComment comment, String authorName) {
        return TaskCommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .authorId(comment.getAuthorId())
                .authorName(authorName)
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public TaskHistoryResponse toResponse(TaskStatusHistory history, String changedByName) {
        return TaskHistoryResponse.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedBy(history.getChangedBy())
                .changedByName(changedByName)
                .createdAt(history.getCreatedAt())
                .build();
    }

    public TaskAttachmentResponse toResponse(TaskAttachment attachment, String uploadedByName) {
        return TaskAttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileUrl(attachment.getFileUrl())
                .uploadedBy(attachment.getUploadedBy())
                .uploadedByName(uploadedByName)
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
