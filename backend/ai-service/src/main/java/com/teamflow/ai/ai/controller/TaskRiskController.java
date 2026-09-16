package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.response.TaskRiskResponse;
import com.teamflow.ai.ai.service.TaskRiskService;
import com.teamflow.ai.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/ai/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Task Risk AI", description = "AI predictions for task delays and budget overruns")
public class TaskRiskController {

    private final TaskRiskService taskRiskService;

    @GetMapping("/{taskId}/risk")
    @PreAuthorize("hasAuthority('" + com.teamflow.ai.common.constant.PermissionNames.VIEW_REPORT + "')")
    @Operation(summary = "Get an AI risk assessment for a specific task")
    public ResponseEntity<ApiResponse<TaskRiskResponse>> getRiskAssessment(@PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.success(taskRiskService.evaluateRisk(taskId)));
    }
}
