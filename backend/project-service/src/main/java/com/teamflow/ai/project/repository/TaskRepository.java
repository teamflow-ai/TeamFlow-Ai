package com.teamflow.ai.project.repository;

import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.project.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndDeletedFalse(UUID id);

    List<Task> findTop5ByTitleContainingIgnoreCaseAndDeletedFalse(String keyword);

    Page<Task> findAllByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);

    List<Task> findByProjectIdAndDeletedFalse(UUID projectId);

    Page<Task> findAllByAssigneeIdAndDeletedFalse(UUID assigneeId, Pageable pageable);

    long countByProjectIdAndDeletedFalse(UUID projectId);

    long countByProjectIdAndStatusAndDeletedFalse(UUID projectId, TaskStatus status);

    long countBySprintIdAndDeletedFalse(UUID sprintId);

    long countBySprintIdAndStatusAndDeletedFalse(UUID sprintId, TaskStatus status);

    @Query("select coalesce(sum(t.estimatedHours), 0) from Task t where t.sprint.id = :sprintId and t.deleted = false")
    java.math.BigDecimal sumEstimatedHoursBySprintId(@Param("sprintId") UUID sprintId);

    @Query("select coalesce(sum(t.actualHours), 0) from Task t where t.sprint.id = :sprintId and t.deleted = false")
    java.math.BigDecimal sumActualHoursBySprintId(@Param("sprintId") UUID sprintId);

    long countByStatusAndDeletedFalse(TaskStatus status);

    long countByAssigneeIdAndStatusNotInAndDeletedFalse(UUID assigneeId, List<TaskStatus> terminalStatuses);

    @Query("""
            select t from Task t
            where t.deleted = false
              and t.status not in :terminalStatuses
              and t.dueDate is not null
              and t.dueDate < :today
            """)
    List<Task> findOverdue(@Param("today") LocalDate today, @Param("terminalStatuses") List<TaskStatus> terminalStatuses);

    @Query("""
            select t from Task t
            where t.deleted = false
              and t.assigneeId = :employeeId
              and t.status not in :terminalStatuses
            """)
    List<Task> findActiveByAssignee(@Param("employeeId") UUID employeeId,
                                    @Param("terminalStatuses") List<TaskStatus> terminalStatuses);
}
