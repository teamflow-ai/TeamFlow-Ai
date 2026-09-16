package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.document.EmployeeProfile;
import com.teamflow.ai.ai.document.LeaveSnapshot;
import com.teamflow.ai.ai.document.MeetingSnapshot;
import com.teamflow.ai.ai.document.ProjectSnapshot;
import com.teamflow.ai.ai.document.TaskSnapshot;
import com.teamflow.ai.ai.dto.response.DailyManagerBriefResponse;
import com.teamflow.ai.ai.dto.response.ProjectHealthResponse;
import com.teamflow.ai.ai.client.ProjectServiceClient;
import com.teamflow.ai.ai.provider.AiProvider;
import com.teamflow.ai.ai.repository.EmployeeProfileRepository;
import com.teamflow.ai.ai.repository.LeaveSnapshotRepository;
import com.teamflow.ai.ai.repository.MeetingSnapshotRepository;
import com.teamflow.ai.ai.repository.ProjectSnapshotRepository;
import com.teamflow.ai.ai.repository.TaskSnapshotRepository;
import com.teamflow.ai.common.enums.HealthCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI Daily Manager Brief (product brief Feature 4).
 *
 * <p>Deliberately company-wide rather than filtered to one manager's own
 * projects: identity-service owns the manager→project/team mapping and this
 * service only holds the Mongo read model built from published events, which
 * does not include that org-chart relationship. A per-manager brief is a
 * straightforward filter to add later once that mapping is replicated too;
 * documented here rather than silently guessed at.
 *
 * <p>Composes {@link AlertService}, {@link WorkloadScoringService} and
 * {@link ProjectHealthService} rather than re-deriving any of their numbers —
 * every fact in the brief traces back to exactly one place that computes it.
 */
@Service
@RequiredArgsConstructor
public class DailyManagerBriefService {

    private final TaskSnapshotRepository taskSnapshotRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final LeaveSnapshotRepository leaveSnapshotRepository;
    private final MeetingSnapshotRepository meetingSnapshotRepository;
    private final ProjectSnapshotRepository projectSnapshotRepository;
    private final WorkloadScoringService scoringService;
    private final ProjectServiceClient projectServiceClient;
    private final AiProvider aiProvider;

    public DailyManagerBriefResponse getBrief() {
        LocalDate today = LocalDate.now();
        Instant now = Instant.now();
        Instant next24h = now.plus(java.time.Duration.ofHours(24));

        List<TaskSnapshot> dueToday = taskSnapshotRepository.findAllByStatusInAndDueDateBetween(
                WorkloadScoringService.ACTIVE_TASK_STATUSES, today, today);
        List<String> todaysDeadlines = dueToday.stream().map(TaskSnapshot::getTitle).toList();

        List<TaskSnapshot> blocked = taskSnapshotRepository.findAllByStatusIn(List.of("BLOCKED"));
        List<String> blockedTasks = blocked.stream().map(TaskSnapshot::getTitle).toList();

        List<EmployeeProfile> employees = employeeProfileRepository.findAllByActiveTrue();
        List<String> overloaded = employees.stream()
                .map(scoringService::score)
                .filter(scoringService::isOverloaded)
                .map(WorkloadScoreResult::employeeName)
                .toList();

        List<LeaveSnapshot> onLeave = leaveSnapshotRepository
                .findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
        List<String> employeesOnLeave = onLeave.stream()
                .map(l -> employeeProfileRepository.findById(l.getEmployeeId())
                        .map(EmployeeProfile::getFullName).orElse(l.getEmployeeId()))
                .toList();

        List<MeetingSnapshot> meetings = meetingSnapshotRepository.findAllByScheduledAtBetween(now, next24h);
        List<String> upcomingMeetings = meetings.stream()
                .map(m -> "%s (%s)".formatted(m.getTitle(),
                        m.getScheduledAt().atZone(ZoneId.systemDefault()).toLocalTime()))
                .toList();

        List<ProjectSnapshot> activeProjects = projectSnapshotRepository.findAllByStatus("ACTIVE");
        List<String> atRiskProjects = new ArrayList<>();
        for (ProjectSnapshot project : activeProjects) {
            ProjectHealthResponse health = projectServiceClient.getProjectHealth(UUID.fromString(project.getProjectId())).getData();
            if (health.category() == HealthCategory.NEEDS_ATTENTION || health.category() == HealthCategory.CRITICAL) {
                atRiskProjects.add("%s (%.0f%%, %s)".formatted(health.projectName(), health.healthScore(), health.category()));
            }
        }

        List<String> suggestedActions = buildSuggestedActions(dueToday.size(), blocked.size(), overloaded.size(), atRiskProjects.size());

        String summary = aiProvider.explain(
                "{deadlines} task(s) due today, {blocked} blocked, {overloaded} employee(s) overloaded, "
                        + "{leave} on leave, {risk} project(s) need attention.",
                Map.of("deadlines", dueToday.size(), "blocked", blocked.size(), "overloaded", overloaded.size(),
                        "leave", employeesOnLeave.size(), "risk", atRiskProjects.size()));

        return DailyManagerBriefResponse.builder()
                .todaysDeadlines(todaysDeadlines)
                .blockedTasks(blockedTasks)
                .overloadedEmployees(overloaded)
                .employeesOnLeaveToday(employeesOnLeave)
                .upcomingMeetings(upcomingMeetings)
                .atRiskProjects(atRiskProjects)
                .suggestedActions(suggestedActions)
                .summary(summary)
                .build();
    }

    private List<String> buildSuggestedActions(int dueToday, int blocked, int overloaded, int atRisk) {
        List<String> actions = new ArrayList<>();
        if (blocked > 0) {
            actions.add("Review and unblock %d blocked task(s)".formatted(blocked));
        }
        if (overloaded > 0) {
            actions.add("Rebalance workload for %d overloaded employee(s)".formatted(overloaded));
        }
        if (dueToday > 0) {
            actions.add("Confirm progress on %d task(s) due today".formatted(dueToday));
        }
        if (atRisk > 0) {
            actions.add("Check in on %d at-risk project(s)".formatted(atRisk));
        }
        if (actions.isEmpty()) {
            actions.add("No urgent action needed today");
        }
        return actions;
    }
}
