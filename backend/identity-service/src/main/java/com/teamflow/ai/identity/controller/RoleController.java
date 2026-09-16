package com.teamflow.ai.identity.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.identity.dto.response.PermissionResponse;
import com.teamflow.ai.identity.dto.response.RoleResponse;
import com.teamflow.ai.identity.repository.PermissionRepository;
import com.teamflow.ai.identity.repository.RoleRepository;
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

/**
 * Read-only listings for the RBAC catalogue.
 *
 * <p>Roles and permissions themselves are seed data (see the {@code V2} Flyway
 * migration), not something this API mutates: re-mapping what a role grants is an
 * operational, DBA-level change, not a runtime admin action for this version of
 * the platform. What the frontend does need is a way to list both, e.g. to
 * populate a role-selection dropdown during employee onboarding.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Roles & Permissions", description = "Read-only RBAC catalogue")
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_ROLES + "') or hasAuthority('" + PermissionNames.MANAGE_USERS + "')")
    @Operation(summary = "List roles with their granted permissions")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listRoles() {
        List<RoleResponse> roles = roleRepository.findAllWithPermissions().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .systemRole(role.isSystemRole())
                        .permissions(role.getPermissions().stream().map(p -> p.getName()).sorted().toList())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_ROLES + "') or hasAuthority('" + PermissionNames.MANAGE_USERS + "')")
    @Operation(summary = "List every grantable permission")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> listPermissions() {
        List<PermissionResponse> permissions = permissionRepository.findAllByDeletedFalseOrderByCategoryAscNameAsc()
                .stream()
                .map(permission -> PermissionResponse.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .description(permission.getDescription())
                        .category(permission.getCategory())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}
