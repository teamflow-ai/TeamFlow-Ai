package com.teamflow.ai.identity.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.identity.dto.request.CreateTeamRequest;
import com.teamflow.ai.identity.dto.request.UpdateTeamRequest;
import com.teamflow.ai.identity.dto.response.TeamResponse;
import com.teamflow.ai.identity.service.TeamService;
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
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Teams", description = "Teams within a department; the unit work is normally allocated to")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_DEPARTMENTS + "')")
    @Operation(summary = "Create a team")
    public ResponseEntity<ApiResponse<TeamResponse>> create(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Team created", teamService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_DEPARTMENTS + "')")
    @Operation(summary = "Update a team")
    public ResponseEntity<ApiResponse<TeamResponse>> update(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateTeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team updated", teamService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a team by id")
    public ResponseEntity<ApiResponse<TeamResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.get(id)));
    }

    @GetMapping
    @Operation(summary = "List teams", description = "Optionally filtered to a single department.")
    public ResponseEntity<ApiResponse<PageResponse<TeamResponse>>> list(
            @RequestParam(required = false) UUID departmentId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(teamService.list(departmentId, pageable)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_DEPARTMENTS + "')")
    @Operation(summary = "Remove a team", description = "Rejected while any employee is still assigned to it.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        teamService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Team removed", null));
    }
}
