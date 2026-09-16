package com.teamflow.ai.common.dto;

/**
 * One field-level error detail carried inside {@link ApiResponse#getErrors()}.
 *
 * @param field   the offending property, or {@code null} for object-level errors
 * @param code    stable machine-readable error code for frontend branching
 * @param message human-readable description safe to surface to end users
 */
public record ApiError(String field, String code, String message) {

    public static ApiError of(String field, String message) {
        return new ApiError(field, "VALIDATION_ERROR", message);
    }
}
