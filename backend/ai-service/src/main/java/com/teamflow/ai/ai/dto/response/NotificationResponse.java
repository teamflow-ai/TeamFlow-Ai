package com.teamflow.ai.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(description = "An in-app notification")
public record NotificationResponse(
        String id,
        String title,
        String body,
        String category,
        String targetUrl,
        boolean read,
        Instant createdAt) {
}
