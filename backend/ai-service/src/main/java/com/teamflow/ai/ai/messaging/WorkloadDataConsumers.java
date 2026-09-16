package com.teamflow.ai.ai.messaging;

import com.teamflow.ai.ai.document.BugSnapshot;
import com.teamflow.ai.ai.document.EmployeeProfile;
import com.teamflow.ai.ai.document.LeaveSnapshot;
import com.teamflow.ai.ai.document.MeetingSnapshot;
import com.teamflow.ai.ai.document.ProjectSnapshot;
import com.teamflow.ai.ai.document.TaskSnapshot;
import com.teamflow.ai.ai.repository.BugSnapshotRepository;
import com.teamflow.ai.ai.repository.EmployeeProfileRepository;
import com.teamflow.ai.ai.repository.LeaveSnapshotRepository;
import com.teamflow.ai.ai.repository.MeetingSnapshotRepository;
import com.teamflow.ai.ai.repository.ProjectSnapshotRepository;
import com.teamflow.ai.ai.repository.ProjectMemberSnapshotRepository;
import com.teamflow.ai.ai.repository.TaskSnapshotRepository;
import com.teamflow.ai.common.constant.MessagingConstants;
import com.teamflow.ai.common.event.BugEvent;
import com.teamflow.ai.common.event.EmployeeEvent;
import com.teamflow.ai.common.event.LeaveEvent;
import com.teamflow.ai.common.event.MeetingEvent;
import com.teamflow.ai.common.event.ProjectEvent;
import com.teamflow.ai.common.event.ProjectMemberEvent;
import com.teamflow.ai.common.event.TaskEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Keeps ai-service's Mongo read model in step with the rest of the platform.
 *
 * <p>Each handler is a plain upsert: the incoming event already carries the full
 * current state of the aggregate (see each event record's Javadoc), so there is
 * nothing to merge — the latest message always wins, which is also what makes
 * these handlers safe to replay after a redelivery.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadDataConsumers {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final ProjectSnapshotRepository projectSnapshotRepository;
    private final TaskSnapshotRepository taskSnapshotRepository;
    private final BugSnapshotRepository bugSnapshotRepository;
    private final MeetingSnapshotRepository meetingSnapshotRepository;
    private final LeaveSnapshotRepository leaveSnapshotRepository;
    private final ProjectMemberSnapshotRepository projectMemberSnapshotRepository;

    @RabbitListener(queues = MessagingConstants.QUEUE_AI_EMPLOYEE_EVENTS)
    public void onEmployeeEvent(EmployeeEvent event) {
        String id = event.employeeId().toString();
        if (MessagingConstants.EMPLOYEE_DELETED.equals(event.routingKey())) {
            employeeProfileRepository.deleteById(id);
            log.debug("Removed employee profile {}", id);
            return;
        }
        EmployeeProfile profile = employeeProfileRepository.findById(id).orElseGet(EmployeeProfile::new);
        profile.setEmployeeId(id);
        profile.setFullName(event.fullName());
        profile.setEmail(event.email());
        profile.setDepartmentId(event.departmentId() != null ? event.departmentId().toString() : null);
        profile.setSkills(event.skills());
        profile.setWeeklyCapacityHours(event.weeklyCapacityHours() != null ? event.weeklyCapacityHours() : 40);
        profile.setActive(true);
        profile.setUpdatedAt(Instant.now());
        employeeProfileRepository.save(profile);
        log.debug("Upserted employee profile {}", id);
    }

    @RabbitListener(queues = MessagingConstants.QUEUE_AI_PROJECT_EVENTS)
    public void onProjectEvent(ProjectEvent event) {
        String id = event.projectId().toString();
        ProjectSnapshot snapshot = projectSnapshotRepository.findById(id).orElseGet(ProjectSnapshot::new);
        snapshot.setProjectId(id);
        snapshot.setName(event.name());
        snapshot.setManagerId(event.managerId() != null ? event.managerId().toString() : null);
        snapshot.setClientId(event.clientId() != null ? event.clientId().toString() : null);
        snapshot.setStatus(event.status().name());
        snapshot.setStartDate(event.startDate());
        snapshot.setEndDate(event.endDate());
        snapshot.setUpdatedAt(Instant.now());
        projectSnapshotRepository.save(snapshot);
        log.debug("Upserted project snapshot {}", id);
    }

    @RabbitListener(queues = MessagingConstants.QUEUE_AI_PROJECT_EVENTS)
    public void onProjectMemberEvent(ProjectMemberEvent event) {
        String id = event.projectId().toString() + "_" + event.employeeId().toString();
        if (MessagingConstants.PROJECT_MEMBER_REMOVED.equals(event.routingKey())) {
            projectMemberSnapshotRepository.deleteById(id);
            log.debug("Removed project member snapshot {}", id);
            return;
        }
        com.teamflow.ai.ai.document.ProjectMemberSnapshot snapshot = projectMemberSnapshotRepository.findById(id).orElseGet(com.teamflow.ai.ai.document.ProjectMemberSnapshot::new);
        snapshot.setId(id);
        snapshot.setProjectId(event.projectId().toString());
        snapshot.setEmployeeId(event.employeeId().toString());
        snapshot.setAllocatedHours(event.allocatedHours());
        snapshot.setUpdatedAt(Instant.now());
        projectMemberSnapshotRepository.save(snapshot);
        log.debug("Upserted project member snapshot {}", id);
    }

    @RabbitListener(queues = MessagingConstants.QUEUE_AI_TASK_EVENTS)
    public void onTaskEvent(TaskEvent event) {
        String id = event.taskId().toString();
        TaskSnapshot snapshot = taskSnapshotRepository.findById(id).orElseGet(TaskSnapshot::new);
        snapshot.setTaskId(id);
        snapshot.setTitle(event.title());
        snapshot.setProjectId(event.projectId() != null ? event.projectId().toString() : null);
        snapshot.setSprintId(event.sprintId() != null ? event.sprintId().toString() : null);
        snapshot.setAssigneeId(event.assigneeId() != null ? event.assigneeId().toString() : null);
        snapshot.setStatus(event.status().name());
        snapshot.setPriority(event.priority().name());
        snapshot.setEstimatedHours(event.estimatedHours());
        snapshot.setActualHours(event.actualHours());
        snapshot.setDueDate(event.dueDate());
        snapshot.setUpdatedAt(Instant.now());
        taskSnapshotRepository.save(snapshot);
        log.debug("Upserted task snapshot {}", id);
    }

    @RabbitListener(queues = MessagingConstants.QUEUE_AI_BUG_EVENTS)
    public void onBugEvent(BugEvent event) {
        String id = event.bugId().toString();
        BugSnapshot snapshot = bugSnapshotRepository.findById(id).orElseGet(BugSnapshot::new);
        snapshot.setBugId(id);
        snapshot.setTitle(event.title());
        snapshot.setProjectId(event.projectId() != null ? event.projectId().toString() : null);
        snapshot.setAssigneeId(event.assigneeId() != null ? event.assigneeId().toString() : null);
        snapshot.setStatus(event.status());
        snapshot.setSeverity(event.severity().name());
        snapshot.setUpdatedAt(Instant.now());
        bugSnapshotRepository.save(snapshot);
        log.debug("Upserted bug snapshot {}", id);
    }

    @RabbitListener(queues = MessagingConstants.QUEUE_AI_MEETING_EVENTS)
    public void onMeetingEvent(MeetingEvent event) {
        String id = event.meetingId().toString();
        MeetingSnapshot snapshot = meetingSnapshotRepository.findById(id).orElseGet(MeetingSnapshot::new);
        snapshot.setMeetingId(id);
        snapshot.setTitle(event.title());
        snapshot.setProjectId(event.projectId() != null ? event.projectId().toString() : null);
        snapshot.setScheduledAt(event.scheduledAt());
        snapshot.setStatus(event.status());
        snapshot.setUpdatedAt(Instant.now());
        meetingSnapshotRepository.save(snapshot);
        log.debug("Upserted meeting snapshot {}", id);
    }

    @RabbitListener(queues = MessagingConstants.QUEUE_AI_LEAVE_EVENTS)
    public void onLeaveEvent(LeaveEvent event) {
        String id = event.leaveRequestId().toString();
        LeaveSnapshot snapshot = new LeaveSnapshot();
        snapshot.setLeaveRequestId(id);
        snapshot.setEmployeeId(event.employeeId().toString());
        snapshot.setStartDate(event.startDate());
        snapshot.setEndDate(event.endDate());
        leaveSnapshotRepository.save(snapshot);
        log.debug("Recorded approved leave {} for employee {}", id, event.employeeId());
    }
}
