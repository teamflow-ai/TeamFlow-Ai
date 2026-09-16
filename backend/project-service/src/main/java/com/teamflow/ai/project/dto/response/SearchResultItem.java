package com.teamflow.ai.project.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SearchResultItem(
        String type,
        UUID id,
        String title,
        String subtitle) {
}
