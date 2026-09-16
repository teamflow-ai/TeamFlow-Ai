package com.teamflow.ai.common.enums;

/**
 * Shared priority scale for tasks and bugs.
 *
 * <p>{@link #weight()} feeds the AI workload calculations, where a CRITICAL item
 * must count for materially more than a LOW one when measuring employee load.
 */
public enum Priority {

    LOW(1),
    MEDIUM(2),
    HIGH(4),
    CRITICAL(8);

    private final int weight;

    Priority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }
}
