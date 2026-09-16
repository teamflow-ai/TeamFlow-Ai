package com.teamflow.ai.common.enums;

/** Lifecycle state of an {@code ApprovalRequest}. */
public enum ApprovalStatus {

    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    RETURNED_FOR_CHANGES,
    CANCELLED;

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED;
    }

    public boolean isDecidable() {
        return this == PENDING || this == UNDER_REVIEW;
    }
}
