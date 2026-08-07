package com.teamflow.ai.common.constant;

/**
 * RabbitMQ topology shared by producers and consumers.
 *
 * <p>A single topic exchange carries every domain event. Routing keys follow
 * {@code <aggregate>.<action>}, letting a consumer bind to one event
 * ({@code task.assigned}) or a whole aggregate ({@code task.*}) without the
 * producer knowing who listens.
 *
 * <p>Every queue is declared with a dead-letter binding onto {@link #DLX_EXCHANGE}
 * so that a message failing all retries is parked for inspection rather than
 * requeued forever.
 */
public final class MessagingConstants {

    public static final String TOPIC_EXCHANGE = "teamflow.events";
    public static final String DLX_EXCHANGE = "teamflow.events.dlx";
    public static final String DLQ_QUEUE = "teamflow.events.dlq";

    // ---- routing keys : identity ----
    public static final String EMPLOYEE_CREATED = "employee.created";
    public static final String EMPLOYEE_UPDATED = "employee.updated";
    public static final String EMPLOYEE_DELETED = "employee.deleted";
    public static final String LEAVE_APPROVED = "leave.approved";

    // ---- routing keys : project ----
    public static final String TASK_CREATED = "task.created";
    public static final String TASK_ASSIGNED = "task.assigned";
    public static final String TASK_COMPLETED = "task.completed";
    public static final String TASK_STATUS_CHANGED = "task.status_changed";
    public static final String PROJECT_CREATED = "project.created";
    public static final String PROJECT_UPDATED = "project.updated";
    public static final String PROJECT_COMPLETED = "project.completed";
    public static final String BUG_REPORTED = "bug.reported";
    public static final String BUG_ASSIGNED = "bug.assigned";
    public static final String BUG_RESOLVED = "bug.resolved";
    public static final String MEETING_CREATED = "meeting.created";
    public static final String MEETING_UPDATED = "meeting.updated";
    public static final String MEETING_CANCELLED = "meeting.cancelled";
    public static final String NOTIFICATION_CREATED = "notification.created";

    // ---- routing keys : ai ----
    public static final String AI_RECOMMENDATION_GENERATED = "ai.recommendation.generated";
    public static final String DASHBOARD_UPDATED = "dashboard.updated";
    public static final String REPORT_GENERATED = "report.generated";
    public static final String KNOWLEDGE_UPLOADED = "knowledge.uploaded";

    // ---- queues ----
    public static final String QUEUE_AI_EMPLOYEE_EVENTS = "ai.employee.events";
    public static final String QUEUE_AI_TASK_EVENTS = "ai.task.events";
    public static final String QUEUE_AI_PROJECT_EVENTS = "ai.project.events";
    public static final String QUEUE_AI_BUG_EVENTS = "ai.bug.events";
    public static final String QUEUE_AI_MEETING_EVENTS = "ai.meeting.events";
    public static final String QUEUE_AI_LEAVE_EVENTS = "ai.leave.events";
    public static final String QUEUE_PROJECT_EMPLOYEE_EVENTS = "project.employee.events";
    public static final String QUEUE_PROJECT_AI_RECOMMENDATIONS = "project.ai.recommendations";
    public static final String QUEUE_NOTIFICATION_EVENTS = "notification.events";

    private MessagingConstants() {
    }
}
