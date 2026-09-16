package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.document.TaskSnapshot;
import com.teamflow.ai.ai.dto.response.TaskRiskResponse;
import com.teamflow.ai.ai.provider.AiProvider;
import com.teamflow.ai.ai.repository.TaskSnapshotRepository;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRiskService {

    private final TaskSnapshotRepository taskSnapshotRepository;
    private final AiProvider aiProvider;

    public TaskRiskResponse evaluateRisk(String taskId) {
        TaskSnapshot task = taskSnapshotRepository.findById(taskId)
                .orElseThrow(() -> ResourceNotFoundException.of("TaskSnapshot", taskId));

        String riskLevel = "LOW";
        String template = "The task '{title}' is on track. {logged} of {estimated} hours logged.";

        Double est = task.getEstimatedHours() != null ? task.getEstimatedHours() : 0.0;
        Double act = task.getActualHours() != null ? task.getActualHours() : 0.0;
        LocalDate due = task.getDueDate();
        LocalDate now = LocalDate.now();

        if ("DONE".equalsIgnoreCase(task.getStatus()) || "VERIFIED".equalsIgnoreCase(task.getStatus())) {
            riskLevel = "LOW";
            template = "Task is completed, no risk detected.";
        } else if (due != null && due.isBefore(now)) {
            riskLevel = "HIGH";
            template = "Task is OVERDUE! It was due on {dueDate}. Currently {logged} hours logged against {estimated} hours estimated.";
        } else if (est > 0 && act >= est) {
            riskLevel = "HIGH";
            template = "Budget overrun! {logged} hours logged, which exceeds the estimated {estimated} hours. Due in {daysLeft} days.";
        } else if (due != null && est > 0) {
            long daysLeft = ChronoUnit.DAYS.between(now, due);
            if (daysLeft <= 3 && (act / est) < 0.5) {
                riskLevel = "HIGH";
                template = "Crunch time! Only {daysLeft} days left until deadline, but less than 50% of work is logged ({logged}/{estimated} hrs). High risk of missing deadline.";
            } else if (daysLeft <= 7 && (act / est) < 0.2) {
                riskLevel = "MEDIUM";
                template = "Falling behind! {daysLeft} days left until deadline, but less than 20% of work is logged ({logged}/{estimated} hrs). Medium risk of delay.";
            } else {
                template = "Task '{title}' is on track. {logged} out of {estimated} hours logged. Due in {daysLeft} days.";
            }
        }

        long finalDaysLeft = due != null ? ChronoUnit.DAYS.between(now, due) : 0;
        
        String explanation = aiProvider.explain(template, Map.of(
                "title", task.getTitle() != null ? task.getTitle() : "Unknown",
                "logged", act,
                "estimated", est,
                "dueDate", due != null ? due.toString() : "No Date",
                "daysLeft", finalDaysLeft > 0 ? finalDaysLeft : 0
        ));

        return TaskRiskResponse.builder()
                .riskLevel(riskLevel)
                .explanation(explanation)
                .provider(aiProvider.name())
                .build();
    }
}
