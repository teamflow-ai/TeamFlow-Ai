package com.teamflow.ai.ai.dto.response;

import lombok.Builder;

@Builder
public record TaskRiskResponse(
        String riskLevel,
        String explanation,
        String provider
) {}
