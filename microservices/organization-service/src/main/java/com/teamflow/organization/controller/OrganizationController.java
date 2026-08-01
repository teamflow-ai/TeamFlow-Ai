package com.teamflow.organization.controller;

import com.teamflow.organization.dto.request.CreateOrganizationRequest;
import com.teamflow.organization.dto.request.UpdateOrganizationRequest;
import com.teamflow.organization.dto.request.UpdateOrganizationStatusRequest;
import com.teamflow.organization.dto.response.ApiResponse;
import com.teamflow.organization.dto.response.OrganizationResponse;
import com.teamflow.organization.enums.OrganizationStatus;
import com.teamflow.organization.service.interfaces.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST endpoints for managing organizations.
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "Organization management APIs")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Create a new organization")
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Organization created successfully", response));
    }

    @Operation(summary = "Get an organization by its id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable UUID id) {
        OrganizationResponse response = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get an organization by its unique code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationByCode(@PathVariable String code) {
        OrganizationResponse response = organizationService.getOrganizationByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "List organizations, optionally filtered by status")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrganizationResponse>>> getAllOrganizations(
            @RequestParam(required = false) OrganizationStatus status,
            Pageable pageable) {
        Page<OrganizationResponse> response = organizationService.getAllOrganizations(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Search organizations by name")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<OrganizationResponse>>> searchOrganizations(
            @RequestParam String name,
            Pageable pageable) {
        Page<OrganizationResponse> response = organizationService.searchOrganizationsByName(name, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update an organization's profile")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        OrganizationResponse response = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", response));
    }

    @Operation(summary = "Update an organization's status (activate, deactivate, suspend)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganizationStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        OrganizationResponse response = organizationService.updateOrganizationStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Organization status updated successfully", response));
    }

    @Operation(summary = "Delete an organization")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully", null));
    }

}
