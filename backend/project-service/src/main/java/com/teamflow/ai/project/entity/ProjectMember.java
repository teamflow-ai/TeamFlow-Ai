package com.teamflow.ai.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * A team member on a project, embedded in {@link Project#getMembers()}.
 *
 * <p>Not a top-level entity: membership has no lifecycle independent of the
 * project it belongs to, and nothing ever queries it except through its project.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectMember {

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "employee_id", length = 36, nullable = false)
    private UUID employeeId;

    @Column(name = "role_on_project", length = 50)
    private String roleOnProject;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "allocated_hours")
    private Integer allocatedHours;
}
