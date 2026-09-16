package com.teamflow.ai.project.service;

import com.teamflow.ai.common.enums.BugStatus;
import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.dto.response.EmployeeProductivityReportResponse;
import com.teamflow.ai.project.dto.response.ProjectReportResponse;
import com.teamflow.ai.project.dto.response.SprintReportResponse;
import com.teamflow.ai.project.dto.response.WorklogReportResponse;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.Sprint;
import com.teamflow.ai.project.entity.WorkLog;
import com.teamflow.ai.project.repository.BugRepository;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.repository.SprintRepository;
import com.teamflow.ai.project.repository.TaskRepository;
import com.teamflow.ai.project.repository.TaskStatusHistoryRepository;
import com.teamflow.ai.project.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Report generation for the four report types the product brief calls for that
 * this service owns the source data for: Project, Sprint, Worklog and Employee
 * Productivity. Department Report lives in identity-service instead, since
 * headcount and leave are identity-service's data, not this service's.
 *
 * <p>Every figure here is a direct aggregation query against the relational
 * database — the source of truth — rather than the eventually-consistent Mongo
 * read model ai-service uses for dashboards. A report a manager might export and
 * hand to a client or exec must reflect the database, not a snapshot that could
 * be a few seconds behind.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final List<BugStatus> OPEN_BUG_STATUSES =
            Arrays.stream(BugStatus.values()).filter(BugStatus::isOpen).toList();

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final BugRepository bugRepository;
    private final WorkLogRepository workLogRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final EmployeeLookupService employeeLookupService;

    @Transactional(readOnly = true)
    public ProjectReportResponse projectReport(UUID projectId) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", projectId));

        Map<String, Long> tasksByStatus = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status.name(), taskRepository.countByProjectIdAndStatusAndDeletedFalse(projectId, status));
        }
        long totalTasks = taskRepository.countByProjectIdAndDeletedFalse(projectId);
        long totalBugs = bugRepository.countByProjectIdAndDeletedFalse(projectId);
        long openBugs = bugRepository.countByProjectIdAndStatusInAndDeletedFalse(projectId, OPEN_BUG_STATUSES);

        LocalDate from = project.getStartDate() != null ? project.getStartDate() : LocalDate.of(2000, 1, 1);
        LocalDate to = project.getEndDate() != null ? project.getEndDate() : LocalDate.now();
        BigDecimal totalHours = workLogRepository.findAllByProjectIdAndDeletedFalseAndLogDateBetween(projectId, from, to)
                .stream().map(WorkLog::getHours).reduce(BigDecimal.ZERO, BigDecimal::add);

        return ProjectReportResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .teamSize(project.getMembers().size())
                .totalTasks(totalTasks)
                .tasksByStatus(tasksByStatus)
                .totalBugs(totalBugs)
                .openBugs(openBugs)
                .totalWorklogHours(totalHours)
                .build();
    }

    @Transactional(readOnly = true)
    public SprintReportResponse sprintReport(UUID sprintId) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(sprintId)
                .orElseThrow(() -> ResourceNotFoundException.of("Sprint", sprintId));

        Map<String, Long> tasksByStatus = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status.name(), taskRepository.countBySprintIdAndStatusAndDeletedFalse(sprintId, status));
        }
        long totalTasks = taskRepository.countBySprintIdAndDeletedFalse(sprintId);
        long completed = tasksByStatus.getOrDefault(TaskStatus.DONE.name(), 0L);

        return SprintReportResponse.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getName())
                .status(sprint.getStatus())
                .totalTasks(totalTasks)
                .tasksByStatus(tasksByStatus)
                .completedTasks(completed)
                .totalEstimatedHours(taskRepository.sumEstimatedHoursBySprintId(sprintId))
                .totalActualHours(taskRepository.sumActualHoursBySprintId(sprintId))
                .build();
    }

    @Transactional(readOnly = true)
    public WorklogReportResponse worklogReport(UUID projectId, LocalDate from, LocalDate to) {
        List<WorkLog> logs = workLogRepository.findAllByProjectIdAndDeletedFalseAndLogDateBetween(projectId, from, to);
        BigDecimal total = logs.stream().map(WorkLog::getHours).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<WorklogReportResponse.Entry> entries = logs.stream()
                .map(log -> WorklogReportResponse.Entry.builder()
                        .employeeId(log.getEmployeeId())
                        .employeeName(employeeLookupService.tryResolveName(log.getEmployeeId()))
                        .taskId(log.getTask() != null ? log.getTask().getId() : null)
                        .taskTitle(log.getTask() != null ? log.getTask().getTitle() : null)
                        .logDate(log.getLogDate())
                        .hours(log.getHours())
                        .notes(log.getNotes())
                        .build())
                .toList();

        return WorklogReportResponse.builder()
                .projectId(projectId).from(from).to(to).totalHours(total).entries(entries)
                .build();
    }

    @Transactional(readOnly = true)
    public EmployeeProductivityReportResponse employeeProductivityReport(UUID employeeId, LocalDate from, LocalDate to) {
        List<WorkLog> logs = workLogRepository.findAllByEmployeeIdAndDeletedFalseAndLogDateBetween(employeeId, from, to);
        BigDecimal totalHours = logs.stream().map(WorkLog::getHours).reduce(BigDecimal.ZERO, BigDecimal::add);

        Instant start = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        long tasksCompleted = taskStatusHistoryRepository.countByChangedByAndToStatusAndCreatedAtBetween(
                employeeId, TaskStatus.DONE, start, end);

        long days = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1);
        double averagePerDay = totalHours.doubleValue() / days;

        return EmployeeProductivityReportResponse.builder()
                .employeeId(employeeId)
                .employeeName(employeeLookupService.tryResolveName(employeeId))
                .from(from).to(to)
                .tasksCompleted(tasksCompleted)
                .totalHoursLogged(totalHours)
                .averageHoursPerDay(Math.round(averagePerDay * 100.0) / 100.0)
                .build();
    }
}
