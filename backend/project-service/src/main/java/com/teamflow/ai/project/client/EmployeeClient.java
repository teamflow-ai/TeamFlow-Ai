package com.teamflow.ai.project.client;

import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Reads employee data from identity-service.
 *
 * <p>Used only for infrequent, low-volume lookups (validating a manager or member
 * reference when a project or task is created or changed) — never on a per-request
 * hot path such as task-assignment scoring, which instead relies on the
 * {@code EmployeeEvent} projection ai-service builds from the message bus.
 */
@FeignClient(name = "identity-service", path = "/api/v1/employees")
public interface EmployeeClient {

    @GetMapping("/{id}")
    ApiResponse<EmployeeSummary> getEmployee(@PathVariable("id") UUID id);

    /**
     * Reuses identity-service's existing {@code GET /api/v1/employees?query=} search
     * endpoint as-is for {@code GlobalSearchService} — no identity-service change
     * needed, since identity-service's {@code EmployeeResponse} already carries
     * {@code id}/{@code fullName}/{@code active}/{@code departmentId} under those
     * exact names and Jackson ignores the rest.
     */
    @GetMapping
    ApiResponse<PageResponse<EmployeeSummary>> search(@RequestParam("query") String query,
                                                       @RequestParam("size") int size);
}
