package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.config.WorkloadProperties;
import com.teamflow.ai.ai.document.EmployeeProfile;
import com.teamflow.ai.ai.document.RecommendationHistory;
import com.teamflow.ai.ai.document.TaskSnapshot;
import com.teamflow.ai.ai.dto.request.TaskAssignmentRequest;
import com.teamflow.ai.ai.dto.response.ReassignmentSuggestionResponse;
import com.teamflow.ai.ai.dto.response.AssignmentCandidateResponse;
import com.teamflow.ai.ai.dto.response.TaskAssignmentRecommendationResponse;
import com.teamflow.ai.ai.provider.AiProvider;
import com.teamflow.ai.ai.repository.EmployeeProfileRepository;
import com.teamflow.ai.ai.repository.RecommendationHistoryRepository;
import com.teamflow.ai.ai.repository.TaskSnapshotRepository;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The Smart Task Assignment engine: turns a task's requirements into a ranked,
 * explainable shortlist of employees, and — for an already-overloaded employee —
 * suggests which of their tasks could move and to whom.
 *
 * <p>Nothing here is ever applied automatically. A manager always makes the final
 * call by calling project-service's {@code /tasks/{id}/assign}; this service only
 * ever advises.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final TaskSnapshotRepository taskSnapshotRepository;
    private final WorkloadScoringService scoringService;
    private final WorkloadProperties properties;
    private final AiProvider aiProvider;
    private final RecommendationHistoryRepository recommendationHistoryRepository;

    public List<TaskAssignmentRecommendationResponse> recommend(TaskAssignmentRequest request) {
        List<EmployeeProfile> candidates = employeeProfileRepository.findAllByActiveTrue();

        Set<String> requiredSkills = normalizedSkills(request.requiredSkills());
        List<EmployeeProfile> skillMatched = requiredSkills.isEmpty() ? candidates
                : candidates.stream()
                        .filter(profile -> !disjoint(normalizedSkills(profile.getSkills()), requiredSkills))
                        .toList();

        // A required skill nobody currently has shouldn't leave a manager with zero
        // candidates; fall back to the whole active roster rather than an empty list.
        List<EmployeeProfile> pool = skillMatched.isEmpty() ? candidates : skillMatched;

        List<TaskAssignmentRecommendationResponse> ranked = pool.stream()
                .map(profile -> toRecommendation(profile, requiredSkills))
                .sorted(Comparator.comparingDouble(TaskAssignmentRecommendationResponse::workloadScore))
                .limit(properties.getRecommendationLimit())
                .toList();

        if (request.taskId() != null) {
            recordHistory(request, ranked);
        }
        return ranked;
    }

    /** Feature 8: Recommendation History. Failure here must never break the recommendation response itself. */
    private void recordHistory(TaskAssignmentRequest request, List<TaskAssignmentRecommendationResponse> ranked) {
        try {
            RecommendationHistory history = new RecommendationHistory();
            history.setId(UUID.randomUUID().toString());
            history.setTaskId(request.taskId().toString());
            history.setAiProvider(aiProvider.name());
            history.setPromptMetadata(Map.of(
                    "priority", request.priority() != null ? request.priority().name() : "UNSET",
                    "requiredSkills", request.requiredSkills() != null ? request.requiredSkills() : Set.of(),
                    "dueDate", request.dueDate() != null ? request.dueDate().toString() : "UNSET"));
            history.setRecommendedEmployeeIds(ranked.stream()
                    .map(r -> r.employeeId().toString()).toList());
            history.setTopRecommendationEmployeeId(ranked.isEmpty() ? null : ranked.get(0).employeeId().toString());
            history.setEstimatedHours(request.estimatedHours() != null ? request.estimatedHours().doubleValue() : null);
            history.setTimestamp(Instant.now());
            recommendationHistoryRepository.save(history);
        } catch (Exception ex) {
            log.warn("Could not record recommendation history for task {}: {}", request.taskId(), ex.getMessage());
        }
    }

    public List<ReassignmentSuggestionResponse> suggestReassignments(UUID employeeId) {
        EmployeeProfile profile = employeeProfileRepository.findById(employeeId.toString())
                .orElseThrow(() -> ResourceNotFoundException.of("Employee", employeeId));

        WorkloadScoreResult current = scoringService.score(profile);
        if (!scoringService.isOverloaded(current)) {
            return List.of();
        }

        List<TaskSnapshot> tasks = taskSnapshotRepository
                .findAllByAssigneeIdAndStatusIn(employeeId.toString(), WorkloadScoringService.ACTIVE_TASK_STATUSES);

        List<TaskSnapshot> offloadCandidates = tasks.stream()
                .sorted(Comparator.comparingDouble(this::taskLoadWeight).reversed())
                .limit(3)
                .toList();

        List<ReassignmentSuggestionResponse> suggestions = new ArrayList<>();
        for (TaskSnapshot task : offloadCandidates) {
            List<TaskAssignmentRecommendationResponse> alternatives = employeeProfileRepository.findAllByActiveTrue()
                    .stream()
                    .filter(candidate -> !candidate.getEmployeeId().equals(employeeId.toString()))
                    .map(candidate -> toRecommendation(candidate, Set.of()))
                    .sorted(Comparator.comparingDouble(TaskAssignmentRecommendationResponse::workloadScore))
                    .limit(1)
                    .toList();

            if (!alternatives.isEmpty()) {
                suggestions.add(ReassignmentSuggestionResponse.builder()
                        .taskId(UUID.fromString(task.getTaskId()))
                        .taskTitle(task.getTitle())
                        .suggestedAssignee(alternatives.get(0))
                        .build());
            }
        }
        return suggestions;
    }

    // ------------------------------------------------------------------

    public List<AssignmentCandidateResponse> getAssignmentCandidates(TaskAssignmentRequest request) {
        List<EmployeeProfile> candidates = employeeProfileRepository.findAllByActiveTrue();
        Set<String> requiredSkills = normalizedSkills(request.requiredSkills());
        
        // Find top N recommended candidates to flag them in the response
        List<TaskAssignmentRecommendationResponse> recommended = recommend(request);
        Set<UUID> recommendedIds = recommended.stream()
                .map(TaskAssignmentRecommendationResponse::employeeId)
                .collect(Collectors.toSet());

        return candidates.stream().map(profile -> {
            WorkloadScoreResult result = scoringService.score(profile);
            List<String> reasons = buildReasons(profile, result, requiredSkills);
            boolean isRec = recommendedIds.contains(result.employeeId());
            
            int capacity = profile.getWeeklyCapacityHours() > 0 ? profile.getWeeklyCapacityHours() : properties.getDefaultWeeklyCapacityHours();
            double utilization = (result.remainingEstimatedHours() / capacity) * 100.0;
            
            return AssignmentCandidateResponse.builder()
                    .employeeId(result.employeeId())
                    .employeeName(result.employeeName())
                    .skills(profile.getSkills())
                    .weeklyCapacityHours(capacity)
                    .workloadScore(round(result.score()))
                    .riskBand(result.band())
                    .activeTaskCount(result.activeTaskCount())
                    .overdueTaskCount(result.overdueTaskCount())
                    .remainingEstimatedHours(result.remainingEstimatedHours())
                    .openBugCount(result.openBugCount())
                    .onLeaveToday(result.onLeaveToday())
                    .utilizationPercent(round(utilization))
                    .isRecommended(isRec)
                    .recommendationReasons(isRec ? reasons : List.of())
                    .build();
        }).collect(Collectors.toList());
    }

    private TaskAssignmentRecommendationResponse toRecommendation(EmployeeProfile profile, Set<String> requiredSkills) {
        WorkloadScoreResult result = scoringService.score(profile);
        List<String> reasons = buildReasons(profile, result, requiredSkills);
        return TaskAssignmentRecommendationResponse.builder()
                .employeeId(result.employeeId())
                .employeeName(result.employeeName())
                .workloadScore(round(result.score()))
                .reasons(reasons)
                .build();
    }

    private List<String> buildReasons(EmployeeProfile profile, WorkloadScoreResult result, Set<String> requiredSkills) {
        List<String> reasons = new ArrayList<>();

        String headline = aiProvider.explain(
                "{employee} has a workload score of {score} out of 100 ({band}).",
                Map.of("employee", result.employeeName(), "score", (int) round(result.score()), "band", result.band()));
        reasons.add(headline);

        reasons.add("%d active task(s), %d overdue".formatted(result.activeTaskCount(), result.overdueTaskCount()));
        if (result.remainingEstimatedHours() > 0) {
            reasons.add("%.1f estimated hour(s) of work remaining".formatted(result.remainingEstimatedHours()));
        }
        if (result.openBugCount() > 0) {
            reasons.add("%d open bug(s) assigned".formatted(result.openBugCount()));
        }
        if (result.onLeaveToday()) {
            reasons.add("Currently on approved leave");
        }
        if (!requiredSkills.isEmpty()) {
            Set<String> matched = normalizedSkills(profile.getSkills());
            matched.retainAll(requiredSkills);
            reasons.add(matched.isEmpty()
                    ? "No exact skill match on record — shown as the best available capacity"
                    : "Matches required skill(s): " + String.join(", ", matched));
        }
        return reasons;
    }

    private double taskLoadWeight(TaskSnapshot task) {
        double hours = task.getEstimatedHours() != null ? task.getEstimatedHours() : 0.0;
        int priorityWeight = switch (task.getPriority() != null ? task.getPriority() : "MEDIUM") {
            case "CRITICAL" -> 8;
            case "HIGH" -> 4;
            case "LOW" -> 1;
            default -> 2;
        };
        return hours + priorityWeight * 2.0;
    }

    private Set<String> normalizedSkills(Set<String> skills) {
        if (skills == null) {
            return new HashSet<>();
        }
        return skills.stream().map(s -> s.trim().toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
    }

    private boolean disjoint(Set<String> a, Set<String> b) {
        return a.stream().noneMatch(b::contains);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
