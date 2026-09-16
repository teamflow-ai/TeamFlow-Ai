package com.teamflow.ai.common.event;

import java.time.Instant;
import java.util.UUID;

/** Emitted by project-service when a member is added to or removed from a project. */
public record ProjectMemberEvent(
        UUID eventId,
        Instant occurredAt,
        String routingKey,
        UUID projectId,
        UUID employeeId,
        Integer allocatedHours) implements DomainEvent {

    public static ProjectMemberEvent of(String routingKey, UUID projectId, UUID employeeId, Integer allocatedHours) {
        return new ProjectMemberEvent(UUID.randomUUID(), Instant.now(), routingKey, projectId, employeeId, allocatedHours);
    }
}
