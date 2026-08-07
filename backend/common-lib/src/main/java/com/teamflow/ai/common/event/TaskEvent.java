package com.teamflow.ai.common.event;

import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Emitted by project-service across the task lifecycle.
 *
 * <p>Drives two consumers: ai-service recalculates workload and health scores, and
 * the notification consumer informs the assignee.
 */
public record TaskEvent(
        UUID eventId,
        Instant occurredAt,
        String routingKey,
        UUID taskId,
        String title,
        UUID projectId,
        UUID sprintId,
        UUID assigneeId,
        TaskStatus status,
        Priority priority,
        Double estimatedHours,
        Double actualHours,
        LocalDate dueDate) implements DomainEvent {

    public static TaskEvent of(String routingKey, UUID taskId, String title, UUID projectId,
                               UUID sprintId, UUID assigneeId, TaskStatus status, Priority priority,
                               Double estimatedHours, Double actualHours, LocalDate dueDate) {
        return new TaskEvent(UUID.randomUUID(), Instant.now(), routingKey,
                taskId, title, projectId, sprintId, assigneeId, status, priority, estimatedHours, actualHours, dueDate);
    }
}
