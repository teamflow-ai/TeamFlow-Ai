package com.teamflow.ai.project.client;

import com.teamflow.ai.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Checks employee leave status in identity-service for strict Agile validations.
 */
@FeignClient(name = "identity-service", contextId = "leaveClient", path = "/api/v1/leaves")
public interface LeaveClient {

    @GetMapping("/check-overlap")
    ApiResponse<Boolean> checkOverlap(
            @RequestParam("employeeId") UUID employeeId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
