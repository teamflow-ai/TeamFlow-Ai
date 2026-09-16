package com.teamflow.ai.project.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GlobalSearchResponse(
        List<SearchResultItem> projects,
        List<SearchResultItem> tasks,
        List<SearchResultItem> clients,
        List<SearchResultItem> meetings,
        List<SearchResultItem> employees) {
}
