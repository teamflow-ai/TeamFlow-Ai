package com.teamflow.ai.identity.repository;

import com.teamflow.ai.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query("""
            select distinct r from Role r
            left join fetch r.permissions
            where r.name = :name and r.deleted = false
            """)
    Optional<Role> findByNameWithPermissions(@Param("name") String name);

    Optional<Role> findByNameAndDeletedFalse(String name);

    boolean existsByNameAndDeletedFalse(String name);

    @Query("select distinct r from Role r left join fetch r.permissions where r.deleted = false")
    List<Role> findAllWithPermissions();
}
