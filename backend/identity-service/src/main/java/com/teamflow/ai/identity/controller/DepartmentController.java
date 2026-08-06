package com.teamflow.ai.identity.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.identity.dto.request.CreateDepartmentRequest;
import com.teamflow.ai.identity.dto.request.UpdateDepartmentRequest;
import com.teamflow.ai.identity.dto.response.DepartmentResponse;
import com.teamflow.ai.identity.service.DepartmentService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Departments", description = "Company department administration")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_DEPARTMENTS + "')")
    @Operation(summary = "Create a department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Department created", departmentService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_DEPARTMENTS + "')")
    @Operation(summary = "Update a department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(@PathVariable UUID id,
                                                                   @Valid @RequestBody UpdateDepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Department updated", departmentService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a department by id")
    public ResponseEntity<ApiResponse<DepartmentResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.get(id)));
    }

    @GetMapping
    @Operation(summary = "List departments")
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> list(
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.list(pageable)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.MANAGE_DEPARTMENTS + "')")
    @Operation(summary = "Remove a department", description = "Rejected while any employee is still assigned to it.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Department removed", null));
    }
}
