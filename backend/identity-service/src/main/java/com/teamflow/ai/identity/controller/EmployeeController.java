package com.teamflow.ai.identity.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.identity.dto.request.CreateEmployeeRequest;
import com.teamflow.ai.identity.dto.request.UpdateEmployeeRequest;
import com.teamflow.ai.identity.dto.response.EmployeeResponse;
import com.teamflow.ai.identity.service.EmployeeService;
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

/**
 * Employee HR-record administration.
 *
 * <p>Distinct from {@code /api/v1/auth}: this manages the workforce roster, not
 * login credentials. An employee record can exist without a {@code User} login
 * (e.g. someone HR is onboarding next week).
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Employees", description = "Workforce roster: profiles, department/team/manager mapping, search")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.CREATE_EMPLOYEE + "')")
    @Operation(summary = "Create an employee record")
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Employee created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_EMPLOYEE + "')")
    @Operation(summary = "Update an employee's profile")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(@PathVariable UUID id,
                                                                @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Employee updated", employeeService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee by id")
    public ResponseEntity<ApiResponse<EmployeeResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.get(id)));
    }

    @GetMapping
    @Operation(summary = "Search employees",
            description = "Filters compose: all supplied parameters are ANDed together. "
                    + "Omit a parameter to leave that dimension unfiltered.")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) UUID managerId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String skill,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.search(query, departmentId, teamId, managerId, active, skill, pageable)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_EMPLOYEE + "')")
    @Operation(summary = "Reactivate an employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Employee activated", employeeService.setActive(id, true)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_EMPLOYEE + "')")
    @Operation(summary = "Deactivate an employee",
            description = "Marks the employee inactive without deleting the record, e.g. for an extended leave.")
    public ResponseEntity<ApiResponse<EmployeeResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated", employeeService.setActive(id, false)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.DELETE_EMPLOYEE + "')")
    @Operation(summary = "Remove an employee record", description = "Soft delete; the row is retained for audit history.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Employee removed", null));
    }
}
