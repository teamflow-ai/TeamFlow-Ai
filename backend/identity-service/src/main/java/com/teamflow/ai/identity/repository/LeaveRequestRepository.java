package com.teamflow.ai.identity.repository;

import com.teamflow.ai.common.enums.LeaveStatus;
import com.teamflow.ai.identity.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    Optional<LeaveRequest> findByIdAndDeletedFalse(UUID id);

    Page<LeaveRequest> findAllByEmployeeIdAndDeletedFalse(UUID employeeId, Pageable pageable);

    Page<LeaveRequest> findAllByStatusAndDeletedFalse(LeaveStatus status, Pageable pageable);

    @Query("""
            select l from LeaveRequest l
            where l.status = :status
              and l.deleted = false
              and l.employee.managerId = :managerId
            """)
    Page<LeaveRequest> findAllByStatusAndEmployeeManagerIdAndDeletedFalse(
            @Param("status") LeaveStatus status,
            @Param("managerId") UUID managerId,
            Pageable pageable);

    /**
     * Detects a clash with an existing request before accepting a new one.
     *
     * <p>Two ranges overlap when each starts on or before the other ends; encoding
     * that here keeps the rule out of the service and testable directly.
     */
    @Query("""
            select case when count(l) > 0 then true else false end from LeaveRequest l
            where l.employee.id = :employeeId
              and l.deleted = false
              and l.status in :activeStatuses
              and l.startDate <= :endDate
              and l.endDate >= :startDate
            """)
    boolean existsOverlapping(@Param("employeeId") UUID employeeId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("activeStatuses") List<LeaveStatus> activeStatuses);

    /** Approved leave intersecting a window; used to discount capacity in AI scoring. */
    @Query("""
            select l from LeaveRequest l
            where l.employee.id in :employeeIds
              and l.deleted = false
              and l.status = com.teamflow.ai.common.enums.LeaveStatus.APPROVED
              and l.startDate <= :endDate
              and l.endDate >= :startDate
            """)
    List<LeaveRequest> findApprovedInWindow(@Param("employeeIds") List<UUID> employeeIds,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("""
            select count(l) from LeaveRequest l
            where l.employee.department.id = :departmentId
              and l.deleted = false
              and l.status = com.teamflow.ai.common.enums.LeaveStatus.PENDING
            """)
    long countPendingByDepartmentId(@Param("departmentId") UUID departmentId);
}
