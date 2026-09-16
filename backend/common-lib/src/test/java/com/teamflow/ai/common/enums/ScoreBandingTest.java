package com.teamflow.ai.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Score banding boundaries")
class ScoreBandingTest {

    @Test
    @DisplayName("health bands are inclusive at their lower bound")
    void healthBoundaries() {
        assertEquals(HealthCategory.EXCELLENT, HealthCategory.fromScore(100.0));
        assertEquals(HealthCategory.EXCELLENT, HealthCategory.fromScore(80.0));
        assertEquals(HealthCategory.GOOD, HealthCategory.fromScore(79.9));
        assertEquals(HealthCategory.GOOD, HealthCategory.fromScore(60.0));
        assertEquals(HealthCategory.NEEDS_ATTENTION, HealthCategory.fromScore(59.9));
        assertEquals(HealthCategory.NEEDS_ATTENTION, HealthCategory.fromScore(40.0));
        assertEquals(HealthCategory.CRITICAL, HealthCategory.fromScore(39.9));
        assertEquals(HealthCategory.CRITICAL, HealthCategory.fromScore(0.0));
    }

    @Test
    @DisplayName("burnout risk rises with the score, unlike health")
    void burnoutBoundaries() {
        assertEquals(BurnoutRisk.HEALTHY, BurnoutRisk.fromScore(0.0));
        assertEquals(BurnoutRisk.HEALTHY, BurnoutRisk.fromScore(39.9));
        assertEquals(BurnoutRisk.WARNING, BurnoutRisk.fromScore(40.0));
        assertEquals(BurnoutRisk.HIGH_RISK, BurnoutRisk.fromScore(60.0));
        assertEquals(BurnoutRisk.CRITICAL, BurnoutRisk.fromScore(80.0));
        assertEquals(BurnoutRisk.CRITICAL, BurnoutRisk.fromScore(100.0));
    }

    @Test
    @DisplayName("priority weights are ordered so CRITICAL dominates workload maths")
    void priorityWeights() {
        assertEquals(1, Priority.LOW.weight());
        assertEquals(2, Priority.MEDIUM.weight());
        assertEquals(4, Priority.HIGH.weight());
        assertEquals(8, Priority.CRITICAL.weight());
    }
}
