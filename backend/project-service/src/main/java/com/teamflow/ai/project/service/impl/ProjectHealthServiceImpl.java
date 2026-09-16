package com.teamflow.ai.project.service.impl;

import com.teamflow.ai.common.enums.BugStatus;
import com.teamflow.ai.common.enums.HealthCategory;
import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.client.AiRecommendationClient;
import com.teamflow.ai.project.client.AssignmentCandidateResponse;
import com.teamflow.ai.project.client.TaskAssignmentRecommendationRequest;
import com.teamflow.ai.project.dto.response.ProjectHealthResponse;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.Task;
import com.teamflow.ai.project.repository.BugRepository;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.repository.TaskRepository;
import com.teamflow.ai.project.service.ProjectHealthService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectHealthServiceImpl implements ProjectHealthService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final BugRepository bugRepository;
    private final AiRecommendationClient aiRecommendationClient;

    private static final double DELAYED_TASK_PENALTY = 6.0;
    private static final double BLOCKED_TASK_PENALTY = 8.0;
    private static final double OPEN_BUG_PENALTY = 4.0;
    private static final double LEAVE_IMPACT_PENALTY = 3.0;
    private static final double WORKLOAD_IMBALANCE_PENALTY = 5.0;
    private static final double COMPLETION_BONUS_CAP = 15.0;

    private static final List<BugStatus> OPEN_BUG_STATUSES =
            Arrays.stream(BugStatus.values()).filter(BugStatus::isOpen).toList();

    @Override
    @Transactional(readOnly = true)
    public ProjectHealthResponse getHealth(UUID projectId) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", projectId));

        List<Task> allTasks = taskRepository.findByProjectIdAndDeletedFalse(projectId);
        List<Task> activeTasks = allTasks.stream().filter(t -> !t.getStatus().isTerminal()).toList();
        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long blockedTasks = activeTasks.stream().filter(t -> t.getStatus() == TaskStatus.BLOCKED).count();
        
        LocalDate today = LocalDate.now();
        long delayedTasks = activeTasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today)).count();

        long openBugs = bugRepository.countByProjectIdAndStatusInAndDeletedFalse(projectId, OPEN_BUG_STATUSES);

        long employeesOnLeave = 0;
        try {
            TaskAssignmentRecommendationRequest dummyRequest = new TaskAssignmentRecommendationRequest(
                    null, projectId, java.util.Set.of(), null, null, null);
            List<AssignmentCandidateResponse> candidates = aiRecommendationClient.getAssignmentCandidates(dummyRequest).getData();
            employeesOnLeave = candidates.stream().filter(AssignmentCandidateResponse::onLeaveToday).count();
        } catch (FeignException ex) {
            log.warn("ai-service unreachable while fetching leave status for project {}", projectId);
        }

        double imbalancePenalty = workloadImbalancePenalty(activeTasks);

        double score = 100.0
                - DELAYED_TASK_PENALTY * delayedTasks
                - BLOCKED_TASK_PENALTY * blockedTasks
                - OPEN_BUG_PENALTY * openBugs
                - LEAVE_IMPACT_PENALTY * employeesOnLeave
                - imbalancePenalty
                + completionBonus(completedTasks, allTasks.size());
        
        score = Math.max(0.0, Math.min(100.0, score));

        HealthCategory category = HealthCategory.fromScore(score);

        // Standard summary without AI
        String summary = String.format("Project health is %d%% (%s). %d of %d tasks done, %d delayed, %d blocked, %d open bug(s), %d team member(s) on leave today.",
                Math.round(score), category, completedTasks, allTasks.size(), delayedTasks, blockedTasks, openBugs, employeesOnLeave);

        return ProjectHealthResponse.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .healthScore(Math.round(score * 10.0) / 10.0)
                .category(category)
                .completedTasks(completedTasks)
                .totalTasks(allTasks.size())
                .delayedTasks(delayedTasks)
                .blockedTasks(blockedTasks)
                .openBugs(openBugs)
                .employeesOnLeave(employeesOnLeave)
                .summary(summary)
                .build();
    }

    private double workloadImbalancePenalty(List<Task> activeTasks) {
        Map<UUID, Long> byAssignee = activeTasks.stream()
                .filter(t -> t.getAssigneeId() != null)
                .collect(Collectors.groupingBy(Task::getAssigneeId, Collectors.counting()));
        if (byAssignee.size() < 2) {
            return 0.0;
        }
        double average = byAssignee.values().stream().mapToLong(Long::longValue).average().orElse(0);
        long max = byAssignee.values().stream().mapToLong(Long::longValue).max().orElse(0);
        if (average <= 0 || max <= average) {
            return 0.0;
        }
        double excessRatio = (max - average) / average;
        return Math.min(20.0, excessRatio * 10.0 * WORKLOAD_IMBALANCE_PENALTY / 5.0);
    }

    private double completionBonus(long completed, int total) {
        if (total == 0) {
            return 0.0;
        }
        double completionRate = (double) completed / total;
        return Math.min(COMPLETION_BONUS_CAP, completionRate * COMPLETION_BONUS_CAP);
    }
}
