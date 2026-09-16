package com.teamflow.ai.common.exception;

import lombok.Getter;

/**
 * Base type for all deliberately-thrown application errors.
 *
 * <p>Carrying an {@link ErrorCode} lets the global handler derive both the HTTP
 * status and the machine-readable code without a chain of instanceof checks.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public BusinessException(String message) {
        this(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
