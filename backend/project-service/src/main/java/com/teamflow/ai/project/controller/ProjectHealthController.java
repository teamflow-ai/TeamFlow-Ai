package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.project.dto.response.ProjectHealthResponse;
import com.teamflow.ai.project.service.ProjectHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/health")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Project Health", description = "Business-rule health score")
public class ProjectHealthController {

    private final ProjectHealthService projectHealthService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.VIEW_ANALYTICS + "')")
    @Operation(summary = "Get a project's current health score")
    public ResponseEntity<ApiResponse<ProjectHealthResponse>> getHealth(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectHealthService.getHealth(projectId)));
    }
}
