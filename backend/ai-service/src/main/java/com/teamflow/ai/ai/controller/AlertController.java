package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.response.AlertResponse;
import com.teamflow.ai.ai.service.AlertService;
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
@RequestMapping("/api/v1/analytics/alerts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Alerts", description = "Live manager-facing alerts from the workload engine")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.VIEW_ANALYTICS + "')")
    @Operation(summary = "Current alerts",
            description = "Overloaded employees, high-priority concentration, overdue tasks, approaching "
                    + "deadlines, and sprint imbalance — computed fresh on every call.")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlerts() {
        return ResponseEntity.ok(ApiResponse.success(alertService.getAlerts()));
    }
}
