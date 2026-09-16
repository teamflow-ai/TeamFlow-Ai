package com.teamflow.ai.ai.service;

import com.teamflow.ai.ai.document.RecommendationHistory;
import com.teamflow.ai.ai.dto.request.RecommendationDecisionRequest;
import com.teamflow.ai.ai.dto.response.RecommendationHistoryResponse;
import com.teamflow.ai.ai.mapper.RecommendationHistoryMapper;
import com.teamflow.ai.ai.repository.RecommendationHistoryRepository;
import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationHistoryService {

    private final RecommendationHistoryRepository recommendationHistoryRepository;
    private final RecommendationHistoryMapper mapper;

    public List<RecommendationHistoryResponse> forTask(String taskId) {
        return recommendationHistoryRepository.findAllByTaskIdOrderByTimestampDesc(taskId).stream()
                .map(mapper::toResponse).toList();
    }

    public PageResponse<RecommendationHistoryResponse> all(Pageable pageable) {
        Page<RecommendationHistory> page = recommendationHistoryRepository.findAllByOrderByTimestampDesc(pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    /** Records the manager's decision and, once actual hours are also known, the resulting prediction accuracy. */
    public RecommendationHistoryResponse recordDecision(String id, RecommendationDecisionRequest request) {
        RecommendationHistory history = recommendationHistoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("RecommendationHistory", id));

        history.setAccepted(request.accepted());
        if (request.actualHours() != null) {
            history.setActualHours(request.actualHours());
        }
        if (history.getEstimatedHours() != null && history.getActualHours() != null
                && history.getEstimatedHours() > 0) {
            double error = Math.abs(history.getEstimatedHours() - history.getActualHours()) / history.getEstimatedHours();
            history.setPredictionAccuracy(Math.max(0.0, Math.round((1.0 - error) * 1000.0) / 10.0));
        }
        return mapper.toResponse(recommendationHistoryRepository.save(history));
    }
}
