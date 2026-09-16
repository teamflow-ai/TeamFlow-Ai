package com.teamflow.ai.project.messaging;

import com.teamflow.ai.common.constant.MessagingConstants;
import com.teamflow.ai.common.event.BugEvent;
import com.teamflow.ai.common.event.MeetingEvent;
import com.teamflow.ai.common.event.NotificationEvent;
import com.teamflow.ai.common.event.ProjectEvent;
import com.teamflow.ai.common.event.TaskEvent;
import com.teamflow.ai.project.entity.Bug;
import com.teamflow.ai.project.entity.Meeting;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publishes delivery-domain events onto the shared topic exchange.
 *
 * <p>ai-service is the primary consumer: it keeps its own read model of project,
 * task and bug state so the workload scorer and dashboards never need a
 * synchronous call back into this service. Publish failures are logged rather than
 * thrown, for the same reason documented on identity-service's equivalent
 * publisher — a lost event must never fail the request that triggered it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void projectCreated(Project project) {
        publishProject(MessagingConstants.PROJECT_CREATED, project);
    }

    public void projectUpdated(Project project) {
        publishProject(MessagingConstants.PROJECT_UPDATED, project);
    }

    public void projectCompleted(Project project) {
        publishProject(MessagingConstants.PROJECT_COMPLETED, project);
    }

    public void projectMemberAdded(Project project, UUID employeeId, Integer allocatedHours) {
        publishProjectMember(MessagingConstants.PROJECT_MEMBER_ADDED, project.getId(), employeeId, allocatedHours);
    }

    public void projectMemberRemoved(Project project, UUID employeeId) {
        publishProjectMember(MessagingConstants.PROJECT_MEMBER_REMOVED, project.getId(), employeeId, null);
    }

    public void taskCreated(Task task) {
        publishTask(MessagingConstants.TASK_CREATED, task);
    }

    public void taskAssigned(Task task) {
        publishTask(MessagingConstants.TASK_ASSIGNED, task);
    }

    public void taskStatusChanged(Task task) {
        String routingKey = task.getStatus().isTerminal()
                ? MessagingConstants.TASK_COMPLETED
                : MessagingConstants.TASK_STATUS_CHANGED;
        publishTask(routingKey, task);
    }

    public void bugReported(Bug bug) {
        publishBug(MessagingConstants.BUG_REPORTED, bug);
    }

    public void bugAssigned(Bug bug) {
        publishBug(MessagingConstants.BUG_ASSIGNED, bug);
    }

    public void bugResolved(Bug bug) {
        publishBug(MessagingConstants.BUG_RESOLVED, bug);
    }

    public void meetingCreated(Meeting meeting) {
        publishMeeting(MessagingConstants.MEETING_CREATED, meeting);
    }

    public void meetingUpdated(Meeting meeting) {
        publishMeeting(MessagingConstants.MEETING_UPDATED, meeting);
    }

    public void meetingCancelled(Meeting meeting) {
        publishMeeting(MessagingConstants.MEETING_CANCELLED, meeting);
    }

    public void notify(UUID recipientEmployeeId, String title, String body, String category, String targetUrl) {
        if (recipientEmployeeId == null) {
            return;
        }
        try {
            NotificationEvent event = NotificationEvent.of(recipientEmployeeId, title, body, category, targetUrl);
            rabbitTemplate.convertAndSend(MessagingConstants.TOPIC_EXCHANGE, event.routingKey(), event);
        } catch (Exception ex) {
            log.warn("Failed to publish notification '{}' for employee {}: {}", title, recipientEmployeeId,
                    ex.getMessage());
        }
    }

    // ------------------------------------------------------------------

    private void publishProject(String routingKey, Project project) {
        try {
            ProjectEvent event = ProjectEvent.of(routingKey, project.getId(), project.getName(),
                    project.getManagerId(), project.getClient() != null ? project.getClient().getId() : null,
                    project.getStatus(), project.getStartDate(), project.getEndDate());
            rabbitTemplate.convertAndSend(MessagingConstants.TOPIC_EXCHANGE, event.routingKey(), event);
        } catch (Exception ex) {
            log.warn("Failed to publish {} for project {}: {}", routingKey, project.getId(), ex.getMessage());
        }
    }

    private void publishProjectMember(String routingKey, UUID projectId, UUID employeeId, Integer allocatedHours) {
        try {
            com.teamflow.ai.common.event.ProjectMemberEvent event = 
                com.teamflow.ai.common.event.ProjectMemberEvent.of(routingKey, projectId, employeeId, allocatedHours);
            rabbitTemplate.convertAndSend(MessagingConstants.TOPIC_EXCHANGE, event.routingKey(), event);
        } catch (Exception ex) {
            log.warn("Failed to publish {} for project {} member {}: {}", routingKey, projectId, employeeId, ex.getMessage());
        }
    }

    private void publishTask(String routingKey, Task task) {
        try {
            Double estimatedHours = task.getEstimatedHours() != null ? task.getEstimatedHours().doubleValue() : null;
            Double actualHours = task.getActualHours() != null ? task.getActualHours().doubleValue() : null;
            TaskEvent event = TaskEvent.of(routingKey, task.getId(), task.getTitle(), task.getProject().getId(),
                    task.getSprint() != null ? task.getSprint().getId() : null, task.getAssigneeId(),
                    task.getStatus(), task.getPriority(), estimatedHours, actualHours, task.getDueDate());
            rabbitTemplate.convertAndSend(MessagingConstants.TOPIC_EXCHANGE, event.routingKey(), event);
        } catch (Exception ex) {
            log.warn("Failed to publish {} for task {}: {}", routingKey, task.getId(), ex.getMessage());
        }
    }

    private void publishBug(String routingKey, Bug bug) {
        try {
            BugEvent event = BugEvent.of(routingKey, bug.getId(), bug.getTitle(), bug.getProject().getId(),
                    bug.getAssigneeId(), bug.getStatus().name(), bug.getSeverity());
            rabbitTemplate.convertAndSend(MessagingConstants.TOPIC_EXCHANGE, event.routingKey(), event);
        } catch (Exception ex) {
            log.warn("Failed to publish {} for bug {}: {}", routingKey, bug.getId(), ex.getMessage());
        }
    }

    private void publishMeeting(String routingKey, Meeting meeting) {
        try {
            MeetingEvent event = MeetingEvent.of(routingKey, meeting.getId(), meeting.getTitle(),
                    meeting.getProject() != null ? meeting.getProject().getId() : null,
                    meeting.getScheduledAt(), meeting.getStatus().name());
            rabbitTemplate.convertAndSend(MessagingConstants.TOPIC_EXCHANGE, event.routingKey(), event);
        } catch (Exception ex) {
            log.warn("Failed to publish {} for meeting {}: {}", routingKey, meeting.getId(), ex.getMessage());
        }
    }
}
