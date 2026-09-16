package com.teamflow.ai.identity.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** A team within a department; the unit that work is normally allocated to. */
@Entity
@Table(name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_teams_department_name", columnNames = {"department_id", "name"})
        },
        indexes = {
                @Index(name = "idx_teams_department", columnList = "department_id"),
                @Index(name = "idx_teams_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
public class Team extends AuditableEntity {

    @NotBlank(message = "Team name is required")
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Department is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_teams_department"))
    private Department department;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "lead_employee_id", length = 36)
    private UUID leadEmployeeId;
}
