package com.teamflow.ai.project.dto.request;

import com.teamflow.ai.common.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Report a bug")
public record CreateBugRequest(

        @NotNull(message = "Project is required")
        UUID projectId,

        UUID taskId,

        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,

        @Size(max = 4000)
        String description,

        Priority severity,

        UUID assigneeId) {
}
