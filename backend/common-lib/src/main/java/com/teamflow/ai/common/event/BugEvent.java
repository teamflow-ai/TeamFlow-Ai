package com.teamflow.ai.common.event;

import com.teamflow.ai.common.enums.Priority;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted by project-service across the bug lifecycle.
 *
 * <p>Consumed by ai-service for the same two reasons as {@link TaskEvent}: a
 * critical bug assigned to someone counts toward their workload score, and open/
 * closed counts feed the bug dashboard without a synchronous call back into
 * project-service.
 */
public record BugEvent(
        UUID eventId,
        Instant occurredAt,
        String routingKey,
        UUID bugId,
        String title,
        UUID projectId,
        UUID assigneeId,
        String status,
        Priority severity) implements DomainEvent {

    public static BugEvent of(String routingKey, UUID bugId, String title, UUID projectId,
                              UUID assigneeId, String status, Priority severity) {
        return new BugEvent(UUID.randomUUID(), Instant.now(), routingKey,
                bugId, title, projectId, assigneeId, status, severity);
    }
}
