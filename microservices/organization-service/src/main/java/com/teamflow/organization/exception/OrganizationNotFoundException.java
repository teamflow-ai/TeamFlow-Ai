package com.teamflow.organization.exception;

/**
 * Thrown when an organization cannot be located by the given identifier or code.
 */
public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(String message) {
        super(message);
    }

}
