package com.teamflow.ai.common.enums;

/** Banding applied to a project health score. */
public enum HealthCategory {

    EXCELLENT,
    GOOD,
    NEEDS_ATTENTION,
    CRITICAL;

    /** Maps a 0-100 score onto its band. */
    public static HealthCategory fromScore(double score) {
        if (score >= 80.0) {
            return EXCELLENT;
        }
        if (score >= 60.0) {
            return GOOD;
        }
        if (score >= 40.0) {
            return NEEDS_ATTENTION;
        }
        return CRITICAL;
    }
}
