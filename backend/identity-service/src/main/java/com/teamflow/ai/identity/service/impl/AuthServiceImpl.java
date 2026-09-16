package com.teamflow.ai.identity.service.impl;

import com.teamflow.ai.common.constant.RoleNames;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.DuplicateResourceException;
import com.teamflow.ai.common.exception.ErrorCode;
import com.teamflow.ai.common.exception.InvalidTokenException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.common.security.JwtProperties;
import com.teamflow.ai.common.security.JwtService;
import com.teamflow.ai.identity.dto.request.ChangePasswordRequest;
import com.teamflow.ai.identity.dto.request.LoginRequest;
import com.teamflow.ai.identity.dto.request.RefreshTokenRequest;
import com.teamflow.ai.identity.dto.request.RegisterRequest;
import com.teamflow.ai.identity.dto.response.AuthResponse;
import com.teamflow.ai.identity.dto.response.UserResponse;
import com.teamflow.ai.identity.entity.Employee;
import com.teamflow.ai.identity.entity.RefreshToken;
import com.teamflow.ai.identity.entity.Role;
import com.teamflow.ai.identity.entity.User;
import com.teamflow.ai.identity.mapper.UserMapper;
import com.teamflow.ai.identity.repository.EmployeeRepository;
import com.teamflow.ai.identity.repository.RefreshTokenRepository;
import com.teamflow.ai.identity.repository.RoleRepository;
import com.teamflow.ai.identity.repository.UserRepository;
import com.teamflow.ai.identity.service.AuthService;
import com.teamflow.ai.identity.util.EmployeeCodeGenerator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Core authentication logic.
 *
 * <p>Several behaviours here are security decisions rather than incidental choices:
 *
 * <ul>
 *   <li><b>Uniform failure messages.</b> A wrong email and a wrong password return
 *       exactly the same error, so the endpoint cannot be used to enumerate which
 *       addresses hold accounts.</li>
 *   <li><b>Lockout after repeated failures.</b> Counters live in the database, not
 *       in memory, so a restart cannot clear a lockout and it holds across instances.</li>
 *   <li><b>Refresh-token rotation.</b> Redeeming a refresh token revokes it and
 *       issues a new one. A replayed token is therefore already revoked, which turns
 *       theft into a detectable, single-use event.</li>
 *   <li><b>Hashed token storage.</b> Only a SHA-256 digest is persisted, so a
 *       database disclosure yields no usable credentials.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;
    private static final String GENERIC_LOGIN_FAILURE = "Invalid email or password";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeCodeGenerator employeeCodeGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(email)) {
            throw DuplicateResourceException.of("User", "email", email);
        }

        String roleName = request.roleName() == null || request.roleName().isBlank()
                ? RoleNames.DEVELOPER
                : request.roleName().trim().toUpperCase();

        Role role = roleRepository.findByNameWithPermissions(roleName)
                .orElseThrow(() -> ResourceNotFoundException.of("Role", roleName));

        // A SUPER_ADMIN cannot be minted through the public endpoint; that would let
        // anyone escalate to full platform control simply by naming the role.
        if (RoleNames.SUPER_ADMIN.equals(role.getName())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "SUPER_ADMIN accounts cannot be created through self-registration");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setPasswordChangedAt(Instant.now());

        // The HR record is created alongside the login so the user immediately
        // appears in workload and assignment calculations.
        Employee employee = new Employee();
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setWorkEmail(email);
        employee.setEmployeeCode(employeeCodeGenerator.next());
        employee.setActive(true);
        user.setEmployee(employeeRepository.save(employee));

        User saved = userRepository.save(user);
        log.info("Registered user {} with role {}", saved.getId(), role.getName());

        return issueTokens(saved, null, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String userAgent, String ipAddress) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findActiveByEmailWithRole(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATION_FAILED, GENERIC_LOGIN_FAILURE));

        if (user.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED,
                    "Account is temporarily locked due to repeated failed sign-in attempts. Try again later.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, GENERIC_LOGIN_FAILURE);
        }

        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED,
                    "This account has been deactivated. Contact your administrator.");
        }

        user.registerSuccessfulLogin();
        userRepository.save(user);
        log.info("User {} signed in", user.getId());

        return issueTokens(user, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String userAgent, String ipAddress) {
        Claims claims = jwtService.parseClaims(request.refreshToken());
        if (!jwtService.isRefreshToken(claims)) {
            throw new InvalidTokenException("Provided token is not a refresh token");
        }

        String hash = hashToken(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHashWithUser(hash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is not recognised"));

        if (!stored.isUsable()) {
            // Presenting an already-revoked token suggests replay; drop every session
            // for that user rather than merely refusing this one request.
            log.warn("Revoked or expired refresh token replayed for user {}", stored.getUser().getId());
            refreshTokenRepository.revokeAllForUser(stored.getUser().getId());
            throw new InvalidTokenException("Refresh token has expired or been revoked");
        }

        // Rotation: the redeemed token is single-use.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = userRepository.findActiveByIdWithRole(stored.getUser().getId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", stored.getUser().getId()));

        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "This account has been deactivated");
        }

        return issueTokens(user, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        String hash = hashToken(refreshToken);
        refreshTokenRepository.findByTokenHashWithUser(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Session ended for user {}", token.getUser().getId());
        });
        // Absent tokens are ignored: logout is idempotent and must not reveal
        // whether a given token ever existed.
    }

    @Override
    @Transactional
    public void logoutAllDevices(UUID userId) {
        int revoked = refreshTokenRepository.revokeAllForUser(userId);
        log.info("Revoked {} active session(s) for user {}", revoked, userId);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findActiveByIdWithRole(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException("New password must differ from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        // Any session established with the old credential is no longer trustworthy.
        refreshTokenRepository.revokeAllForUser(userId);
        log.info("Password changed for user {}; all sessions revoked", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findActiveByIdWithRole(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return userMapper.toResponse(user);
    }

    // ------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------

    private AuthResponse issueTokens(User user, String userAgent, String ipAddress) {
        List<String> permissions = user.getRole().getPermissions().stream()
                .map(permission -> permission.getName())
                .toList();

        UUID employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().getName(), permissions, employeeId);
        String refreshTokenValue = jwtService.generateRefreshToken(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(refreshTokenValue));
        refreshToken.setIssuedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpiration()));
        refreshToken.setUserAgent(truncate(userAgent, 255));
        refreshToken.setIpAddress(truncate(ipAddress, 45));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration().toSeconds())
                .user(userMapper.toResponse(user))
                .build();
    }

    private void registerFailedAttempt(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockoutExpiresAt(Instant.now().plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES));
            log.warn("User {} locked out after {} failed attempts", user.getId(), user.getFailedLoginAttempts());
        }
        userRepository.save(user);
    }

    /**
     * SHA-256 rather than BCrypt for token storage. The token is already 128+ bits
     * of unguessable entropy, so the slow-hash property BCrypt provides against
     * dictionary attack buys nothing, while its cost would be paid on every request.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable in this JVM", ex);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
