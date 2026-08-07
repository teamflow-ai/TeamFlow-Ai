package com.teamflow.ai.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Contract shared by every message published onto the topic exchange.
 *
 * <p>{@link #eventId()} is the idempotency key. Consumers are required to record
 * processed ids, because RabbitMQ guarantees at-least-once delivery and a redelivery
 * after a partial failure would otherwise double-apply the effect.
 */
public interface DomainEvent {

    UUID eventId();

    Instant occurredAt();

    /** Routing key this event is published under. */
    String routingKey();
}
