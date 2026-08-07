package com.teamflow.ai.common.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Emitted by identity-service when an employee record changes.
 *
 * <p>Carries the skill set and weekly capacity because ai-service needs both to
 * score task assignments, and copying them onto the event avoids a synchronous
 * Feign call on the hot path.
 */
public record EmployeeEvent(
        UUID eventId,
        Instant occurredAt,
        String routingKey,
        UUID employeeId,
        String email,
        String fullName,
        UUID departmentId,
        Set<String> skills,
        Integer weeklyCapacityHours) implements DomainEvent {

    public static EmployeeEvent of(String routingKey, UUID employeeId, String email,
                                   String fullName, UUID departmentId, Set<String> skills,
                                   Integer weeklyCapacityHours) {
        return new EmployeeEvent(UUID.randomUUID(), Instant.now(), routingKey,
                employeeId, email, fullName, departmentId, skills, weeklyCapacityHours);
    }
}
