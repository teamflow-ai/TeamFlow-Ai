package com.teamflow.ai.project.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import com.teamflow.ai.common.enums.AssignmentMode;
import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.TaskStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A unit of work within a project, optionally scoped to a sprint.
 *
 * <p>{@code requiredSkills} exists specifically so the workload/assignment scorer
 * in ai-service can match a task against candidate employees the same way
 * {@code Employee.skills} lets it match a person against a role.
 */
@Entity
@Table(name = "tasks",
        indexes = {
                @Index(name = "idx_tasks_project", columnList = "project_id"),
                @Index(name = "idx_tasks_sprint", columnList = "sprint_id"),
                @Index(name = "idx_tasks_assignee", columnList = "assignee_id"),
                @Index(name = "idx_tasks_status", columnList = "status"),
                @Index(name = "idx_tasks_priority", columnList = "priority"),
                @Index(name = "idx_tasks_due_date", columnList = "due_date"),
                @Index(name = "idx_tasks_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
public class Task extends AuditableEntity {

    @NotNull(message = "Project is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_tasks_project"))
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_tasks_sprint"))
    private Sprint sprint;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 4000)
    @Column(name = "description", length = 4000)
    private String description;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "assignee_id", length = 36)
    private UUID assigneeId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "reporter_id", length = 36)
    private UUID reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.BACKLOG;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "estimated_hours", precision = 6, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal actualHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_mode", nullable = false, length = 10)
    private AssignmentMode assignmentMode = AssignmentMode.MANUAL;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_required_skills",
            joinColumns = @JoinColumn(name = "task_id"),
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_task_required_skills_task"))
    @Column(name = "skill", length = 60, nullable = false)
    private Set<String> requiredSkills = new LinkedHashSet<>();

    public boolean isOverdue() {
        return dueDate != null && !status.isTerminal() && dueDate.isBefore(LocalDate.now());
    }
}
