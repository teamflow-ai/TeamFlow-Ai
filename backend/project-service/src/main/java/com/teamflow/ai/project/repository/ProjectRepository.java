package com.teamflow.ai.project.repository;

import com.teamflow.ai.common.enums.ProjectStatus;
import com.teamflow.ai.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    Optional<Project> findByIdAndDeletedFalse(UUID id);

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    long countByStatusAndDeletedFalse(ProjectStatus status);

    List<Project> findAllByStatusAndDeletedFalse(ProjectStatus status);

    Page<Project> findAllByManagerIdAndDeletedFalse(UUID managerId, Pageable pageable);
}
