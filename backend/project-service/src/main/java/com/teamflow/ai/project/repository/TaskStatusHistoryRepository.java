package com.teamflow.ai.project.repository;

import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.project.entity.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, UUID> {

    List<TaskStatusHistory> findAllByTaskIdOrderByCreatedAtDesc(UUID taskId);

    long countByChangedByAndToStatusAndCreatedAtBetween(UUID changedBy, TaskStatus toStatus, Instant start, Instant end);
}
