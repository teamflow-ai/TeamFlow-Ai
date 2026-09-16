package com.teamflow.ai.common.security;

import com.teamflow.ai.common.exception.ErrorCode;
import com.teamflow.ai.common.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final String SECRET = "a-test-signing-secret-that-is-long-enough-for-hmac-sha256";

    private JwtService jwtService;
    private JwtProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer("teamflow.ai");
        properties.setAccessTokenExpiration(Duration.ofMinutes(15));
        properties.setRefreshTokenExpiration(Duration.ofDays(7));
        jwtService = new JwtService(properties);
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("refuses to start without a secret rather than defaulting to one")
        void rejectsMissingSecret() {
            JwtProperties blank = new JwtProperties();
            assertThrows(IllegalStateException.class, () -> new JwtService(blank));
        }

        @Test
        @DisplayName("refuses a secret too short for HMAC-SHA256")
        void rejectsShortSecret() {
            JwtProperties weak = new JwtProperties();
            weak.setSecret("too-short");
            assertThrows(IllegalStateException.class, () -> new JwtService(weak));
        }
    }

    @Nested
    @DisplayName("access tokens")
    class AccessTokens {

        @Test
        @DisplayName("round-trips every claim it was given")
        void roundTripsClaims() {
            UUID userId = UUID.randomUUID();
            UUID employeeId = UUID.randomUUID();
            List<String> permissions = List.of("CREATE_PROJECT", "ASSIGN_TASK");

            String token = jwtService.generateAccessToken(
                    userId, "ankit@teamflow.ai", "PROJECT_MANAGER", permissions, employeeId);
            Claims claims = jwtService.parseClaims(token);

            assertEquals(userId, jwtService.extractUserId(claims));
            assertEquals("ankit@teamflow.ai", jwtService.extractEmail(claims));
            assertEquals("PROJECT_MANAGER", jwtService.extractRole(claims));
            assertEquals(permissions, jwtService.extractPermissions(claims));
            assertEquals(employeeId, jwtService.extractEmployeeId(claims));
            assertTrue(jwtService.isAccessToken(claims));
            assertFalse(jwtService.isRefreshToken(claims));
        }

        @Test
        @DisplayName("tolerates a user with no linked employee record")
        void handlesNullOptionalClaims() {
            String token = jwtService.generateAccessToken(
                    UUID.randomUUID(), "admin@teamflow.ai", "SUPER_ADMIN", List.of(), null);
            Claims claims = jwtService.parseClaims(token);

            assertNull(jwtService.extractEmployeeId(claims));
            assertTrue(jwtService.extractPermissions(claims).isEmpty());
        }
    }

    @Nested
    @DisplayName("refresh tokens")
    class RefreshTokens {

        @Test
        @DisplayName("are typed distinctly so they cannot be used as access credentials")
        void typedAsRefresh() {
            Claims claims = jwtService.parseClaims(jwtService.generateRefreshToken(UUID.randomUUID()));
            assertTrue(jwtService.isRefreshToken(claims));
            assertFalse(jwtService.isAccessToken(claims));
        }

        @Test
        @DisplayName("carry no role or permission claims, since those may change before redemption")
        void carryNoAuthorityClaims() {
            Claims claims = jwtService.parseClaims(jwtService.generateRefreshToken(UUID.randomUUID()));
            assertNull(jwtService.extractRole(claims));
            assertTrue(jwtService.extractPermissions(claims).isEmpty());
        }

        @Test
        @DisplayName("are unique per issue, so each can be revoked independently")
        void uniquePerIssue() {
            UUID userId = UUID.randomUUID();
            assertFalse(jwtService.generateRefreshToken(userId)
                    .equals(jwtService.generateRefreshToken(userId)));
        }
    }

    @Nested
    @DisplayName("verification")
    class Verification {

        @Test
        @DisplayName("rejects a token signed with a different secret")
        void rejectsForeignSignature() {
            JwtProperties other = new JwtProperties();
            other.setSecret("a-completely-different-secret-key-of-sufficient-length");
            other.setIssuer("teamflow.ai");
            String foreign = new JwtService(other)
                    .generateAccessToken(UUID.randomUUID(), "e@x.io", "DEVELOPER", List.of(), null);

            assertThrows(InvalidTokenException.class, () -> jwtService.parseClaims(foreign));
            assertFalse(jwtService.isTokenValid(foreign));
        }

        @Test
        @DisplayName("reports expiry with a distinct error code so clients know to refresh")
        void reportsExpiryDistinctly() {
            JwtProperties expiring = new JwtProperties();
            expiring.setSecret(SECRET);
            expiring.setIssuer("teamflow.ai");
            expiring.setAccessTokenExpiration(Duration.ofSeconds(-1));
            String expired = new JwtService(expiring)
                    .generateAccessToken(UUID.randomUUID(), "e@x.io", "DEVELOPER", List.of(), null);

            InvalidTokenException ex =
                    assertThrows(InvalidTokenException.class, () -> jwtService.parseClaims(expired));
            assertEquals(ErrorCode.TOKEN_EXPIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("rejects malformed input without throwing an unchecked parser error")
        void rejectsGarbage() {
            assertFalse(jwtService.isTokenValid("not-a-jwt"));
            assertFalse(jwtService.isTokenValid(""));
            assertThrows(InvalidTokenException.class, () -> jwtService.parseClaims("a.b.c"));
        }
    }
}
