package com.teamflow.ai.project.dto.request;

import com.teamflow.ai.common.enums.AssignmentMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Assign (or reassign) a task to an employee")
public record AssignTaskRequest(

        @NotNull(message = "Assignee is required")
        java.util.UUID assigneeId,

        @Schema(description = "How this assignee was chosen: a manager's own pick, or one they accepted from the "
                + "recommendation list. Purely informational — both paths assign the task the same way.")
        AssignmentMode mode) {
}
