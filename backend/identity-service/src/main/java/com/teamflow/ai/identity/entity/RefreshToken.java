package com.teamflow.ai.identity.entity;

import com.teamflow.ai.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A server-side record of an issued refresh token.
 *
 * <p>The JWT itself is stateless, so without this table logout would be impossible:
 * a stolen refresh token would remain valid until natural expiry. Persisting a row
 * per token allows single-device logout ({@code revoked = true}) and
 * logout-everywhere (revoke all rows for a user).
 *
 * <p>Only a SHA-256 hash of the token is stored. A database leak therefore does not
 * hand the attacker usable credentials, exactly as with passwords.
 */
@Entity
@Table(name = "refresh_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refresh_tokens_hash", columnNames = "token_hash")
        },
        indexes = {
                @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
                @Index(name = "idx_refresh_tokens_expires", columnList = "expires_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken extends BaseEntity {

    @NotBlank
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_refresh_tokens_user"))
    private User user;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    /** Recorded for the login-history view; never used for authorization. */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    public boolean isUsable() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }
}
