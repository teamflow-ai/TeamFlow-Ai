package com.teamflow.ai.project.approval.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.common.security.SecurityUtils;
import com.teamflow.ai.project.approval.dto.ApprovalDecisionRequest;
import com.teamflow.ai.project.approval.dto.ApprovalResponse;
import com.teamflow.ai.project.approval.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Generic view and decision endpoints for the Approval Workflow Engine.
 *
 * <p>Opening an approval request is always initiated from the module that owns
 * the underlying entity (e.g. {@code POST /api/v1/milestones/{id}/submit}), never
 * from a generic "create approval of any type" endpoint here — that would let a
 * caller open an approval against an entity id it has no real authority over.
 * Authorization for {@code decide} is deliberately coarse (any of the three
 * approver-capable permissions); each {@code ApprovalOutcomeHandler} still
 * enforces its own type-specific business rules.
 */
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Approvals", description = "Generic Approval Workflow Engine: milestones, task completion, project closure, client sign-off")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/{id}")
    @Operation(summary = "Get an approval request by id")
    public ResponseEntity<ApiResponse<ApprovalResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(approvalService.get(id)));
    }

    @GetMapping
    @Operation(summary = "Search approval requests",
            description = "Filter by type, status, approver or project. Typically used to build an approver's inbox.")
    public ResponseEntity<ApiResponse<PageResponse<ApprovalResponse>>> search(
            @RequestParam(required = false) ApprovalType approvalType,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) UUID approverId,
            @RequestParam(required = false) UUID projectId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalService.search(approvalType, status, approverId, projectId, pageable)));
    }

    @PatchMapping("/{id}/decide")
    @PreAuthorize("hasAuthority('" + PermissionNames.APPROVE_MILESTONE + "')"
            + " or hasAuthority('" + PermissionNames.APPROVE_TASK_COMPLETION + "')"
            + " or hasAuthority('" + PermissionNames.APPROVE_PROJECT_CLOSURE + "')"
            + " or hasAuthority('" + PermissionNames.MANAGE_CLIENT_APPROVAL + "')")
    @Operation(summary = "Approve, reject or return an approval request",
            description = "decision must be APPROVED, REJECTED or RETURNED_FOR_CHANGES. The caller must additionally "
                    + "hold the specific permission for this request's approval type.")
    public ResponseEntity<ApiResponse<ApprovalResponse>> decide(
            @PathVariable UUID id, @Valid @RequestBody ApprovalDecisionRequest request) {
        ApprovalResponse response = approvalService.decide(id, SecurityUtils.requireCurrentEmployeeId(), request);
        return ResponseEntity.ok(ApiResponse.success("Approval decision recorded", response));
    }
}
