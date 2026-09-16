package com.teamflow.ai.project.repository;

import com.teamflow.ai.project.entity.Milestone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {

    Optional<Milestone> findByIdAndDeletedFalse(UUID id);

    Page<Milestone> findAllByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);
}
