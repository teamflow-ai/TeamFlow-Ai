package com.teamflow.ai.common.enums;

/**
 * Leave request state.
 *
 * <p>Two approval gates are modelled explicitly: a request sits in
 * {@link #MANAGER_APPROVED} until HR confirms it, matching the platform's
 * two-stage leave workflow.
 */
public enum LeaveStatus {
    PENDING,
    MANAGER_APPROVED,
    APPROVED,
    REJECTED,
    CANCELLED
}
