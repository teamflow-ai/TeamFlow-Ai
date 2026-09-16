package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.request.TaskAssignmentRequest;
import com.teamflow.ai.ai.dto.response.ReassignmentSuggestionResponse;
import com.teamflow.ai.ai.dto.response.TaskAssignmentRecommendationResponse;
import com.teamflow.ai.ai.service.RecommendationService;
import com.teamflow.ai.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

/**
 * TeamFlow.AI's signature feature: Intelligent Workload Management & Smart Task
 * Assignment. Every endpoint here is advisory — see {@link RecommendationService}.
 */
@RestController
@RequestMapping("/api/v1/ai/recommendations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recommendations", description = "Deterministic, explainable workload-based assignment recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/task-assignment")
    @PreAuthorize("hasAuthority('" + com.teamflow.ai.common.constant.PermissionNames.ASSIGN_TASK + "')")
    @Operation(summary = "Rank candidates for a task",
            description = "Called by project-service when a manager opts for smart assignment. Returns the "
                    + "least-loaded eligible employees, ranked, with plain-language reasons for each.")
    public ResponseEntity<ApiResponse<List<TaskAssignmentRecommendationResponse>>> recommendTaskAssignment(
            @RequestBody TaskAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.recommend(request)));
    }

    @PostMapping("/candidates")
    @PreAuthorize("hasAuthority('" + com.teamflow.ai.common.constant.PermissionNames.ASSIGN_TASK + "')")
    @Operation(summary = "Get all assignment candidates with workload metrics",
            description = "Returns all active employees with their current workload, capacity, and whether they are AI recommended.")
    public ResponseEntity<ApiResponse<List<com.teamflow.ai.ai.dto.response.AssignmentCandidateResponse>>> getAssignmentCandidates(
            @RequestBody TaskAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getAssignmentCandidates(request)));
    }

    @GetMapping("/reassignment/{employeeId}")
    @PreAuthorize("hasAuthority('" + com.teamflow.ai.common.constant.PermissionNames.ASSIGN_TASK + "')")
    @Operation(summary = "Reassignment suggestions for an overloaded employee",
            description = "Empty when the employee is not currently over the configured overload threshold.")
    public ResponseEntity<ApiResponse<List<ReassignmentSuggestionResponse>>> suggestReassignments(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.suggestReassignments(employeeId)));
    }

}
