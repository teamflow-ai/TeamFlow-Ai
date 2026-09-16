package com.teamflow.ai.project.controller;

import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.project.dto.response.GlobalSearchResponse;
import com.teamflow.ai.project.service.GlobalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Global Search: one keyword, five categories, top matches from each. */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Global Search", description = "Keyword search across projects, tasks, clients, meetings and employees")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    @Operation(summary = "Search across the platform", description = "Top 5 matches per category.")
    public ResponseEntity<ApiResponse<GlobalSearchResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success(globalSearchService.search(query)));
    }
}
