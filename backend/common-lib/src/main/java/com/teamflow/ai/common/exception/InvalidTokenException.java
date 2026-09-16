package com.teamflow.ai.common.exception;

/** Raised when a JWT is malformed, tampered with, revoked or expired. */
public class InvalidTokenException extends BusinessException {

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }

    public InvalidTokenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
