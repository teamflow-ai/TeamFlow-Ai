package com.teamflow.ai.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Team")
public record TeamResponse(

        UUID id,
        String name,
        String description,
        UUID departmentId,
        String departmentName,
        UUID leadEmployeeId,
        String leadEmployeeName,
        long memberCount) {
}
