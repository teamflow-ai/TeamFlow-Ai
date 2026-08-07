package com.teamflow.ai.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted by project-service when a meeting is scheduled, updated or cancelled.
 *
 * <p>Consumed by ai-service purely for the "today's meetings" dashboard tile and
 * deadline-proximity alerts; meetings do not factor into the workload score.
 */
public record MeetingEvent(
        UUID eventId,
        Instant occurredAt,
        String routingKey,
        UUID meetingId,
        String title,
        UUID projectId,
        Instant scheduledAt,
        String status) implements DomainEvent {

    public static MeetingEvent of(String routingKey, UUID meetingId, String title, UUID projectId,
                                  Instant scheduledAt, String status) {
        return new MeetingEvent(UUID.randomUUID(), Instant.now(), routingKey,
                meetingId, title, projectId, scheduledAt, status);
    }
}
