package com.teamflow.ai.common.constant;

/**
 * Redis cache region names.
 *
 * <p>Only genuinely hot, read-dominated data is cached. Anything written more often
 * than it is read is deliberately absent, because the invalidation cost outweighs
 * the hit rate.
 */
public final class CacheNames {

    public static final String USER_BY_EMAIL = "userByEmail";
    public static final String USER_PERMISSIONS = "userPermissions";
    public static final String EMPLOYEE_SUMMARY = "employeeSummary";
    public static final String PROJECT_STATISTICS = "projectStatistics";
    public static final String DASHBOARD_METRICS = "dashboardMetrics";
    public static final String AI_RECOMMENDATIONS = "aiRecommendations";
    public static final String PROJECT_HEALTH = "projectHealth";

    private CacheNames() {
    }
}
