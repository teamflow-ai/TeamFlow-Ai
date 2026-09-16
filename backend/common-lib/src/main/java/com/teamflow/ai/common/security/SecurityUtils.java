package com.teamflow.ai.common.security;

import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/** Static accessors for the current principal, for use inside service methods. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AuthenticatedUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /** @throws BusinessException when invoked outside an authenticated request */
    public static AuthenticatedUser requireCurrentUser() {
        return getCurrentUser().orElseThrow(() ->
                new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "No authenticated user in context"));
    }

    public static UUID requireCurrentUserId() {
        return requireCurrentUser().userId();
    }

    /**
     * The employee record backing the caller's login.
     *
     * @throws BusinessException if the caller has no linked employee record (e.g. a
     *      platform admin account created without one)
     */
    public static UUID requireCurrentEmployeeId() {
        UUID employeeId = requireCurrentUser().employeeId();
        if (employeeId == null) {
            throw new BusinessException("This account has no linked employee record");
        }
        return employeeId;
    }
}
