package com.teamflow.ai.project.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import com.teamflow.ai.common.enums.MilestoneStatus;
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

import java.time.LocalDate;

/**
 * A significant checkpoint within a project's delivery timeline.
 *
 * <p>Moving from {@code READY_FOR_REVIEW} to {@code APPROVED}/{@code REJECTED}
 * happens exclusively through the Approval Workflow Engine
 * ({@code MilestoneApprovalHandler}) — never set directly by
 * {@code MilestoneServiceImpl} — so the milestone's status and its approval
 * history can never disagree.
 */
@Entity
@Table(name = "milestones",
        indexes = {
                @Index(name = "idx_milestones_project", columnList = "project_id"),
                @Index(name = "idx_milestones_status", columnList = "status"),
                @Index(name = "idx_milestones_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
public class Milestone extends AuditableEntity {

    @NotNull(message = "Project is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_milestones_project"))
    private Project project;

    @NotBlank(message = "Milestone title is required")
    @Size(max = 150)
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MilestoneStatus status = MilestoneStatus.PLANNED;
}
