package com.teamflow.ai.identity.repository;

import com.teamflow.ai.identity.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByIdAndDeletedFalse(UUID id);

    Page<Team> findAllByDeletedFalse(Pageable pageable);

    Page<Team> findAllByDepartmentIdAndDeletedFalse(UUID departmentId, Pageable pageable);

    boolean existsByDepartmentIdAndNameIgnoreCaseAndDeletedFalse(UUID departmentId, String name);
}
