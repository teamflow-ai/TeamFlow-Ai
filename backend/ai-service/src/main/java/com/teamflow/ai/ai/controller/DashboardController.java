package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.response.DashboardSummaryResponse;
import com.teamflow.ai.ai.dto.response.EmployeeWorkloadResponse;
import com.teamflow.ai.ai.service.DashboardService;
import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Company-wide and per-employee workload metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('" + PermissionNames.VIEW_ANALYTICS + "')")
    @Operation(summary = "Company-wide summary tiles")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> summary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }

    @GetMapping("/workload")
    @PreAuthorize("hasAuthority('" + PermissionNames.VIEW_ANALYTICS + "')")
    @Operation(summary = "Every active employee's current workload",
            description = "Sorted most-loaded first, so an overloaded team member is never buried in the list.")
    public ResponseEntity<ApiResponse<List<EmployeeWorkloadResponse>>> workload() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getWorkloadBoard()));
    }
}
