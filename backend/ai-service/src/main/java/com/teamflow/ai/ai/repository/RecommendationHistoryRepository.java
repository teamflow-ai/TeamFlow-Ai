package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.RecommendationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationHistoryRepository extends MongoRepository<RecommendationHistory, String> {

    List<RecommendationHistory> findAllByTaskIdOrderByTimestampDesc(String taskId);

    Page<RecommendationHistory> findAllByOrderByTimestampDesc(Pageable pageable);
}
