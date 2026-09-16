package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Record an attachment's metadata against a task (no file content is stored here)")
public record AddTaskAttachmentRequest(
        @NotBlank(message = "File name is required")
        @Size(max = 255)
        String fileName,

        @NotBlank(message = "File URL is required")
        @Size(max = 1000)
        String fileUrl) {
}
