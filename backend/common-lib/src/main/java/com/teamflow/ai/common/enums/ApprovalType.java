package com.teamflow.ai.common.enums;

/**
 * The kinds of business decision that flow through the generic Approval Workflow
 * Engine (see {@code project-service}'s {@code approval} package).
 *
 * <p>Leave requests are deliberately not modelled here: they already have a
 * bespoke two-stage (manager, then HR) approval in identity-service's
 * {@code LeaveRequest}/{@code LeaveService}, which is more specific than this
 * engine's single-decision model and works correctly today. Reusing existing
 * working business logic takes priority over forcing every approval through one
 * shape; this enum instead covers the approval types that had no implementation
 * before this engine was added, and it deliberately stays open — a new constant
 * plus one {@code ApprovalOutcomeHandler} implementation is all a future approval
 * type needs.
 */
public enum ApprovalType {

    /** A project manager marks a milestone ready; an admin confirms. */
    MILESTONE,

    /** An employee submits a task for review; the project manager confirms completion. */
    TASK_COMPLETION,

    /** A project manager requests project closure; an admin confirms. */
    PROJECT_CLOSURE,

    /**
     * A deliverable is sent to a client for sign-off. The CLIENT role has no
     * dashboard in this version of the platform (see product brief), so the
     * decision is recorded by the project manager or admin on the client's
     * behalf once received out of band (email, a client portal outside this
     * system's current scope, etc.) rather than through a client login.
     */
    CLIENT_APPROVAL
}
