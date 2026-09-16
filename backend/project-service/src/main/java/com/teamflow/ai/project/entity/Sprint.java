package com.teamflow.ai.project.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import com.teamflow.ai.common.enums.SprintStatus;
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

/** A time-boxed iteration of work within a project. */
@Entity
@Table(name = "sprints",
        indexes = {
                @Index(name = "idx_sprints_project", columnList = "project_id"),
                @Index(name = "idx_sprints_status", columnList = "status"),
                @Index(name = "idx_sprints_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
public class Sprint extends AuditableEntity {

    @NotNull(message = "Project is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_sprints_project"))
    private Project project;

    @NotBlank(message = "Sprint name is required")
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 1000)
    @Column(name = "goal", length = 1000)
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SprintStatus status = SprintStatus.PLANNED;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
