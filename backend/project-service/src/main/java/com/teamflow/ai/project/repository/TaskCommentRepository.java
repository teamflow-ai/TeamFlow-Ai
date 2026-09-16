package com.teamflow.ai.project.repository;

import com.teamflow.ai.project.entity.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    Page<TaskComment> findAllByTaskIdAndDeletedFalseOrderByCreatedAtDesc(UUID taskId, Pageable pageable);
}
