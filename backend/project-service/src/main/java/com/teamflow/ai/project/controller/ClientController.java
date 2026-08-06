package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.project.dto.request.CreateClientRequest;
import com.teamflow.ai.project.dto.request.UpdateClientRequest;
import com.teamflow.ai.project.dto.response.ClientResponse;
import com.teamflow.ai.project.service.ClientService;
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
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clients", description = "External clients projects are delivered for")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.CREATE_PROJECT + "')")
    @Operation(summary = "Create a client")
    public ResponseEntity<ApiResponse<ClientResponse>> create(@Valid @RequestBody CreateClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Client created", clientService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.UPDATE_PROJECT + "')")
    @Operation(summary = "Update a client")
    public ResponseEntity<ApiResponse<ClientResponse>> update(@PathVariable UUID id,
                                                               @Valid @RequestBody UpdateClientRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Client updated", clientService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a client by id")
    public ResponseEntity<ApiResponse<ClientResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(clientService.get(id)));
    }

    @GetMapping
    @Operation(summary = "List clients")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> list(
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(clientService.list(pageable)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.DELETE_PROJECT + "')")
    @Operation(summary = "Remove a client")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Client removed", null));
    }
}
