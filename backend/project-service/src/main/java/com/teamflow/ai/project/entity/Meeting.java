package com.teamflow.ai.project.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import com.teamflow.ai.common.enums.MeetingStatus;
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

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** A scheduled meeting, optionally tied to a project. */
@Entity
@Table(name = "meetings", indexes = {
        @Index(name = "idx_meetings_project", columnList = "project_id"),
        @Index(name = "idx_meetings_scheduled_at", columnList = "scheduled_at"),
        @Index(name = "idx_meetings_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class Meeting extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_meetings_project"))
    private Project project;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 2000)
    @Column(name = "agenda", length = 2000)
    private String agenda;

    @NotNull(message = "Scheduled time is required")
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes = 30;

    @NotNull(message = "Organizer is required")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "organizer_id", length = 36, nullable = false)
    private UUID organizerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    @Size(max = 4000)
    @Column(name = "notes", length = 4000)
    private String notes;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "meeting_participants",
            joinColumns = @JoinColumn(name = "meeting_id"),
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_meeting_participants_meeting"))
    @Column(name = "employee_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private Set<UUID> participantIds = new LinkedHashSet<>();
}
