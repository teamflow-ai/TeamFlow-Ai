package com.teamflow.ai.ai.controller;

import com.teamflow.ai.ai.dto.response.NotificationResponse;
import com.teamflow.ai.ai.service.NotificationService;
import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "In-app notification history for the signed-in employee")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List my notifications")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        var page = notificationService.list(SecurityUtils.requireCurrentEmployeeId(), unreadOnly, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Unread notification count")
    public ResponseEntity<ApiResponse<Long>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.unreadCount(SecurityUtils.requireCurrentEmployeeId())));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark one notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(@PathVariable String id) {
        var response = notificationService.markRead(id, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("Marked as read", response));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark every notification as read")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        notificationService.markAllRead(SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        notificationService.delete(id, SecurityUtils.requireCurrentEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("Notification removed", null));
    }
}
