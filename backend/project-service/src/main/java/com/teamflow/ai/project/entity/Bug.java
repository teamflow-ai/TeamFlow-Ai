package com.teamflow.ai.project.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import com.teamflow.ai.common.enums.BugStatus;
import com.teamflow.ai.common.enums.Priority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** A defect reported against a project, optionally linked to the task it blocks. */
@Entity
@Table(name = "bugs", indexes = {
        @Index(name = "idx_bugs_project", columnList = "project_id"),
        @Index(name = "idx_bugs_assignee", columnList = "assignee_id"),
        @Index(name = "idx_bugs_status", columnList = "status"),
        @Index(name = "idx_bugs_severity", columnList = "severity"),
        @Index(name = "idx_bugs_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
public class Bug extends AuditableEntity {

    @NotNull(message = "Project is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_bugs_project"))
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_bugs_task"))
    private Task task;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 4000)
    @Column(name = "description", length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Priority severity = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BugStatus status = BugStatus.OPEN;

    @NotNull(message = "Reporter is required")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "reported_by", length = 36, nullable = false)
    private UUID reportedBy;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "assignee_id", length = 36)
    private UUID assigneeId;

    @Size(max = 2000)
    @Column(name = "resolution", length = 2000)
    private String resolution;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
