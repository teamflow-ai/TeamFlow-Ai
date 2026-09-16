package com.teamflow.ai.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Tunable weights for the deterministic workload scorer.
 *
 * <p>None of the business rule is hard-coded in {@code WorkloadScoringService}:
 * every coefficient lives here, bound from {@code teamflow.workload.*}, so the
 * algorithm can be retuned by configuration alone as the platform learns what
 * "overloaded" actually means for this company.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "teamflow.workload")
public class WorkloadProperties {

    /** Points added per active (non-terminal) task assigned. */
    private double taskWeight = 8.0;

    /** Multiplier applied to each active task's {@code Priority.weight()}. */
    private double priorityWeight = 3.0;

    /** Extra points added per overdue active task. */
    private double overduePenalty = 15.0;

    /** Points added per estimated hour of remaining active work. */
    private double hoursWeight = 1.2;

    /** Points added per open bug assigned, scaled by the bug's severity weight. */
    private double bugSeverityWeight = 4.0;

    /** Score ceiling; the raw formula is clamped to [0, maxScore]. */
    private double maxScore = 100.0;

    /** Weekly capacity assumed when an employee's own value is unavailable. */
    private int defaultWeeklyCapacityHours = 40;

    /** Score at or above which an employee is flagged overloaded in alerts and recommendations. */
    private double overloadThreshold = 75.0;

    /** Number of high/critical-priority active tasks that triggers a "too many high-priority tasks" alert. */
    private int highPriorityTaskAlertThreshold = 3;

    /** How many days out a due date counts as an "approaching deadline". */
    private int deadlineWarningDays = 3;

    /** How many candidates the recommendation endpoint returns. */
    private int recommendationLimit = 5;
}
