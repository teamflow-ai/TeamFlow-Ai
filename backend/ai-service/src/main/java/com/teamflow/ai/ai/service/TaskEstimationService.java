package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.document.TaskSnapshot;
import com.teamflow.ai.ai.dto.request.TaskEstimationRequest;
import com.teamflow.ai.ai.dto.response.TaskEstimationResponse;
import com.teamflow.ai.ai.provider.AiProvider;
import com.teamflow.ai.ai.repository.TaskSnapshotRepository;
import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI Task Estimation (product brief Feature 2).
 *
 * <p>The estimate itself is always a rule-based computation over historical
 * completed-task hours — never a number an LLM invented — so it is reproducible
 * and auditable in a CDAC viva. The configured {@link AiProvider} is used only to
 * phrase the reasoning in natural language, exactly the same pattern
 * {@code RecommendationService} already uses.
 */
@Service
@RequiredArgsConstructor
public class TaskEstimationService {

    /** Complexity multiplier applied to the historical baseline. */
    private static final Map<String, Double> COMPLEXITY_MULTIPLIER = Map.of(
            "LOW", 0.7, "MEDIUM", 1.0, "HIGH", 1.5);

    private static final double DEFAULT_BASELINE_HOURS = 8.0;
    private static final double HOURS_PER_WORKDAY = 6.0;

    private final TaskSnapshotRepository taskSnapshotRepository;
    private final AiProvider aiProvider;

    public TaskEstimationResponse estimate(TaskEstimationRequest request) {
        String complexity = normalizeComplexity(request.complexity());
        double multiplier = COMPLEXITY_MULTIPLIER.getOrDefault(complexity, 1.0);

        List<TaskSnapshot> priorityMatched = completedWithHours(request.priority());
        List<TaskSnapshot> historicalSample = priorityMatched.isEmpty() ? completedWithHours(null) : priorityMatched;

        double baseline = historicalSample.isEmpty()
                ? DEFAULT_BASELINE_HOURS * priorityFactor(request.priority())
                : average(historicalSample);

        double estimatedHours = round(baseline * multiplier);
        int workdays = Math.max(1, (int) Math.ceil(estimatedHours / HOURS_PER_WORKDAY));
        LocalDate start = LocalDate.now();
        LocalDate completion = addWorkdays(start, workdays);

        String risk = riskLevel(complexity, request.priority());
        int confidence = confidencePercent(historicalSample.size(), !priorityMatched.isEmpty());

        String reasoning = aiProvider.explain(
                "Estimated at {hours}h based on {sample} comparable completed task(s) "
                        + "({priority} priority, {complexity} complexity), risk: {risk}.",
                Map.of("hours", estimatedHours, "sample", historicalSample.size(),
                        "priority", request.priority(), "complexity", complexity, "risk", risk));

        return TaskEstimationResponse.builder()
                .estimatedHours(estimatedHours)
                .estimatedStartDate(start)
                .estimatedCompletionDate(completion)
                .complexityRating(complexity)
                .risk(risk)
                .confidencePercent(confidence)
                .reasoning(reasoning)
                .build();
    }

    private List<TaskSnapshot> completedWithHours(Priority priority) {
        List<TaskSnapshot> done = taskSnapshotRepository.findAllByStatusIn(List.of(TaskStatus.DONE.name()));
        return done.stream()
                .filter(task -> task.getActualHours() != null && task.getActualHours() > 0)
                .filter(task -> priority == null || priority.name().equals(task.getPriority()))
                .toList();
    }

    private double average(List<TaskSnapshot> tasks) {
        return tasks.stream().mapToDouble(TaskSnapshot::getActualHours).average().orElse(DEFAULT_BASELINE_HOURS);
    }

    private double priorityFactor(Priority priority) {
        return switch (priority) {
            case LOW -> 0.75;
            case MEDIUM -> 1.0;
            case HIGH -> 1.5;
            case CRITICAL -> 2.0;
        };
    }

    private String riskLevel(String complexity, Priority priority) {
        int score = (complexity.equals("HIGH") ? 2 : complexity.equals("MEDIUM") ? 1 : 0) + priority.weight();
        if (score >= 8) {
            return "HIGH";
        }
        if (score >= 4) {
            return "MEDIUM";
        }
        return "LOW";
    }

    /** More historical, priority-matched samples earn higher confidence; a pure fallback earns the least. */
    private int confidencePercent(int sampleSize, boolean priorityMatched) {
        if (sampleSize == 0) {
            return 30;
        }
        int base = priorityMatched ? 60 : 45;
        return Math.min(95, base + Math.min(sampleSize, 10) * 3);
    }

    private String normalizeComplexity(String raw) {
        String upper = raw == null ? "MEDIUM" : raw.trim().toUpperCase(Locale.ROOT);
        return COMPLEXITY_MULTIPLIER.containsKey(upper) ? upper : "MEDIUM";
    }

    private LocalDate addWorkdays(LocalDate start, int workdays) {
        LocalDate date = start;
        int added = 0;
        while (added < workdays) {
            date = date.plusDays(1);
            if (date.getDayOfWeek().getValue() < 6) {
                added++;
            }
        }
        return date;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
