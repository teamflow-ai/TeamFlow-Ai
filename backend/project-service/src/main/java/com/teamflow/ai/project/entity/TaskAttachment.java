package com.teamflow.ai.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Metadata for a file attached to a task.
 *
 * <p>No binary content lives in this service; {@code fileUrl} points at wherever
 * the client uploaded the object. Storing only metadata keeps this service's
 * database small and avoids duplicating an object-storage integration here.
 */
@Entity
@Table(name = "task_attachments", indexes = {
        @Index(name = "idx_task_attachments_task", columnList = "task_id")
})
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TaskAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_task_attachments_task"))
    private Task task;

    @NotBlank(message = "File name is required")
    @Size(max = 255)
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank(message = "File URL is required")
    @Size(max = 1000)
    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "uploaded_by", length = 36, nullable = false)
    private UUID uploadedBy;
}
