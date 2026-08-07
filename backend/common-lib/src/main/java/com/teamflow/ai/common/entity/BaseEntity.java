package com.teamflow.ai.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Root persistence type for every MySQL-backed entity in TeamFlow.AI.
 *
 * <p>Provides a UUID surrogate key, an optimistic-locking version counter and a
 * soft-delete flag. Concrete entities should extend {@link AuditableEntity}
 * rather than this class unless audit columns are genuinely unwanted.
 *
 * <p>{@code equals} and {@code hashCode} are deliberately identifier-based. Using
 * Lombok {@code @Data} on a JPA entity generates field-based equality that touches
 * lazy associations and breaks Hibernate identity semantics across persistence
 * contexts, so it is never used on entities in this codebase.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    /**
     * Stored as {@code CHAR(36)} rather than Hibernate's MySQL default of
     * {@code BINARY(16)}. The binary form is unreadable in a SQL console and cannot
     * be pasted into a URL during a demo or support call; the extra 20 bytes per row
     * is a worthwhile trade for legibility, and it keeps the hand-written Flyway
     * DDL aligned with what Hibernate expects.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private UUID id;

    /** Optimistic locking guard; incremented by Hibernate on every flush. */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /** Soft-delete marker. Queries filter on this rather than issuing DELETE. */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    /**
     * Identity-based equality that is stable across the transient/persistent
     * boundary. Two entities are equal only when both carry the same non-null id
     * and share a compatible proxy-unwrapped type.
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BaseEntity that)) {
            return false;
        }
        return id != null && id.equals(that.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass().getSimpleName());
    }
}
