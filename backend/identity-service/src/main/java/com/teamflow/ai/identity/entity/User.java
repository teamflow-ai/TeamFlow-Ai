package com.teamflow.ai.identity.entity;

import com.teamflow.ai.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Login credentials and account state.
 *
 * <p>Migrated from the pre-merge {@code Long} identifier to the platform-standard
 * UUID inherited from {@code BaseEntity}. The password column stores a BCrypt hash
 * only; no code path outside {@code AuthServiceImpl} ever writes to it.
 *
 * <p>Account lockout state lives here rather than in a cache so that a restart
 * cannot be used to clear a lockout.
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_users_role", columnList = "role_id"),
                @Index(name = "idx_users_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
public class User extends AuditableEntity {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150)
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /** BCrypt hash. Never logged, never serialized into any DTO. */
    @NotBlank
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @NotNull(message = "Role is required")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_users_role"))
    private Role role;

    /** The HR record this login belongs to; absent for platform-level admins. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_users_employee"))
    private Employee employee;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    /** Consecutive failed attempts; reset to zero on any successful authentication. */
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    /** Non-null while the account is locked out; compared against now on each attempt. */
    @Column(name = "lockout_expires_at")
    private Instant lockoutExpiresAt;

    /** True while a lockout window is still in the future. */
    public boolean isLocked() {
        return lockoutExpiresAt != null && lockoutExpiresAt.isAfter(Instant.now());
    }

    public void registerSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lockoutExpiresAt = null;
        this.lastLoginAt = Instant.now();
    }
}
