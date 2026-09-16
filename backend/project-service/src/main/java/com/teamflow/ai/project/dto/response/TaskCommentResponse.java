package com.teamflow.ai.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "Comment left on a task")
public record TaskCommentResponse(
        UUID id,
        UUID taskId,
        UUID authorId,
        String authorName,
        String comment,
        Instant createdAt) {
}
