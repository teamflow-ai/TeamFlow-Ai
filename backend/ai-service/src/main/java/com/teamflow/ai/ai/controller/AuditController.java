package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.response.AuditHistoryResponse;
import com.teamflow.ai.ai.service.AuditHistoryService;
import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only access to the audit trail written by {@code ApprovalEventConsumer}.
 * Gated behind {@code VIEW_REPORT} — the same permission that already governs
 * every other reporting surface in the platform — rather than introducing a
 * dedicated audit permission nobody else needs.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('" + PermissionNames.VIEW_REPORT + "')")
@Tag(name = "Audit", description = "Audit trail and approval history, sourced from every ApprovalEvent published on the platform")
public class AuditController {

    private final AuditHistoryService auditHistoryService;

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Audit trail for one entity",
            description = "entityType is an ApprovalType (MILESTONE, TASK_COMPLETION, PROJECT_CLOSURE, CLIENT_APPROVAL).")
    public ResponseEntity<ApiResponse<PageResponse<AuditHistoryResponse>>> forEntity(
            @PathVariable String entityType, @PathVariable String entityId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(auditHistoryService.forEntity(entityType, entityId, pageable)));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Audit trail for every approval raised under one project")
    public ResponseEntity<ApiResponse<PageResponse<AuditHistoryResponse>>> forProject(
            @PathVariable String projectId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(auditHistoryService.forProject(projectId, pageable)));
    }

    @GetMapping
    @Operation(summary = "Full audit trail", description = "Every approval transition platform-wide, most recent first.")
    public ResponseEntity<ApiResponse<PageResponse<AuditHistoryResponse>>> all(
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(auditHistoryService.all(pageable)));
    }
}
