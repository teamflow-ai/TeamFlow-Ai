package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Tracks the cross-project workload capacity allocations for employees.
 */
@Document(collection = "project_member_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class ProjectMemberSnapshot {

    @Id
    private String id; // composite: projectId_employeeId

    private String projectId;
    private String employeeId;
    private Integer allocatedHours;
    private Instant updatedAt;
}
