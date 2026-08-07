package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.project.dto.request.CreateSprintRequest;
import com.teamflow.ai.project.dto.request.UpdateSprintRequest;
import com.teamflow.ai.project.dto.request.UpdateSprintStatusRequest;
import com.teamflow.ai.project.dto.response.SprintResponse;
import com.teamflow.ai.project.service.SprintService;
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
@RequestMapping("/api/v1/sprints")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sprints", description = "Time-boxed iterations of work within a project")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.CREATE_PROJECT + "')")
    @Operation(summary = "Create a sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> create(@Valid @RequestBody CreateSprintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Sprint created", sprintService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_PROJECT + "')")
    @Operation(summary = "Update a sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> update(@PathVariable UUID id,
                                                               @Valid @RequestBody UpdateSprintRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Sprint updated", sprintService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a sprint by id")
    public ResponseEntity<ApiResponse<SprintResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.get(id)));
    }

    @GetMapping
    @Operation(summary = "List sprints for a project")
    public ResponseEntity<ApiResponse<PageResponse<SprintResponse>>> list(
            @RequestParam UUID projectId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.listForProject(projectId, pageable)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_PROJECT + "')")
    @Operation(summary = "Transition sprint status")
    public ResponseEntity<ApiResponse<SprintResponse>> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateSprintStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Sprint status updated", sprintService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.DELETE_PROJECT + "')")
    @Operation(summary = "Remove a sprint", description = "Rejected while any task is still assigned to it.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sprintService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Sprint removed", null));
    }
}
