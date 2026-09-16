package com.teamflow.ai.project.approval.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import com.teamflow.ai.common.enums.ApprovalStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * A single, generic approval decision awaiting (or having received) a verdict.
 *
 * <p>This is the one and only approval table in the platform. Every approvable
 * action — milestone sign-off, task completion, project closure, client
 * deliverable sign-off, and any future approval type — is a row here,
 * distinguished by {@link #approvalType}. Business-specific side effects (e.g.
 * moving a {@code Task} to {@code DONE}, or a {@code Project} to
 * {@code COMPLETED}) are never modelled as columns on this entity; they live in
 * an {@code ApprovalOutcomeHandler} for that type, keeping this table — and the
 * engine around it — genuinely reusable rather than a grab-bag of
 * type-specific fields that only apply to some rows.
 *
 * <p>Leave requests intentionally do not go through this table — see
 * {@link ApprovalType} for why.
 */
@Entity
@Table(name = "approval_requests",
        indexes = {
                @Index(name = "idx_approval_requests_type_status", columnList = "approval_type,status"),
                @Index(name = "idx_approval_requests_reference", columnList = "reference_entity_id"),
                @Index(name = "idx_approval_requests_project", columnList = "project_id"),
                @Index(name = "idx_approval_requests_approver", columnList = "approver_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class ApprovalRequest extends AuditableEntity {

    @NotNull(message = "Approval type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", nullable = false, length = 30)
    private ApprovalType approvalType;

    /** The id of the Milestone / Task / Project / deliverable this decision is about. */
    @NotNull(message = "Reference entity id is required")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "reference_entity_id", nullable = false, length = 36)
    private UUID referenceEntityId;

    /** The project this approval belongs to, for scoping and reporting. */
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "project_id", length = 36)
    private UUID projectId;

    @NotNull(message = "Requested by is required")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "requested_by", nullable = false, length = 36)
    private UUID requestedBy;

    /** Null until an approver is determined (e.g. by role) or picks the request up. */
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "approver_id", length = 36)
    private UUID approverId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Size(max = 1000)
    @Column(name = "remarks", length = 1000)
    private String remarks;

    @NotNull
    @Column(name = "requested_date", nullable = false)
    private Instant requestedDate = Instant.now();

    @Column(name = "approved_date")
    private Instant approvedDate;

    @Column(name = "rejected_date")
    private Instant rejectedDate;
}
