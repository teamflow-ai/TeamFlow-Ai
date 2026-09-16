package com.teamflow.ai.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Every coefficient the Project Health score uses, so — like the workload
 * scorer — the formula can be retuned by configuration alone. Starts at 100 and
 * loses points per problem signal; never gains points beyond that ceiling.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "teamflow.project-health")
public class ProjectHealthProperties {

    /** Points lost per delayed (overdue, still-active) task. */
    private double delayedTaskPenalty = 6.0;

    /** Points lost per blocked task. */
    private double blockedTaskPenalty = 8.0;

    /** Points lost per open bug, scaled by severity weight. */
    private double openBugPenalty = 4.0;

    /** Points lost per team member currently on approved leave. */
    private double leaveImpactPenalty = 3.0;

    /** Points lost for each 10 percentage points the busiest assignee exceeds the mean active-task share by. */
    private double workloadImbalancePenalty = 5.0;

    /** Points gained per 10% of tasks completed, up to this cap — rewards real delivery progress. */
    private double completionBonusCap = 15.0;
}
