package com.teamflow.organization.repository;

import com.teamflow.organization.entity.Organization;
import com.teamflow.organization.enums.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for {@link Organization} entities.
 */
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    Page<Organization> findByStatus(OrganizationStatus status, Pageable pageable);

    Page<Organization> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
