package com.teamflow.ai.project.service;

import com.teamflow.ai.project.dto.response.ProjectHealthResponse;
import java.util.UUID;

public interface ProjectHealthService {
    ProjectHealthResponse getHealth(UUID projectId);
}
