package com.teamflow.ai.common.event;

import com.teamflow.ai.common.enums.LeaveStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Emitted when a leave request is approved.
 *
 * <p>ai-service consumes this to remove the employee's capacity for the covered
 * dates, which immediately changes task-assignment recommendations.
 */
public record LeaveEvent(
        UUID eventId,
        Instant occurredAt,
        String routingKey,
        UUID leaveRequestId,
        UUID employeeId,
        LeaveStatus status,
        LocalDate startDate,
        LocalDate endDate) implements DomainEvent {

    public static LeaveEvent of(String routingKey, UUID leaveRequestId, UUID employeeId,
                                LeaveStatus status, LocalDate startDate, LocalDate endDate) {
        return new LeaveEvent(UUID.randomUUID(), Instant.now(), routingKey,
                leaveRequestId, employeeId, status, startDate, endDate);
    }
}
