package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.config.WorkloadProperties;
import com.teamflow.ai.ai.document.BugSnapshot;
import com.teamflow.ai.ai.document.EmployeeProfile;
import com.teamflow.ai.ai.document.LeaveSnapshot;
import com.teamflow.ai.ai.document.TaskSnapshot;
import com.teamflow.ai.ai.repository.BugSnapshotRepository;
import com.teamflow.ai.ai.repository.LeaveSnapshotRepository;
import com.teamflow.ai.ai.repository.TaskSnapshotRepository;
import com.teamflow.ai.common.enums.BugStatus;
import com.teamflow.ai.common.enums.BurnoutRisk;
import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * TeamFlow.AI's Intelligent Workload Management scorer.
 *
 * <p>Deliberately not machine learning: every input is a plain count or sum
 * pulled from the Mongo read model, every coefficient is a configured weight from
 * {@link WorkloadProperties}, and the formula is a single linear combination a
 * manager can be walked through line by line. That transparency is the point — see
 * the module Javadoc on {@link com.teamflow.ai.ai.provider.AiProvider}.
 *
 * <p>Because the score is always computed fresh from current snapshot state (never
 * cached), it automatically reflects the latest task assignment, completion,
 * priority change or work log the moment those events are consumed — satisfying
 * "no manual recalculation required" without needing a separate recompute step to
 * forget to call.
 */
@Service
@RequiredArgsConstructor
public class WorkloadScoringService {

    public static final List<String> ACTIVE_TASK_STATUSES = Arrays.stream(TaskStatus.values())
            .filter(status -> !status.isTerminal())
            .map(Enum::name)
            .toList();

    private static final List<String> OPEN_BUG_STATUSES = Arrays.stream(BugStatus.values())
            .filter(BugStatus::isOpen)
            .map(Enum::name)
            .toList();

    private final TaskSnapshotRepository taskSnapshotRepository;
    private final BugSnapshotRepository bugSnapshotRepository;
    private final LeaveSnapshotRepository leaveSnapshotRepository;
    private final com.teamflow.ai.ai.repository.ProjectMemberSnapshotRepository projectMemberSnapshotRepository;
    private final WorkloadProperties properties;

    public WorkloadScoreResult score(EmployeeProfile profile) {
        String employeeId = profile.getEmployeeId();
        List<TaskSnapshot> activeTasks = taskSnapshotRepository.findAllByAssigneeIdAndStatusIn(employeeId, ACTIVE_TASK_STATUSES);
        List<BugSnapshot> openBugs = bugSnapshotRepository.findAllByAssigneeIdAndStatusIn(employeeId, OPEN_BUG_STATUSES);
        boolean onLeaveToday = isOnLeave(employeeId, LocalDate.now());

        LocalDate today = LocalDate.now();
        int overdueCount = (int) activeTasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(today))
                .count();

        double remainingHours = activeTasks.stream()
                .mapToDouble(this::remainingHours)
                .sum();

        double priorityPoints = activeTasks.stream()
                .mapToDouble(task -> priorityWeight(task.getPriority()))
                .sum();

        double bugPoints = openBugs.stream()
                .mapToDouble(bug -> priorityWeight(bug.getSeverity()))
                .sum();

        int capacity = profile.getWeeklyCapacityHours() > 0
                ? profile.getWeeklyCapacityHours() : properties.getDefaultWeeklyCapacityHours();

        // New calculation logic: Capacity is based on project allocations
        List<com.teamflow.ai.ai.document.ProjectMemberSnapshot> allocations = projectMemberSnapshotRepository.findAllByEmployeeId(employeeId);
        double totalAllocatedHours = allocations.stream()
                .filter(a -> a.getAllocatedHours() != null)
                .mapToDouble(com.teamflow.ai.ai.document.ProjectMemberSnapshot::getAllocatedHours)
                .sum();
        
        // Cap the allocations to the actual capacity so remaining isn't negative
        double effectiveAllocated = Math.min(totalAllocatedHours, capacity);
        double remainingCapacity = capacity - effectiveAllocated;

        // If tasks exceed the remaining project allocation capacity, penalize heavily.
        double capacityPenalty = 0.0;
        if (remainingHours > remainingCapacity) {
            capacityPenalty = (remainingHours - remainingCapacity) * properties.getHoursWeight() * 2; // Heavy penalty
        }

        double raw = properties.getTaskWeight() * activeTasks.size()
                + properties.getPriorityWeight() * priorityPoints
                + properties.getOverduePenalty() * overdueCount
                + properties.getHoursWeight() * remainingHours
                + properties.getBugSeverityWeight() * bugPoints
                + capacityPenalty; // Apply the new penalty

        double capacityFactor = (double) properties.getDefaultWeeklyCapacityHours() / capacity;
        raw *= capacityFactor;

        double score = onLeaveToday ? properties.getMaxScore() : clamp(raw, 0.0, properties.getMaxScore());

        return new WorkloadScoreResult(
                UUID.fromString(employeeId), profile.getFullName(), score, BurnoutRisk.fromScore(score),
                activeTasks.size(), overdueCount, remainingHours, totalAllocatedHours, openBugs.size(), onLeaveToday);
    }

    public boolean isOverloaded(WorkloadScoreResult result) {
        return result.score() >= properties.getOverloadThreshold();
    }

    private boolean isOnLeave(String employeeId, LocalDate date) {
        List<LeaveSnapshot> approvedLeave = leaveSnapshotRepository.findAllByEmployeeId(employeeId);
        return approvedLeave.stream().anyMatch(leave ->
                !date.isBefore(leave.getStartDate()) && !date.isAfter(leave.getEndDate()));
    }

    private double remainingHours(TaskSnapshot task) {
        double estimated = task.getEstimatedHours() != null ? task.getEstimatedHours() : 0.0;
        double actual = task.getActualHours() != null ? task.getActualHours() : 0.0;
        return Math.max(0.0, estimated - actual);
    }

    private int priorityWeight(String priorityName) {
        try {
            return Priority.valueOf(priorityName).weight();
        } catch (IllegalArgumentException | NullPointerException ex) {
            return Priority.MEDIUM.weight();
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
