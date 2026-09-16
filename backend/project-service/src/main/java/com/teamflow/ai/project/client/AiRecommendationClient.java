package com.teamflow.ai.project.client;

import com.teamflow.ai.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * The synchronous seam into ai-service's Intelligent Workload Management engine.
 *
 * <p>This is the one place project-service calls ai-service synchronously rather
 * than through the event bus: a manager creating a task wants recommendations
 * before the task is even saved, so it cannot wait for an asynchronous round trip.
 * Every other interaction between the two services flows through RabbitMQ.
 */
@FeignClient(name = "ai-service", path = "/api/v1/ai/recommendations")
public interface AiRecommendationClient {

    @PostMapping("/task-assignment")
    ApiResponse<List<TaskAssignmentRecommendation>> recommendAssignees(
            @RequestBody TaskAssignmentRecommendationRequest request);

    @PostMapping("/candidates")
    ApiResponse<List<AssignmentCandidateResponse>> getAssignmentCandidates(
            @RequestBody TaskAssignmentRecommendationRequest request);
}
