package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.security.SecurityUtils;
import com.teamflow.ai.project.dto.request.CreateWorkLogRequest;
import com.teamflow.ai.project.dto.request.UpdateWorkLogRequest;
import com.teamflow.ai.project.dto.response.WorkLogResponse;
import com.teamflow.ai.project.service.WorkLogService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/worklogs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Work Logs", description = "Daily effort logged by employees against a project or task")
public class WorkLogController {

    private final WorkLogService workLogService;

    @PostMapping
    @Operation(summary = "Log time worked")
    public ResponseEntity<ApiResponse<WorkLogResponse>> create(@Valid @RequestBody CreateWorkLogRequest request) {
        WorkLogResponse response = workLogService.create(request, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Work logged", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a work log entry", description = "Only the employee who logged it may edit it.")
    public ResponseEntity<ApiResponse<WorkLogResponse>> update(@PathVariable UUID id,
                                                                @Valid @RequestBody UpdateWorkLogRequest request) {
        WorkLogResponse response =
                workLogService.update(id, request, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("Work log updated", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a work log entry by id")
    public ResponseEntity<ApiResponse<WorkLogResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(workLogService.get(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "My logged work")
    public ResponseEntity<ApiResponse<PageResponse<WorkLogResponse>>> mine(
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                workLogService.listForEmployee(SecurityUtils.requireCurrentEmployeeId(), pageable)));
    }

    @GetMapping
    @Operation(summary = "List work logs", description = "Filter by project or task.")
    public ResponseEntity<ApiResponse<PageResponse<WorkLogResponse>>> list(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID taskId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        if (taskId != null) {
            return ResponseEntity.ok(ApiResponse.success(workLogService.listForTask(taskId, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.success(workLogService.listForProject(projectId, pageable)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a work log entry", description = "Only the employee who logged it may delete it.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        workLogService.delete(id, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("Work log removed", null));
    }
}
