package com.teamflow.ai.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Canonical catalogue of business error codes.
 *
 * <p>Codes are part of the public API contract: the React client branches on them,
 * so values must not be renamed once released.
 */
public enum ErrorCode {

    RESOURCE_NOT_FOUND("TF-404", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("TF-409", HttpStatus.CONFLICT),
    VALIDATION_FAILED("TF-400", HttpStatus.BAD_REQUEST),
    BUSINESS_RULE_VIOLATION("TF-422", HttpStatus.UNPROCESSABLE_ENTITY),
    AUTHENTICATION_FAILED("TF-401", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("TF-4011", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TF-4012", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("TF-4013", HttpStatus.LOCKED),
    ACCOUNT_DISABLED("TF-4014", HttpStatus.FORBIDDEN),
    ACCESS_DENIED("TF-403", HttpStatus.FORBIDDEN),
    CONCURRENT_MODIFICATION("TF-4091", HttpStatus.CONFLICT),
    FILE_UPLOAD_FAILED("TF-500F", HttpStatus.INTERNAL_SERVER_ERROR),
    REMOTE_SERVICE_ERROR("TF-502", HttpStatus.BAD_GATEWAY),
    MESSAGING_ERROR("TF-503", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR("TF-500", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
