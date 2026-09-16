package com.teamflow.ai.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "Metadata for a file attached to a task")
public record TaskAttachmentResponse(
        UUID id,
        String fileName,
        String fileUrl,
        UUID uploadedBy,
        String uploadedByName,
        Instant createdAt) {
}
