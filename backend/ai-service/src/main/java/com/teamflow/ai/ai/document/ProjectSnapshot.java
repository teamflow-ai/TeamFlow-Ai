package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

/** ai-service's read model of a project, built from {@code ProjectEvent} messages. */
@Document(collection = "project_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class ProjectSnapshot {

    @Id
    private String projectId;

    private String name;
    private String managerId;
    private String clientId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant updatedAt;
}
