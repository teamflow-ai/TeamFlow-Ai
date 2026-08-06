package com.teamflow.organization.exception;

/**
 * Thrown when attempting to create an organization whose unique {@code code}
 * or {@code name} already exists.
 */
public class DuplicateOrganizationException extends RuntimeException {

    public DuplicateOrganizationException(String message) {
        super(message);
    }

}
