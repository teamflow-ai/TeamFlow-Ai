package com.teamflow.ai.identity.controller;

import com.teamflow.ai.common.dto.ApiResponse;
import com.teamflow.ai.common.security.SecurityUtils;
import com.teamflow.ai.identity.dto.request.ChangePasswordRequest;
import com.teamflow.ai.identity.dto.request.LoginRequest;
import com.teamflow.ai.identity.dto.request.RefreshTokenRequest;
import com.teamflow.ai.identity.dto.request.RegisterRequest;
import com.teamflow.ai.identity.dto.response.AuthResponse;
import com.teamflow.ai.identity.dto.response.UserResponse;
import com.teamflow.ai.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints.
 *
 * <p>Strictly a transport layer: bind, delegate, wrap. Every rule about lockout,
 * rotation and password policy lives in {@code AuthService}, so it stays testable
 * without a servlet container and reusable from non-HTTP callers.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, sign-in and token lifecycle")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Register a new user",
            description = "Creates a login and its matching employee record. "
                    + "Returns a token pair so the client can proceed straight to an authenticated session.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Registration successful", response));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Sign in",
            description = "Exchanges credentials for an access and refresh token pair. "
                    + "Repeated failures temporarily lock the account.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Signed in"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "Account locked")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest servletRequest) {
        AuthResponse response = authService.login(request,
                servletRequest.getHeader(HttpHeaders.USER_AGENT), resolveClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Sign-in successful", response));
    }

    @PostMapping("/refresh-token")
    @SecurityRequirements
    @Operation(summary = "Refresh the access token",
            description = "Rotates the refresh token: the presented token is revoked and a new pair issued.")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                                                  HttpServletRequest servletRequest) {
        AuthResponse response = authService.refreshToken(request,
                servletRequest.getHeader(HttpHeaders.USER_AGENT), resolveClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Sign out of the current session")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Signed out", null));
    }

    @PostMapping("/logout-all")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Sign out of every device",
            description = "Revokes all refresh tokens for the authenticated user.")
    public ResponseEntity<ApiResponse<Void>> logoutAll() {
        authService.logoutAllDevices(SecurityUtils.requireCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Signed out of all devices", null));
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password",
            description = "Verifies the current password, then revokes every existing session.")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.requireCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed. Please sign in again.", null));
    }

    @GetMapping("/profile")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Current user profile",
            description = "Returns the authenticated user with role and resolved permissions, "
                    + "for rebuilding navigation after a page reload.")
    public ResponseEntity<ApiResponse<UserResponse>> profile() {
        return ResponseEntity.ok(ApiResponse.success(
                authService.getProfile(SecurityUtils.requireCurrentUserId())));
    }

    /**
     * Resolves the caller's address, preferring the first hop in
     * {@code X-Forwarded-For} because the direct peer is the gateway, not the user.
     * Recorded for the login-history view only; never used for authorization, since
     * the header is client-supplied and trivially forged.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
