package com.teamflow.ai.project.dto.request;

import com.teamflow.ai.common.enums.BugStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Transition a bug's status")
public record UpdateBugStatusRequest(

        @NotNull(message = "Status is required")
        BugStatus status,

        @Size(max = 2000)
        String resolution) {
}
