package com.teamflow.ai.project.service;

import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ErrorCode;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.client.EmployeeClient;
import com.teamflow.ai.project.client.EmployeeSummary;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Thin wrapper around {@link EmployeeClient} that translates transport failures
 * into the platform's error vocabulary.
 *
 * <p>Two call shapes are exposed deliberately: {@link #requireExisting} is for
 * writes where a bad reference must reject the request, while
 * {@link #tryResolveName} is for read-side enrichment where a transient
 * identity-service outage should degrade a response (missing name) rather than
 * fail it outright.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeLookupService {

    private final EmployeeClient employeeClient;

    /** @throws ResourceNotFoundException if no such employee exists */
    public EmployeeSummary requireExisting(UUID employeeId) {
        try {
            return employeeClient.getEmployee(employeeId).getData();
        } catch (FeignException.NotFound ex) {
            throw ResourceNotFoundException.of("Employee", employeeId);
        } catch (FeignException ex) {
            log.warn("identity-service unreachable while validating employee {}: {}", employeeId, ex.getMessage());
            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR,
                    "Could not verify the employee right now; please try again shortly");
        }
    }

    /** Best-effort lookup for display purposes; never throws. */
    public String tryResolveName(UUID employeeId) {
        if (employeeId == null) {
            return null;
        }
        try {
            EmployeeSummary summary = employeeClient.getEmployee(employeeId).getData();
            return summary != null ? summary.fullName() : null;
        } catch (Exception ex) {
            log.debug("Could not resolve employee name for {}: {}", employeeId, ex.getMessage());
            return null;
        }
    }
}
