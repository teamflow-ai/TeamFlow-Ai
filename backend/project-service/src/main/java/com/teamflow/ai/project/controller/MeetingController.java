package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.security.SecurityUtils;
import com.teamflow.ai.project.dto.request.AddMeetingNotesRequest;
import com.teamflow.ai.project.dto.request.CreateMeetingRequest;
import com.teamflow.ai.project.dto.request.UpdateMeetingRequest;
import com.teamflow.ai.project.dto.response.MeetingResponse;
import com.teamflow.ai.project.service.MeetingService;
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

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Meetings", description = "Scheduled meetings, optionally tied to a project")
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    @Operation(summary = "Schedule a meeting")
    public ResponseEntity<ApiResponse<MeetingResponse>> create(@Valid @RequestBody CreateMeetingRequest request) {
        MeetingResponse response = meetingService.create(request, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Meeting scheduled", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a meeting")
    public ResponseEntity<ApiResponse<MeetingResponse>> update(@PathVariable UUID id,
                                                                @Valid @RequestBody UpdateMeetingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Meeting updated", meetingService.update(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a meeting by id")
    public ResponseEntity<ApiResponse<MeetingResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.get(id)));
    }

    @GetMapping
    @Operation(summary = "List meetings for a project")
    public ResponseEntity<ApiResponse<PageResponse<MeetingResponse>>> list(
            @RequestParam UUID projectId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.listForProject(projectId, pageable)));
    }

    @PatchMapping("/{id}/notes")
    @Operation(summary = "Record meeting notes", description = "Also marks the meeting completed if it was still scheduled.")
    public ResponseEntity<ApiResponse<MeetingResponse>> addNotes(@PathVariable UUID id,
                                                                  @Valid @RequestBody AddMeetingNotesRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Notes recorded", meetingService.addNotes(id, request)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a meeting")
    public ResponseEntity<ApiResponse<MeetingResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Meeting cancelled", meetingService.cancel(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a meeting")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        meetingService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Meeting removed", null));
    }
}
