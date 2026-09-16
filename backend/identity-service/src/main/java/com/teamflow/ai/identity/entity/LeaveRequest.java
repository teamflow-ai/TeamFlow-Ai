package com.teamflow.ai.identity.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import com.teamflow.ai.common.enums.LeaveStatus;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * An employee's request for time off, moving through a two-stage approval.
 *
 * <p>Approval is deliberately two-step (manager, then HR) to mirror the platform's
 * documented leave workflow; {@link LeaveStatus#MANAGER_APPROVED} is the
 * intermediate state.
 */
@Entity
@Table(name = "leave_requests",
        indexes = {
                @Index(name = "idx_leave_requests_employee", columnList = "employee_id"),
                @Index(name = "idx_leave_requests_status", columnList = "status"),
                @Index(name = "idx_leave_requests_dates", columnList = "start_date,end_date")
        })
@Getter
@Setter
@NoArgsConstructor
public class LeaveRequest extends AuditableEntity {

    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_leave_requests_employee"))
    private Employee employee;

    @NotNull(message = "Leave type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 30)
    private LeaveType leaveType;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    @Size(max = 500)
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "manager_approver_id", length = 36)
    private UUID managerApproverId;

    @Column(name = "manager_approved_at")
    private Instant managerApprovedAt;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "hr_approver_id", length = 36)
    private UUID hrApproverId;

    @Column(name = "hr_approved_at")
    private Instant hrApprovedAt;

    @Size(max = 500)
    @Column(name = "decision_comment", length = 500)
    private String decisionComment;

    /** Inclusive day count; used to debit the employee's leave balance on approval. */
    public int getTotalDays() {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /** Categories of leave recognised by the platform. */
    public enum LeaveType {
        CASUAL,
        SICK,
        EARNED,
        MATERNITY,
        PATERNITY,
        BEREAVEMENT,
        UNPAID
    }
}
