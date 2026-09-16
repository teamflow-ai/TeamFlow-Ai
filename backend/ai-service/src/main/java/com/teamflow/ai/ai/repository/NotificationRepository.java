package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.NotificationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationDocument, String> {

    Page<NotificationDocument> findAllByRecipientEmployeeIdOrderByCreatedAtDesc(String recipientEmployeeId, Pageable pageable);

    Page<NotificationDocument> findAllByRecipientEmployeeIdAndReadFalseOrderByCreatedAtDesc(String recipientEmployeeId, Pageable pageable);

    long countByRecipientEmployeeIdAndReadFalse(String recipientEmployeeId);
}
