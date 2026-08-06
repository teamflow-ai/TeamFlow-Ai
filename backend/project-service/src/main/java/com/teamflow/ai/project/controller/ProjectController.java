package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.ProjectStatus;
import com.teamflow.ai.project.dto.request.AddProjectMemberRequest;
import com.teamflow.ai.project.dto.request.CreateProjectRequest;
import com.teamflow.ai.project.dto.request.UpdateProjectRequest;
import com.teamflow.ai.project.dto.request.UpdateProjectStatusRequest;
import com.teamflow.ai.project.dto.response.ProjectResponse;
import com.teamflow.ai.project.service.ProjectService;
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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Projects", description = "Delivery engagements: status, priority, team membership, progress")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.CREATE_PROJECT + "')")
    @Operation(summary = "Create a project")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Project created", projectService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_PROJECT + "')")
    @Operation(summary = "Update a project")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(@PathVariable UUID id,
                                                                @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project updated", projectService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by id")
    public ResponseEntity<ApiResponse<ProjectResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.get(id)));
    }

    @GetMapping
    @Operation(summary = "Search projects")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) UUID managerId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(projectService.search(query, status, managerId, pageable)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_PROJECT + "')")
    @Operation(summary = "Transition project status")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateProjectStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project status updated", projectService.updateStatus(id, request)));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_PROJECT + "')")
    @Operation(summary = "Add a team member to the project")
    public ResponseEntity<ApiResponse<ProjectResponse>> addMember(
            @PathVariable UUID id, @Valid @RequestBody AddProjectMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Member added", projectService.addMember(id, request)));
    }

    @DeleteMapping("/{id}/members/{employeeId}")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_PROJECT + "')")
    @Operation(summary = "Remove a team member from the project")
    public ResponseEntity<ApiResponse<ProjectResponse>> removeMember(
            @PathVariable UUID id, @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Member removed", projectService.removeMember(id, employeeId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.DELETE_PROJECT + "')")
    @Operation(summary = "Remove a project")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Project removed", null));
    }
}
