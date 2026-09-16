package com.teamflow.ai.identity.repository;

import com.teamflow.ai.identity.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Employee persistence.
 *
 * <p>Extends {@link JpaSpecificationExecutor} so the search endpoint can compose
 * optional filters (department, team, skill, active) dynamically instead of
 * exploding into one derived method per filter combination.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByIdAndDeletedFalse(UUID id);

    Optional<Employee> findByWorkEmailIgnoreCaseAndDeletedFalse(String workEmail);

    boolean existsByWorkEmailIgnoreCaseAndDeletedFalse(String workEmail);

    boolean existsByEmployeeCodeIgnoreCaseAndDeletedFalse(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    Page<Employee> findAllByDeletedFalse(Pageable pageable);

    Page<Employee> findAllByDepartmentIdAndDeletedFalse(UUID departmentId, Pageable pageable);

    Page<Employee> findAllByManagerIdAndDeletedFalse(UUID managerId, Pageable pageable);

    /**
     * Employees possessing at least one of the requested skills.
     *
     * <p>Consumed by the AI assignment scorer as its candidate shortlist, which is
     * why filtering happens in SQL rather than by loading every employee.
     */
    @Query("""
            select distinct e from Employee e
            join e.skills s
            where e.active = true
              and e.deleted = false
              and s in :skills
            """)
    List<Employee> findCandidatesBySkills(@Param("skills") List<String> skills);

    long countByDeletedFalse();

    long countByDepartmentIdAndDeletedFalse(UUID departmentId);

    long countByDepartmentIdAndActiveAndDeletedFalse(UUID departmentId, boolean active);

    @Query("SELECT COUNT(DISTINCT e) FROM Employee e JOIN e.teams t WHERE t.team.id = :teamId AND e.deleted = false")
    long countByTeamIdAndDeletedFalse(@Param("teamId") UUID teamId);
}
