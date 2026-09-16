package com.teamflow.ai.project.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.ProjectStatus;
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
import jakarta.persistence.UniqueConstraint;
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
 * A delivery engagement: the umbrella under which sprints, tasks, bugs and
 * meetings are organised.
 *
 * <p>{@code managerId} and member {@code employeeId}s are plain references into
 * identity-service's schema, never a JPA association — the two services do not
 * share a database.
 */
@Entity
@Table(name = "projects",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_projects_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_projects_manager", columnList = "manager_id"),
                @Index(name = "idx_projects_status", columnList = "status"),
                @Index(name = "idx_projects_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
public class Project extends AuditableEntity {

    @NotBlank(message = "Project name is required")
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Project code is required")
    @Size(max = 30)
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_projects_client"))
    private Client client;

    /** The employee accountable for delivery; enforced in the service layer since it references another schema. */
    @NotNull(message = "Project manager is required")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "manager_id", length = 36, nullable = false)
    private UUID managerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_project_members_project"))
    private Set<ProjectMember> members = new LinkedHashSet<>();
}
