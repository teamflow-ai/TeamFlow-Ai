package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Add a comment to a task")
public record AddTaskCommentRequest(
        @NotBlank(message = "Comment text is required")
        @Size(max = 2000)
        String comment) {
}
