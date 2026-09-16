package com.teamflow.ai.project.repository;

import com.teamflow.ai.project.entity.WorkLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, UUID> {

    Optional<WorkLog> findByIdAndDeletedFalse(UUID id);

    Page<WorkLog> findAllByDeletedFalse(Pageable pageable);

    Page<WorkLog> findAllByEmployeeIdAndDeletedFalse(UUID employeeId, Pageable pageable);

    Page<WorkLog> findAllByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);

    Page<WorkLog> findAllByTaskIdAndDeletedFalse(UUID taskId, Pageable pageable);

    @Query("""
            select coalesce(sum(w.hours), 0) from WorkLog w
            where w.employeeId = :employeeId
              and w.deleted = false
              and w.logDate between :start and :end
            """)
    BigDecimal sumHoursByEmployeeAndDateRange(@Param("employeeId") UUID employeeId,
                                              @Param("start") LocalDate start,
                                              @Param("end") LocalDate end);

    List<WorkLog> findAllByProjectIdAndDeletedFalseAndLogDateBetween(UUID projectId, LocalDate start, LocalDate end);

    List<WorkLog> findAllByEmployeeIdAndDeletedFalseAndLogDateBetween(UUID employeeId, LocalDate start, LocalDate end);
}
