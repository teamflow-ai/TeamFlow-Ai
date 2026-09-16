package com.teamflow.ai.common.enums;

/** Burnout classification produced by the AI wellbeing model. */
public enum BurnoutRisk {

    HEALTHY,
    WARNING,
    HIGH_RISK,
    CRITICAL;

    public static BurnoutRisk fromScore(double score) {
        if (score < 40.0) {
            return HEALTHY;
        }
        if (score < 60.0) {
            return WARNING;
        }
        if (score < 80.0) {
            return HIGH_RISK;
        }
        return CRITICAL;
    }
}
