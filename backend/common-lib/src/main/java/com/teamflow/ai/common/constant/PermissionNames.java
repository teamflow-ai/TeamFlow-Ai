package com.teamflow.ai.common.constant;

/**
 * Canonical permission names used in {@code @PreAuthorize("hasAuthority(...)")}.
 *
 * <p>As with roles, the authoritative list lives in the {@code permissions} table;
 * these constants only guard against typos at the call site.
 */
public final class PermissionNames {

    public static final String CREATE_PROJECT = "CREATE_PROJECT";
    public static final String UPDATE_PROJECT = "UPDATE_PROJECT";
    public static final String DELETE_PROJECT = "DELETE_PROJECT";
    public static final String CREATE_EMPLOYEE = "CREATE_EMPLOYEE";
    public static final String UPDATE_EMPLOYEE = "UPDATE_EMPLOYEE";
    public static final String DELETE_EMPLOYEE = "DELETE_EMPLOYEE";
    public static final String ASSIGN_TASK = "ASSIGN_TASK";
    public static final String UPDATE_TASK = "UPDATE_TASK";
    public static final String DELETE_TASK = "DELETE_TASK";
    public static final String VIEW_REPORT = "VIEW_REPORT";
    public static final String GENERATE_REPORT = "GENERATE_REPORT";
    public static final String VIEW_ANALYTICS = "VIEW_ANALYTICS";
    public static final String APPROVE_LEAVE = "APPROVE_LEAVE";
    public static final String VIEW_FINANCE = "VIEW_FINANCE";
    public static final String MANAGE_USERS = "MANAGE_USERS";
    public static final String MANAGE_ROLES = "MANAGE_ROLES";
    public static final String MANAGE_DEPARTMENTS = "MANAGE_DEPARTMENTS";

    private PermissionNames() {
    }
}
