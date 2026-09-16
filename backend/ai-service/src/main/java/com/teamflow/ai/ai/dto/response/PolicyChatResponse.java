package com.teamflow.ai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyChatResponse {
    private String answer;
    private String source; // e.g., "HR Handbook", "Benefits Guide" - for future RAG
}
