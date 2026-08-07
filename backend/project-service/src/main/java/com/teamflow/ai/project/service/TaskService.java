package com.teamflow.ai.project.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.project.client.TaskAssignmentRecommendation;
import com.teamflow.ai.project.dto.request.AddTaskAttachmentRequest;
import com.teamflow.ai.project.dto.request.AddTaskCommentRequest;
import com.teamflow.ai.project.dto.request.AssignTaskRequest;
import com.teamflow.ai.project.dto.request.CreateTaskRequest;
import com.teamflow.ai.project.dto.request.UpdateTaskRequest;
import com.teamflow.ai.project.dto.request.UpdateTaskStatusRequest;
import com.teamflow.ai.project.dto.response.TaskAttachmentResponse;
import com.teamflow.ai.project.dto.response.TaskCommentResponse;
import com.teamflow.ai.project.dto.response.TaskHistoryResponse;
import com.teamflow.ai.project.dto.response.TaskResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request, UUID reporterId);

    TaskResponse update(UUID id, UpdateTaskRequest request);

    TaskResponse get(UUID id);

    PageResponse<TaskResponse> search(UUID projectId, UUID sprintId, UUID assigneeId, TaskStatus status,
                                      Priority priority, Pageable pageable);

    TaskResponse updateStatus(UUID id, UpdateTaskStatusRequest request, UUID changedBy);

    TaskResponse assign(UUID id, AssignTaskRequest request);

    List<TaskAssignmentRecommendation> recommendAssignees(UUID id);

    TaskCommentResponse addComment(UUID id, AddTaskCommentRequest request, UUID authorId);

    PageResponse<TaskCommentResponse> listComments(UUID id, Pageable pageable);

    List<TaskHistoryResponse> listHistory(UUID id);

    TaskAttachmentResponse addAttachment(UUID id, AddTaskAttachmentRequest request, UUID uploadedBy);

    List<TaskAttachmentResponse> listAttachments(UUID id);

    void delete(UUID id);
}
