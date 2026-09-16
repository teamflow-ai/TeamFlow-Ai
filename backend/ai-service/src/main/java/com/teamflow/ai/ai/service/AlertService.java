package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.config.WorkloadProperties;
import com.teamflow.ai.ai.document.EmployeeProfile;
import com.teamflow.ai.ai.document.TaskSnapshot;
import com.teamflow.ai.ai.dto.response.AlertResponse;
import com.teamflow.ai.ai.repository.EmployeeProfileRepository;
import com.teamflow.ai.ai.repository.TaskSnapshotRepository;
import com.teamflow.ai.common.enums.BurnoutRisk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Computes manager-facing alerts on demand from the current Mongo read model.
 *
 * <p>Like {@link WorkloadScoringService}, nothing here is pre-computed or stored:
 * every call reflects the latest state, so an alert that was true five minutes ago
 * and has since been resolved (task reassigned, deadline pushed) simply stops
 * appearing — there is no stale alert to dismiss.
 */
@Service
@RequiredArgsConstructor
public class AlertService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final TaskSnapshotRepository taskSnapshotRepository;
    private final WorkloadScoringService scoringService;
    private final WorkloadProperties properties;

    public List<AlertResponse> getAlerts() {
        List<AlertResponse> alerts = new ArrayList<>();
        List<EmployeeProfile> employees = employeeProfileRepository.findAllByActiveTrue();

        alerts.addAll(overloadedEmployeeAlerts(employees));
        alerts.addAll(highPriorityLoadAlerts(employees));
        alerts.addAll(overdueTaskAlerts());
        alerts.addAll(deadlineApproachingAlerts());
        alerts.addAll(sprintImbalanceAlerts());
        return alerts;
    }

    private List<AlertResponse> overloadedEmployeeAlerts(List<EmployeeProfile> employees) {
        return employees.stream()
                .map(scoringService::score)
                .filter(scoringService::isOverloaded)
                .map(result -> AlertResponse.builder()
                        .type(AlertResponse.AlertType.OVERLOADED_EMPLOYEE)
                        .severity(result.band() == BurnoutRisk.CRITICAL
                                ? AlertResponse.Severity.CRITICAL : AlertResponse.Severity.WARNING)
                        .message("%s's workload score is %.0f/100 (%s)".formatted(
                                result.employeeName(), result.score(), result.band()))
                        .employeeId(result.employeeId())
                        .build())
                .toList();
    }

    private List<AlertResponse> highPriorityLoadAlerts(List<EmployeeProfile> employees) {
        List<AlertResponse> alerts = new ArrayList<>();
        for (EmployeeProfile employee : employees) {
            List<TaskSnapshot> active = taskSnapshotRepository.findAllByAssigneeIdAndStatusIn(
                    employee.getEmployeeId(), WorkloadScoringService.ACTIVE_TASK_STATUSES);
            long highPriorityCount = active.stream()
                    .filter(task -> "HIGH".equals(task.getPriority()) || "CRITICAL".equals(task.getPriority()))
                    .count();
            if (highPriorityCount >= properties.getHighPriorityTaskAlertThreshold()) {
                alerts.add(AlertResponse.builder()
                        .type(AlertResponse.AlertType.TOO_MANY_HIGH_PRIORITY_TASKS)
                        .severity(AlertResponse.Severity.WARNING)
                        .message("%s has %d high/critical priority tasks in flight at once"
                                .formatted(employee.getFullName(), highPriorityCount))
                        .employeeId(UUID.fromString(employee.getEmployeeId()))
                        .build());
            }
        }
        return alerts;
    }

    private List<AlertResponse> overdueTaskAlerts() {
        List<TaskSnapshot> overdue = taskSnapshotRepository.findAllByStatusInAndDueDateLessThan(
                WorkloadScoringService.ACTIVE_TASK_STATUSES, LocalDate.now());
        return overdue.stream()
                .map(task -> AlertResponse.builder()
                        .type(AlertResponse.AlertType.OVERDUE_TASK)
                        .severity(AlertResponse.Severity.CRITICAL)
                        .message("\"%s\" was due %s and is still open".formatted(task.getTitle(), task.getDueDate()))
                        .taskId(UUID.fromString(task.getTaskId()))
                        .projectId(task.getProjectId() != null ? UUID.fromString(task.getProjectId()) : null)
                        .employeeId(task.getAssigneeId() != null ? UUID.fromString(task.getAssigneeId()) : null)
                        .build())
                .toList();
    }

    private List<AlertResponse> deadlineApproachingAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(properties.getDeadlineWarningDays());
        List<TaskSnapshot> approaching = taskSnapshotRepository.findAllByStatusInAndDueDateBetween(
                WorkloadScoringService.ACTIVE_TASK_STATUSES, today, horizon);
        return approaching.stream()
                .map(task -> AlertResponse.builder()
                        .type(AlertResponse.AlertType.DEADLINE_APPROACHING)
                        .severity(AlertResponse.Severity.INFO)
                        .message("\"%s\" is due %s".formatted(task.getTitle(), task.getDueDate()))
                        .taskId(UUID.fromString(task.getTaskId()))
                        .projectId(task.getProjectId() != null ? UUID.fromString(task.getProjectId()) : null)
                        .employeeId(task.getAssigneeId() != null ? UUID.fromString(task.getAssigneeId()) : null)
                        .build())
                .toList();
    }

    /**
     * Flags a sprint where one assignee carries more than double the sprint's
     * average active-task load. A simple ratio rather than a statistical
     * dispersion measure, deliberately: it is easy to explain to a manager and
     * cheap to compute without a dedicated aggregation pipeline.
     */
    private List<AlertResponse> sprintImbalanceAlerts() {
        List<TaskSnapshot> allActive = taskSnapshotRepository.findAllByStatusIn(WorkloadScoringService.ACTIVE_TASK_STATUSES);
        Map<String, List<TaskSnapshot>> bySprint = allActive.stream()
                .filter(task -> task.getSprintId() != null)
                .collect(Collectors.groupingBy(TaskSnapshot::getSprintId));

        List<AlertResponse> alerts = new ArrayList<>();
        for (Map.Entry<String, List<TaskSnapshot>> entry : bySprint.entrySet()) {
            Map<String, Long> byAssignee = entry.getValue().stream()
                    .filter(task -> task.getAssigneeId() != null)
                    .collect(Collectors.groupingBy(TaskSnapshot::getAssigneeId, Collectors.counting()));
            if (byAssignee.size() < 2) {
                continue;
            }
            double average = byAssignee.values().stream().mapToLong(Long::longValue).average().orElse(0);
            byAssignee.entrySet().stream()
                    .max(Comparator.comparingLong(Map.Entry::getValue))
                    .filter(max -> average > 0 && max.getValue() > average * 2)
                    .ifPresent(max -> alerts.add(AlertResponse.builder()
                            .type(AlertResponse.AlertType.SPRINT_IMBALANCE)
                            .severity(AlertResponse.Severity.WARNING)
                            .message("One assignee holds %d of the sprint's active tasks versus an average of %.1f"
                                    .formatted(max.getValue(), average))
                            .employeeId(UUID.fromString(max.getKey()))
                            .build()));
        }
        return alerts;
    }
}
