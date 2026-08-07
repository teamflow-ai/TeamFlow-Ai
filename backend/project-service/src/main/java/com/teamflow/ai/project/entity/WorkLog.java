package com.teamflow.ai.project.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** A single day's logged effort against a project, optionally against one task. */
@Entity
@Table(name = "work_logs", indexes = {
        @Index(name = "idx_work_logs_employee", columnList = "employee_id"),
        @Index(name = "idx_work_logs_task", columnList = "task_id"),
        @Index(name = "idx_work_logs_project", columnList = "project_id"),
        @Index(name = "idx_work_logs_log_date", columnList = "log_date")
})
@Getter
@Setter
@NoArgsConstructor
public class WorkLog extends AuditableEntity {

    @NotNull(message = "Employee is required")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "employee_id", length = 36, nullable = false)
    private UUID employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_work_logs_task"))
    private Task task;

    @NotNull(message = "Project is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_work_logs_project"))
    private Project project;

    @NotNull(message = "Log date is required")
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @NotNull(message = "Hours is required")
    @DecimalMin(value = "0.01", message = "Hours must be greater than zero")
    @DecimalMax(value = "24.0", message = "Hours cannot exceed 24 in a single day")
    @Column(name = "hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal hours;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;
}
