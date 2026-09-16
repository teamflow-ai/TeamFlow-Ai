package com.teamflow.ai.project.repository;

import com.teamflow.ai.project.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    Optional<Meeting> findByIdAndDeletedFalse(UUID id);

    List<Meeting> findTop5ByTitleContainingIgnoreCaseAndDeletedFalse(String keyword);

    Page<Meeting> findAllByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);

    @Query("""
            select m from Meeting m
            where m.deleted = false
              and m.scheduledAt between :dayStart and :dayEnd
            order by m.scheduledAt asc
            """)
    List<Meeting> findScheduledBetween(@Param("dayStart") Instant dayStart, @Param("dayEnd") Instant dayEnd);

    @Query("""
            select m from Meeting m
            join m.participantIds p
            where m.deleted = false and p = :employeeId
              and m.scheduledAt between :dayStart and :dayEnd
            order by m.scheduledAt asc
            """)
    List<Meeting> findForParticipantBetween(@Param("employeeId") UUID employeeId,
                                            @Param("dayStart") Instant dayStart,
                                            @Param("dayEnd") Instant dayEnd);
}
