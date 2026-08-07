package com.teamflow.ai.project.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.util.UUID;

/** A discussion comment left on a task. */
@Entity
@Table(name = "task_comments", indexes = {
        @Index(name = "idx_task_comments_task", columnList = "task_id")
})
@Getter
@Setter
@NoArgsConstructor
public class TaskComment extends AuditableEntity {

    @NotNull(message = "Task is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_task_comments_task"))
    private Task task;

    @NotNull(message = "Author is required")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "author_id", length = 36, nullable = false)
    private UUID authorId;

    @NotBlank(message = "Comment text is required")
    @Size(max = 2000)
    @Column(name = "comment", nullable = false, length = 2000)
    private String comment;
}
