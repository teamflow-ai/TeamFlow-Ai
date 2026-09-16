package com.teamflow.ai.project.repository;

import com.teamflow.ai.common.enums.BugStatus;
import com.teamflow.ai.project.entity.Bug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BugRepository extends JpaRepository<Bug, UUID>, JpaSpecificationExecutor<Bug> {

    Optional<Bug> findByIdAndDeletedFalse(UUID id);

    Page<Bug> findAllByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);

    long countByStatusInAndDeletedFalse(List<BugStatus> statuses);

    long countByAssigneeIdAndStatusInAndDeletedFalse(UUID assigneeId, List<BugStatus> statuses);

    long countByProjectIdAndDeletedFalse(UUID projectId);

    long countByProjectIdAndStatusInAndDeletedFalse(UUID projectId, List<BugStatus> statuses);
}
