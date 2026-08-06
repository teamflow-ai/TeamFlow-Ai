package com.teamflow.ai.common.security;

import com.teamflow.ai.common.exception.ErrorCode;
import com.teamflow.ai.common.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Issues and verifies the platform's JSON Web Tokens.
 *
 * <p>Lives in {@code common-lib} because every downstream service must verify
 * tokens using byte-identical logic, but only identity-service ever issues them.
 *
 * <p>Tokens carry the authenticated user's role and resolved permission list so
 * that project-service and ai-service can authorize requests locally, without a
 * synchronous call back to identity-service on every hop.
 */
@Slf4j
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_EMPLOYEE = "employeeId";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "teamflow.jwt.secret is not configured. Set it via the TEAMFLOW_JWT_SECRET environment variable.");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "teamflow.jwt.secret must be at least 32 bytes to satisfy HMAC-SHA256; got " + keyBytes.length);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Mints a short-lived access token.
     *
     * @param userId      subject of the token
     * @param email       user's login identifier
     * @param role        single role name, without the {@code ROLE_} prefix
     * @param permissions resolved permission names granted through that role
     * @param employeeId  linked employee record, may be {@code null} for platform admins with no HR record
     */
    public String generateAccessToken(UUID userId, String email, String role, List<String> permissions,
                                      UUID employeeId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(properties.getIssuer())
                .claim("email", email)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_PERMISSIONS, permissions)
                .claim(CLAIM_EMPLOYEE, employeeId != null ? employeeId.toString() : null)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getAccessTokenExpiration())))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Mints a refresh token. It carries no role or permission claims: those may
     * change before it is redeemed, so they are re-resolved from the database at
     * refresh time instead of being trusted from the token.
     */
    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(properties.getIssuer())
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getRefreshTokenExpiration())))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifies signature and expiry, returning the parsed claims.
     *
     * @throws InvalidTokenException when the token is expired, tampered with or malformed
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException(ErrorCode.TOKEN_EXPIRED, "Token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            // Never log the token itself - it is a bearer credential.
            log.warn("Rejected JWT: {}", ex.getClass().getSimpleName());
            throw new InvalidTokenException("Token is invalid");
        }
    }

    /** Non-throwing validity probe, used by filters that must not break the chain. */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (InvalidTokenException ex) {
            return false;
        }
    }

    public boolean isAccessToken(Claims claims) {
        return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    public String extractRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(Claims claims) {
        Object raw = claims.get(CLAIM_PERMISSIONS);
        return raw instanceof List<?> list ? (List<String>) list : List.of();
    }

    public UUID extractEmployeeId(Claims claims) {
        String value = claims.get(CLAIM_EMPLOYEE, String.class);
        return value == null ? null : UUID.fromString(value);
    }

    /** Exposes claims as a map for diagnostic endpoints; excludes nothing sensitive beyond the signature. */
    public Map<String, Object> asMap(Claims claims) {
        return Map.copyOf(claims);
    }
}
