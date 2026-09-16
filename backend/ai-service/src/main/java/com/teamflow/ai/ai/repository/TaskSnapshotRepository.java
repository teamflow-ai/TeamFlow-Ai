package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.TaskSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskSnapshotRepository extends MongoRepository<TaskSnapshot, String> {

    long countByStatus(String status);

    long countByStatusIn(List<String> statuses);

    List<TaskSnapshot> findAllByStatusIn(List<String> statuses);

    List<TaskSnapshot> findAllByAssigneeIdAndStatusIn(String assigneeId, List<String> statuses);

    List<TaskSnapshot> findAllByProjectIdAndStatusIn(String projectId, List<String> statuses);

    List<TaskSnapshot> findAllByProjectId(String projectId);

    List<TaskSnapshot> findAllBySprintIdAndStatusIn(String sprintId, List<String> statuses);

    List<TaskSnapshot> findAllByStatusInAndDueDateLessThan(List<String> statuses, LocalDate date);

    List<TaskSnapshot> findAllByStatusInAndDueDateBetween(List<String> statuses, LocalDate from, LocalDate to);
}
