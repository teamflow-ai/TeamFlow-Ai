package com.teamflow.ai.identity.repository;

import com.teamflow.ai.identity.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByNameAndDeletedFalse(String name);

    boolean existsByNameAndDeletedFalse(String name);

    List<Permission> findAllByNameInAndDeletedFalse(Set<String> names);

    List<Permission> findAllByDeletedFalseOrderByCategoryAscNameAsc();
}
