package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.document.EmployeeProfile;
import com.teamflow.ai.ai.document.ProjectSnapshot;
import com.teamflow.ai.ai.document.TaskSnapshot;
import com.teamflow.ai.ai.dto.response.BurnoutAssessmentResponse;
import com.teamflow.ai.ai.provider.AiProvider;
import com.teamflow.ai.ai.repository.EmployeeProfileRepository;
import com.teamflow.ai.ai.repository.ProjectSnapshotRepository;
import com.teamflow.ai.ai.repository.TaskSnapshotRepository;
import com.teamflow.ai.common.enums.BurnoutRisk;
import com.teamflow.ai.common.event.NotificationEvent;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Burnout Detection (product brief Feature 6).
 *
 * <p>Deliberately does not recompute a separate score: {@link WorkloadScoringService}
 * already produces a {@code BurnoutRisk} band from active tasks, overdue count,
 * remaining hours and open bugs (see its javadoc) — recomputing an overlapping
 * "burnout score" here from the same inputs would let the two numbers drift apart
 * for no benefit. This service adds only what the workload scorer intentionally
 * doesn't do: actionable suggestions, a plain-language explanation, and notifying
 * the responsible project manager(s) at HIGH_RISK/CRITICAL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BurnoutDetectionService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final TaskSnapshotRepository taskSnapshotRepository;
    private final ProjectSnapshotRepository projectSnapshotRepository;
    private final WorkloadScoringService scoringService;
    private final AiProvider aiProvider;
    private final NotificationService notificationService;

    public BurnoutAssessmentResponse assess(UUID employeeId) {
        EmployeeProfile profile = employeeProfileRepository.findById(employeeId.toString())
                .orElseThrow(() -> ResourceNotFoundException.of("Employee", employeeId));

        WorkloadScoreResult result = scoringService.score(profile);
        List<String> suggestions = suggestionsFor(result.band());

        String reasoning = aiProvider.explain(
                "{employee}'s burnout risk is {risk}, driven by {active} active task(s), "
                        + "{overdue} overdue and {hours}h of estimated work remaining.",
                Map.of("employee", result.employeeName(), "risk", result.band(), "active", result.activeTaskCount(),
                        "overdue", result.overdueTaskCount(), "hours", Math.round(result.remainingEstimatedHours())));

        if (result.band() == BurnoutRisk.HIGH_RISK || result.band() == BurnoutRisk.CRITICAL) {
            notifyManagers(employeeId, result);
        }

        return BurnoutAssessmentResponse.builder()
                .employeeId(employeeId)
                .employeeName(result.employeeName())
                .risk(result.band())
                .workloadScore(result.score())
                .activeTaskCount(result.activeTaskCount())
                .overdueTaskCount(result.overdueTaskCount())
                .remainingEstimatedHours(result.remainingEstimatedHours())
                .suggestions(suggestions)
                .reasoning(reasoning)
                .build();
    }

    private List<String> suggestionsFor(BurnoutRisk risk) {
        return switch (risk) {
            case CRITICAL -> List.of("Reduce load immediately — reassign at least one active task",
                    "Extend the deadline on any non-critical task", "Check in with the employee directly");
            case HIGH_RISK -> List.of("Reduce load — consider reassigning a lower-priority task",
                    "Assign new work to another team member for now");
            case WARNING -> List.of("Monitor closely before assigning further work",
                    "Consider spacing out upcoming deadlines");
            case HEALTHY -> List.of("No action needed");
        };
    }

    private void notifyManagers(UUID employeeId, WorkloadScoreResult result) {
        List<TaskSnapshot> activeTasks = taskSnapshotRepository.findAllByAssigneeIdAndStatusIn(
                employeeId.toString(), WorkloadScoringService.ACTIVE_TASK_STATUSES);
        Set<String> projectIds = activeTasks.stream()
                .map(TaskSnapshot::getProjectId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());

        Set<String> managerIds = projectIds.stream()
                .map(projectSnapshotRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(ProjectSnapshot::getManagerId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        for (String managerId : managerIds) {
            try {
                notificationService.receive(NotificationEvent.of(UUID.fromString(managerId), "Burnout risk flagged",
                        "%s's burnout risk is now %s".formatted(result.employeeName(), result.band()),
                        "BURNOUT_RISK", "/employees/" + employeeId));
            } catch (Exception ex) {
                log.warn("Could not notify manager {} of burnout risk for {}: {}", managerId, employeeId, ex.getMessage());
            }
        }
    }
}
