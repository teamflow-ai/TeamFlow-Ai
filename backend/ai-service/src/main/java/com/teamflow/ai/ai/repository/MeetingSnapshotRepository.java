package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.MeetingSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MeetingSnapshotRepository extends MongoRepository<MeetingSnapshot, String> {

    List<MeetingSnapshot> findAllByScheduledAtBetween(Instant from, Instant to);
}
