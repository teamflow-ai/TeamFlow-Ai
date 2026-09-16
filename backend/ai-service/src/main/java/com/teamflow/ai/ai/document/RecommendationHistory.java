package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * One record per AI Smart Task Assignment recommendation generated (product
 * brief Feature 8: Recommendation History).
 *
 * <p>Written at generation time by {@code RecommendationService}. {@code accepted}
 * starts {@code null} ("not yet decided") and is filled in later via
 * {@code RecommendationHistoryController} once a manager actually assigns the
 * task — project-service does not currently call back to record that decision
 * automatically, so this is populated either by a manual call to that endpoint
 * or by a future project-service integration; documented here rather than
 * silently assumed. {@code actualHours} similarly starts unset and can be
 * filled in once the task completes, letting {@code predictionAccuracy} be
 * computed for offline review of how good the recommendations actually are.
 */
@Document(collection = "recommendation_history")
@Getter
@Setter
@NoArgsConstructor
public class RecommendationHistory {

    @Id
    private String id;

    @Indexed
    private String taskId;

    private String aiProvider;
    private java.util.Map<String, Object> promptMetadata;
    private List<String> recommendedEmployeeIds;
    private String topRecommendationEmployeeId;

    /** null = not yet decided, true = manager assigned the top recommendation, false = manager chose differently. */
    private Boolean accepted;

    private Double estimatedHours;
    private Double actualHours;

    /** 0-100; null until both estimatedHours and actualHours are known. */
    private Double predictionAccuracy;

    @Indexed
    private Instant timestamp;
}
