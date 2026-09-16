package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

/**
 * ai-service's read model of a task, built from {@code TaskEvent} messages.
 *
 * <p>This is the primary input to the workload scorer: active-task counts,
 * priority weighting and overdue detection are all computed from this collection
 * rather than from project-service's database.
 */
@Document(collection = "task_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class TaskSnapshot {

    @Id
    private String taskId;

    private String title;
    private String projectId;
    private String sprintId;
    private String assigneeId;
    private String status;
    private String priority;
    private Double estimatedHours;
    private Double actualHours;
    private LocalDate dueDate;
    private Instant updatedAt;
}
