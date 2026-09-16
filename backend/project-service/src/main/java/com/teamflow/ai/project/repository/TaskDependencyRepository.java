package com.teamflow.ai.project.repository;

import com.teamflow.ai.project.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {
    List<TaskDependency> findAllByTaskId(UUID taskId);
    List<TaskDependency> findAllByDependsOnTaskId(UUID dependsOnTaskId);
    void deleteByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);
    boolean existsByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);
}
