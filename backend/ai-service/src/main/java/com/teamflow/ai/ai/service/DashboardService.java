package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.dto.response.DashboardSummaryResponse;
import com.teamflow.ai.ai.dto.response.EmployeeWorkloadResponse;
import com.teamflow.ai.ai.repository.BugSnapshotRepository;
import com.teamflow.ai.ai.repository.EmployeeProfileRepository;
import com.teamflow.ai.ai.repository.MeetingSnapshotRepository;
import com.teamflow.ai.ai.repository.ProjectSnapshotRepository;
import com.teamflow.ai.ai.repository.TaskSnapshotRepository;
import com.teamflow.ai.common.enums.BugStatus;
import com.teamflow.ai.common.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Serves the dashboard tiles the product brief asks for.
 *
 * <p>Every figure is computed from the Mongo read model at request time rather
 * than a periodically refreshed cache, trading a little query cost for a
 * guarantee that the dashboard is never stale — appropriate at this company's
 * scale, where the collections involved are small.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int UPCOMING_DEADLINE_WINDOW_DAYS = 7;

    private final EmployeeProfileRepository employeeProfileRepository;
    private final ProjectSnapshotRepository projectSnapshotRepository;
    private final TaskSnapshotRepository taskSnapshotRepository;
    private final BugSnapshotRepository bugSnapshotRepository;
    private final MeetingSnapshotRepository meetingSnapshotRepository;
    private final WorkloadScoringService scoringService;

    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        long overloadedEmployees = employeeProfileRepository.findAllByActiveTrue().stream()
                .map(scoringService::score)
                .filter(scoringService::isOverloaded)
                .count();

        return DashboardSummaryResponse.builder()
                .totalEmployees(employeeProfileRepository.findAllByActiveTrue().size())
                .activeProjects(projectSnapshotRepository.countByStatus("ACTIVE"))
                .pendingTasks(taskSnapshotRepository.countByStatusIn(WorkloadScoringService.ACTIVE_TASK_STATUSES))
                .completedTasks(taskSnapshotRepository.countByStatus(TaskStatus.DONE.name()))
                .openBugs(bugSnapshotRepository.countByStatusIn(openBugStatuses()))
                .closedBugs(bugSnapshotRepository.countByStatusIn(closedBugStatuses()))
                .upcomingDeadlines(taskSnapshotRepository.findAllByStatusInAndDueDateBetween(
                        WorkloadScoringService.ACTIVE_TASK_STATUSES, today, today.plusDays(UPCOMING_DEADLINE_WINDOW_DAYS)).size())
                .todaysMeetings(meetingSnapshotRepository.findAllByScheduledAtBetween(startOfDay, endOfDay).size())
                .overloadedEmployees(overloadedEmployees)
                .build();
    }

    public List<EmployeeWorkloadResponse> getWorkloadBoard() {
        return employeeProfileRepository.findAllByActiveTrue().stream()
                .map(scoringService::score)
                .map(result -> EmployeeWorkloadResponse.builder()
                        .employeeId(result.employeeId())
                        .employeeName(result.employeeName())
                        .workloadScore(result.score())
                        .band(result.band())
                        .activeTaskCount(result.activeTaskCount())
                        .overdueTaskCount(result.overdueTaskCount())
                        .remainingEstimatedHours(result.remainingEstimatedHours())
                        .openBugCount(result.openBugCount())
                        .onLeaveToday(result.onLeaveToday())
                        .build())
                .sorted(Comparator.comparingDouble(EmployeeWorkloadResponse::workloadScore).reversed())
                .toList();
    }

    private List<String> openBugStatuses() {
        return Arrays.stream(BugStatus.values()).filter(BugStatus::isOpen).map(Enum::name).toList();
    }

    private List<String> closedBugStatuses() {
        return List.of(BugStatus.RESOLVED.name(), BugStatus.CLOSED.name());
    }
}
