package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.common.security.SecurityUtils;
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
import com.teamflow.ai.project.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Task CRUD, lifecycle transitions, and the two assignment paths described in the
 * product brief: a manager may assign an employee directly via {@link #assign}, or
 * first call {@link #recommendations} to see ai-service's ranked, explainable
 * suggestions and then assign whichever candidate they accept — through the same
 * endpoint, since accepting a recommendation is still, mechanically, an assignment.
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tasks", description = "Units of work: assignment, status lifecycle, comments, history, attachments")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.ASSIGN_TASK + "') or hasAuthority('" + PermissionNames.UPDATE_TASK + "')")
    @Operation(summary = "Create a task", description = "The task starts unassigned; use /assign or /recommendations next.")
    public ResponseEntity<ApiResponse<TaskResponse>> create(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.create(request, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Task created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_TASK + "')")
    @Operation(summary = "Update a task's details")
    public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable UUID id,
                                                             @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated", taskService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by id")
    public ResponseEntity<ApiResponse<TaskResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.get(id)));
    }

    @GetMapping
    @Operation(summary = "Search tasks")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> search(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID sprintId,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                taskService.search(projectId, sprintId, assigneeId, status, priority, pageable)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_TASK + "')")
    @Operation(summary = "Transition task status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateTaskStatusRequest request) {
        TaskResponse response = taskService.updateStatus(id, request, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("Task status updated", response));
    }

    @GetMapping("/{id}/recommendations")
    @PreAuthorize("hasAuthority('" + PermissionNames.ASSIGN_TASK + "')")
    @Operation(summary = "Smart assignment recommendations",
            description = "Ranked, explainable candidates from the workload engine. Purely advisory: nothing is "
                    + "assigned until you call /assign.")
    public ResponseEntity<ApiResponse<List<TaskAssignmentRecommendation>>> recommendations(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.recommendAssignees(id)));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('" + PermissionNames.ASSIGN_TASK + "')")
    @Operation(summary = "Assign or reassign the task",
            description = "Used both for a manual pick and for accepting a recommended candidate.")
    public ResponseEntity<ApiResponse<TaskResponse>> assign(@PathVariable UUID id,
                                                             @Valid @RequestBody AssignTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task assigned", taskService.assign(id, request)));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable UUID id, @Valid @RequestBody AddTaskCommentRequest request) {
        TaskCommentResponse response =
                taskService.addComment(id, request, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Comment added", response));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "List comments")
    public ResponseEntity<ApiResponse<PageResponse<TaskCommentResponse>>> listComments(
            @PathVariable UUID id, @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(taskService.listComments(id, pageable)));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Status transition history")
    public ResponseEntity<ApiResponse<List<TaskHistoryResponse>>> listHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.listHistory(id)));
    }

    @PostMapping("/{id}/attachments")
    @Operation(summary = "Record an attachment's metadata",
            description = "No file content is uploaded through this API; the client uploads the file elsewhere "
                    + "and records the resulting URL here.")
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> addAttachment(
            @PathVariable UUID id, @Valid @RequestBody AddTaskAttachmentRequest request) {
        TaskAttachmentResponse response =
                taskService.addAttachment(id, request, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Attachment recorded", response));
    }

    @GetMapping("/{id}/attachments")
    @Operation(summary = "List attachments")
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> listAttachments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.listAttachments(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.DELETE_TASK + "')")
    @Operation(summary = "Remove a task")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Task removed", null));
    }
}
