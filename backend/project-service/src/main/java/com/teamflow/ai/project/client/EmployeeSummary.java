package com.teamflow.ai.project.client;

import java.util.UUID;

/**
 * The subset of identity-service's employee projection this service actually
 * needs. Deliberately not the full {@code EmployeeResponse}: keeping this local
 * and minimal means the two services can evolve their own DTOs independently.
 */
public record EmployeeSummary(UUID id, String fullName, boolean active, UUID departmentId) {
}
