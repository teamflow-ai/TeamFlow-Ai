package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.AuditHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditHistoryRepository extends MongoRepository<AuditHistory, String> {

    Page<AuditHistory> findAllByEntityTypeAndEntityIdOrderByTimestampDesc(
            String entityType, String entityId, Pageable pageable);

    Page<AuditHistory> findAllByProjectIdOrderByTimestampDesc(String projectId, Pageable pageable);

    Page<AuditHistory> findAllByOrderByTimestampDesc(Pageable pageable);
}
