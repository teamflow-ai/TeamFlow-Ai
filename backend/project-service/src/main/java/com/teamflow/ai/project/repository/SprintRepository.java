package com.teamflow.ai.project.repository;

import com.teamflow.ai.common.enums.SprintStatus;
import com.teamflow.ai.project.entity.Sprint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    Optional<Sprint> findByIdAndDeletedFalse(UUID id);

    Page<Sprint> findAllByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);

    long countByProjectIdAndStatusAndDeletedFalse(UUID projectId, SprintStatus status);
}
