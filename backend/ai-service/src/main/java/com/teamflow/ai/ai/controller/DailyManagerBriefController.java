package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.response.DailyManagerBriefResponse;
import com.teamflow.ai.ai.service.DailyManagerBriefService;
import com.teamflow.ai.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

/** AI Daily Manager Brief (product brief Feature 4). */
@RestController
@RequestMapping("/api/v1/ai/daily-brief")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Daily Manager Brief", description = "One intelligent summary composed from alerts, workload and project health")
public class DailyManagerBriefController {

    private final DailyManagerBriefService dailyManagerBriefService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + com.teamflow.ai.common.constant.PermissionNames.VIEW_ANALYTICS + "')")
    @Operation(summary = "Get today's manager brief")
    public ResponseEntity<ApiResponse<DailyManagerBriefResponse>> getBrief() {
        return ResponseEntity.ok(ApiResponse.success(dailyManagerBriefService.getBrief()));
    }
}
