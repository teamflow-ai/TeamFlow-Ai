package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.request.TaskEstimationRequest;
import com.teamflow.ai.ai.dto.response.TaskEstimationResponse;
import com.teamflow.ai.ai.service.TaskEstimationService;
import com.teamflow.ai.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

/** AI Task Estimation (product brief Feature 2). Called before a task is created. */
@RestController
@RequestMapping("/api/v1/ai/estimation")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Task Estimation", description = "Effort/date estimate from historical completed-task data")
public class TaskEstimationController {

    private final TaskEstimationService taskEstimationService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + com.teamflow.ai.common.constant.PermissionNames.UPDATE_TASK + "')")
    @Operation(summary = "Estimate effort for a task before creating it")
    public ResponseEntity<ApiResponse<TaskEstimationResponse>> estimate(
            @Valid @RequestBody TaskEstimationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskEstimationService.estimate(request)));
    }
}
