package com.teamflow.ai.identity.service;

import com.teamflow.ai.identity.dto.request.ChangePasswordRequest;
import com.teamflow.ai.identity.dto.request.LoginRequest;
import com.teamflow.ai.identity.dto.request.RefreshTokenRequest;
import com.teamflow.ai.identity.dto.request.RegisterRequest;
import com.teamflow.ai.identity.dto.response.AuthResponse;
import com.teamflow.ai.identity.dto.response.UserResponse;

import java.util.UUID;

/** Authentication and credential lifecycle operations. */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String userAgent, String ipAddress);

    AuthResponse refreshToken(RefreshTokenRequest request, String userAgent, String ipAddress);

    /** Revokes one session. */
    void logout(String refreshToken);

    /** Revokes every live session for the user. */
    void logoutAllDevices(UUID userId);

    void changePassword(UUID userId, ChangePasswordRequest request);

    UserResponse getProfile(UUID userId);
}
