package com.teamflow.ai.identity.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** A department within the company. */
@Entity
@Table(name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_departments_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_departments_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
public class Department extends AuditableEntity {

    @NotBlank(message = "Department name is required")
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    /** Department head; a plain reference to avoid a cycle with {@link Employee}. */
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "head_employee_id", length = 36)
    private UUID headEmployeeId;

    /** Annual operating budget, used by the finance dashboard. */
    @Column(name = "annual_budget", precision = 15, scale = 2)
    private java.math.BigDecimal annualBudget;
}
