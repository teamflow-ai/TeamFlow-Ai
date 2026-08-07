package com.teamflow.ai.identity.controller;

import com.teamflow.ai.common.constant.PermissionNames;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.security.SecurityUtils;
import com.teamflow.ai.identity.dto.request.CreateLeaveRequest;
import com.teamflow.ai.identity.dto.request.LeaveDecisionRequest;
import com.teamflow.ai.identity.dto.response.LeaveResponse;
import com.teamflow.ai.identity.service.LeaveService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * The two-stage (manager, then HR) leave approval workflow.
 */
@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Leave", description = "Time-off requests and their manager/HR approval workflow")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @Operation(summary = "Request time off")
    public ResponseEntity<ApiResponse<LeaveResponse>> request(@Valid @RequestBody CreateLeaveRequest request) {
        LeaveResponse response = leaveService.request(SecurityUtils.requireCurrentEmployeeId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Leave requested", response));
    }

    @GetMapping("/me")
    @Operation(summary = "My leave requests")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> myRequests(
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                leaveService.listForEmployee(SecurityUtils.requireCurrentEmployeeId(), pageable)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('" + PermissionNames.APPROVE_LEAVE + "')")
    @Operation(summary = "Requests awaiting a decision")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> pending(
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.listPending(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a leave request by id")
    public ResponseEntity<ApiResponse<LeaveResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.get(id)));
    }

    @PostMapping("/{id}/manager-decision")
    @PreAuthorize("hasAuthority('" + PermissionNames.APPROVE_LEAVE + "')")
    @Operation(summary = "Manager approval or rejection", description = "First stage of the two-stage workflow.")
    public ResponseEntity<ApiResponse<LeaveResponse>> managerDecision(
            @PathVariable UUID id, @Valid @RequestBody LeaveDecisionRequest request) {
        LeaveResponse response =
                leaveService.managerDecision(id, SecurityUtils.requireCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Manager decision recorded", response));
    }

    @PostMapping("/{id}/hr-decision")
    @PreAuthorize("hasAuthority('" + PermissionNames.APPROVE_LEAVE + "')")
    @Operation(summary = "HR approval or rejection",
            description = "Second and final stage; approval debits the employee's leave balance.")
    public ResponseEntity<ApiResponse<LeaveResponse>> hrDecision(
            @PathVariable UUID id, @Valid @RequestBody LeaveDecisionRequest request) {
        LeaveResponse response = leaveService.hrDecision(id, SecurityUtils.requireCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("HR decision recorded", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel my own leave request")
    public ResponseEntity<ApiResponse<LeaveResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled",
                leaveService.cancel(id, SecurityUtils.requireCurrentEmployeeId())));
    }
}
