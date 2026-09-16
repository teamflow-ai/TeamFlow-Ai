package com.teamflow.ai.identity.repository;

import com.teamflow.ai.identity.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByIdAndDeletedFalse(UUID id);

    Page<Department> findAllByDeletedFalse(Pageable pageable);

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    long countByDeletedFalse();
}
