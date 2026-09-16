package com.teamflow.ai.common.enums;

/**
 * Kanban column / lifecycle state of a task.
 *
 * <p>Legal transitions are encoded here rather than in a service so the rule has a
 * single home and can be unit-tested in isolation.
 */
public enum TaskStatus {

    BACKLOG,
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    BLOCKED,
    DONE,
    CANCELLED;

    /** Terminal states accept no further transitions except reopening to TODO. */
    public boolean isTerminal() {
        return this == DONE || this == CANCELLED;
    }

    public boolean canTransitionTo(TaskStatus target) {
        if (this == target) {
            return false;
        }
        return switch (this) {
            case BACKLOG -> target == TODO || target == CANCELLED;
            case TODO -> target == IN_PROGRESS || target == BLOCKED || target == CANCELLED;
            case IN_PROGRESS -> target == IN_REVIEW || target == BLOCKED || target == TODO || target == CANCELLED;
            case IN_REVIEW -> target == DONE || target == IN_PROGRESS || target == BLOCKED;
            case BLOCKED -> target == TODO || target == IN_PROGRESS || target == CANCELLED;
            case DONE, CANCELLED -> target == TODO;
        };
    }
}
