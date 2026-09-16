package com.teamflow.ai.common.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Externalised JWT settings, bound from the {@code teamflow.jwt.*} namespace.
 *
 * <p>The secret has no default on purpose. A shipped default is the single most
 * common way signing keys leak into production, so services fail fast at startup
 * when it is absent rather than silently signing with a well-known value.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "teamflow.jwt")
public class JwtProperties {

    /** HMAC-SHA signing secret; must decode to at least 256 bits. */
    private String secret;

    /** Short-lived token presented on every request. */
    private Duration accessTokenExpiration = Duration.ofMinutes(15);

    /** Long-lived token exchanged for a new access token. */
    private Duration refreshTokenExpiration = Duration.ofDays(7);

    private String issuer = "teamflow.ai";
}
