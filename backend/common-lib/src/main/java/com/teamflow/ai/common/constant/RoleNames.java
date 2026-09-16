package com.teamflow.ai.common.constant;

/**
 * Canonical role names.
 *
 * <p>Roles themselves are rows in the {@code roles} table so administrators can
 * re-map permissions at runtime. These constants exist only so compile-time
 * references in {@code @PreAuthorize} expressions and seed data cannot drift from
 * the seeded values through a typo.
 */
public final class RoleNames {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String CEO = "CEO";
    public static final String ADMIN = "ADMIN";
    public static final String PROJECT_MANAGER = "PROJECT_MANAGER";
    public static final String TEAM_LEAD = "TEAM_LEAD";
    public static final String DEVELOPER = "DEVELOPER";
    public static final String QA = "QA";
    public static final String DEVOPS = "DEVOPS";
    public static final String HR = "HR";
    public static final String FINANCE = "FINANCE";
    public static final String SALES = "SALES";
    public static final String CLIENT = "CLIENT";

    private RoleNames() {
    }
}
