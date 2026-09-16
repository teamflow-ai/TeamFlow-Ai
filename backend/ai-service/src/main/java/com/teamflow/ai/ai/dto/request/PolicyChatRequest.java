package com.teamflow.ai.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PolicyChatRequest {
    @NotBlank(message = "Query cannot be blank")
    private String query;
}
