package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.request.RecommendationDecisionRequest;
import com.teamflow.ai.ai.dto.response.RecommendationHistoryResponse;
import com.teamflow.ai.ai.service.RecommendationHistoryService;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Recommendation History (product brief Feature 8). */
@RestController
@RequestMapping("/api/v1/ai/recommendation-history")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recommendation History", description = "Every Smart Task Assignment recommendation generated, and the outcome once known")
public class RecommendationHistoryController {

    private final RecommendationHistoryService recommendationHistoryService;

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Recommendation history for one task")
    public ResponseEntity<ApiResponse<List<RecommendationHistoryResponse>>> forTask(@PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.success(recommendationHistoryService.forTask(taskId)));
    }

    @GetMapping
    @Operation(summary = "Full recommendation history, most recent first")
    public ResponseEntity<ApiResponse<PageResponse<RecommendationHistoryResponse>>> all(
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(recommendationHistoryService.all(pageable)));
    }

    @PatchMapping("/{id}/decision")
    @Operation(summary = "Record whether the manager accepted the recommendation",
            description = "Also accepts actualHours once the task is complete, to compute prediction accuracy.")
    public ResponseEntity<ApiResponse<RecommendationHistoryResponse>> recordDecision(
            @PathVariable String id, @Valid @RequestBody RecommendationDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(recommendationHistoryService.recordDecision(id, request)));
    }
}
