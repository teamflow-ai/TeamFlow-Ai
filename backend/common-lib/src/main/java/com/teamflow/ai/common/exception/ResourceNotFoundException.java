package com.teamflow.ai.common.exception;

/** Raised when a lookup by identifier or natural key yields nothing. */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    /** Convenience factory producing a consistent "Project not found with id: x" message. */
    public static ResourceNotFoundException of(String resource, Object identifier) {
        return new ResourceNotFoundException("%s not found with identifier: %s".formatted(resource, identifier));
    }
}
