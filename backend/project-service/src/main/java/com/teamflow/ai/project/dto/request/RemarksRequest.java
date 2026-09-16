package com.teamflow.ai.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional free-text remarks accompanying an action")
public record RemarksRequest(

        @Size(max = 1000)
        String remarks) {
}
