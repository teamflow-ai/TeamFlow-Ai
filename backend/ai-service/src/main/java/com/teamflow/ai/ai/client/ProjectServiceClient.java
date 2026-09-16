package com.teamflow.ai.ai.client;

import com.teamflow.ai.ai.dto.response.ProjectHealthResponse;
import com.teamflow.ai.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "project-service", path = "/api/v1/projects")
public interface ProjectServiceClient {

    @GetMapping("/{projectId}/health")
    ApiResponse<ProjectHealthResponse> getProjectHealth(@PathVariable("projectId") UUID projectId);
}
