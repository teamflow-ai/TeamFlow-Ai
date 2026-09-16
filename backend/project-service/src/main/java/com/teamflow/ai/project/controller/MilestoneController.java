package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.security.SecurityUtils;
import com.teamflow.ai.project.approval.dto.ApprovalResponse;
import com.teamflow.ai.project.dto.request.CreateMilestoneRequest;
import com.teamflow.ai.project.dto.request.RemarksRequest;
import com.teamflow.ai.project.dto.request.UpdateMilestoneRequest;
import com.teamflow.ai.project.dto.response.MilestoneResponse;
import com.teamflow.ai.project.service.MilestoneService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/milestones")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Milestones", description = "Project timeline checkpoints, approved via the Approval Workflow Engine")
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_MILESTONES + "')")
    @Operation(summary = "Create a milestone")
    public ResponseEntity<ApiResponse<MilestoneResponse>> create(@Valid @RequestBody CreateMilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Milestone created", milestoneService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_MILESTONES + "')")
    @Operation(summary = "Update a milestone")
    public ResponseEntity<ApiResponse<MilestoneResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateMilestoneRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Milestone updated", milestoneService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a milestone by id")
    public ResponseEntity<ApiResponse<MilestoneResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.get(id)));
    }

    @GetMapping
    @Operation(summary = "List milestones for a project")
    public ResponseEntity<ApiResponse<PageResponse<MilestoneResponse>>> list(
            @RequestParam UUID projectId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.listForProject(projectId, pageable)));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_MILESTONES + "')")
    @Operation(summary = "Mark a milestone ready for review",
            description = "Opens a MILESTONE approval request; an admin decides via /api/v1/approvals/{id}/decide.")
    public ResponseEntity<ApiResponse<ApprovalResponse>> submitForReview(
            @PathVariable UUID id, @RequestBody(required = false) RemarksRequest request) {
        String remarks = request != null ? request.remarks() : null;
        ApprovalResponse response =
                milestoneService.submitForReview(id, SecurityUtils.requireCurrentEmployeeId(), remarks);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Milestone submitted for review", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_MILESTONES + "')")
    @Operation(summary = "Remove a milestone")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        milestoneService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Milestone removed", null));
    }
}
