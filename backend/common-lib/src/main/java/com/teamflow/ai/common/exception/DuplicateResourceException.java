package com.teamflow.ai.common.exception;

/** Raised when creating a record would violate a natural-key uniqueness rule. */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }

    public static DuplicateResourceException of(String resource, String field, Object value) {
        return new DuplicateResourceException(
                "%s already exists with %s: %s".formatted(resource, field, value));
    }
}
