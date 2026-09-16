package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.response.BurnoutAssessmentResponse;
import com.teamflow.ai.ai.service.BurnoutDetectionService;
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

import java.util.UUID;

/** Burnout Detection (product brief Feature 6). */
@RestController
@RequestMapping("/api/v1/ai/burnout")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Burnout Detection", description = "Wellbeing risk assessment built on the existing workload scorer")
public class BurnoutDetectionController {

    private final BurnoutDetectionService burnoutDetectionService;

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('" + com.teamflow.ai.common.constant.PermissionNames.VIEW_ANALYTICS + "')")
    @Operation(summary = "Assess one employee's burnout risk", description = "Notifies their project manager(s) at HIGH_RISK or CRITICAL.")
    public ResponseEntity<ApiResponse<BurnoutAssessmentResponse>> assess(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(burnoutDetectionService.assess(employeeId)));
    }
}
