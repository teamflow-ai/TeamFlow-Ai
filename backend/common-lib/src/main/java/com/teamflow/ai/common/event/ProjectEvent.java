package com.teamflow.ai.common.event;

import com.teamflow.ai.common.enums.ProjectStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Emitted by project-service when a project is created or reaches a terminal state. */
public record ProjectEvent(
        UUID eventId,
        Instant occurredAt,
        String routingKey,
        UUID projectId,
        String name,
        UUID managerId,
        UUID clientId,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate) implements DomainEvent {

    public static ProjectEvent of(String routingKey, UUID projectId, String name,
                                  UUID managerId, UUID clientId, ProjectStatus status,
                                  LocalDate startDate, LocalDate endDate) {
        return new ProjectEvent(UUID.randomUUID(), Instant.now(), routingKey,
                projectId, name, managerId, clientId, status, startDate, endDate);
    }
}
