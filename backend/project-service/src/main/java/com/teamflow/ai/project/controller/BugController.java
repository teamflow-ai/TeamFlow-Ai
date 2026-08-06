package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.security.SecurityUtils;
import com.teamflow.ai.project.dto.request.AssignBugRequest;
import com.teamflow.ai.project.dto.request.CreateBugRequest;
import com.teamflow.ai.project.dto.request.UpdateBugRequest;
import com.teamflow.ai.project.dto.request.UpdateBugStatusRequest;
import com.teamflow.ai.project.dto.response.BugResponse;
import com.teamflow.ai.project.service.BugService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bugs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bugs", description = "Defects reported against a project, optionally linked to the task they block")
public class BugController {

    private final BugService bugService;

    @PostMapping
    @Operation(summary = "Report a bug")
    public ResponseEntity<ApiResponse<BugResponse>> create(@Valid @RequestBody CreateBugRequest request) {
        BugResponse response = bugService.create(request, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Bug reported", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_TASK + "')")
    @Operation(summary = "Update a bug's details")
    public ResponseEntity<ApiResponse<BugResponse>> update(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateBugRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bug updated", bugService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a bug by id")
    public ResponseEntity<ApiResponse<BugResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bugService.get(id)));
    }

    @GetMapping
    @Operation(summary = "List bugs for a project")
    public ResponseEntity<ApiResponse<PageResponse<BugResponse>>> list(
            @RequestParam UUID projectId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(bugService.listForProject(projectId, pageable)));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('" + PermissionNames.ASSIGN_TASK + "')")
    @Operation(summary = "Assign the bug to an employee")
    public ResponseEntity<ApiResponse<BugResponse>> assign(@PathVariable UUID id,
                                                            @Valid @RequestBody AssignBugRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bug assigned", bugService.assign(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_TASK + "')")
    @Operation(summary = "Transition bug status", description = "Resolving or closing may record a resolution note.")
    public ResponseEntity<ApiResponse<BugResponse>> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateBugStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bug status updated", bugService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.DELETE_TASK + "')")
    @Operation(summary = "Remove a bug")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        bugService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Bug removed", null));
    }
}
