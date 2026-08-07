package com.teamflow.ai.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Request to deliver a notification to a single recipient.
 *
 * <p>Published by any service; consumed by the notification worker, which persists
 * it and pushes it over WebSocket to connected clients.
 */
public record NotificationEvent(
        UUID eventId,
        Instant occurredAt,
        String routingKey,
        UUID recipientEmployeeId,
        String title,
        String body,
        String category,
        String targetUrl) implements DomainEvent {

    public static NotificationEvent of(UUID recipientEmployeeId, String title,
                                       String body, String category, String targetUrl) {
        return new NotificationEvent(UUID.randomUUID(), Instant.now(),
                com.teamflow.ai.common.constant.MessagingConstants.NOTIFICATION_CREATED,
                recipientEmployeeId, title, body, category, targetUrl);
    }
}
