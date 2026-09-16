package com.teamflow.ai.common.enums;

/** Lifecycle state of a reported bug. */
public enum BugStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    REOPENED;

    public boolean isOpen() {
        return this == OPEN || this == IN_PROGRESS || this == REOPENED;
    }
}
