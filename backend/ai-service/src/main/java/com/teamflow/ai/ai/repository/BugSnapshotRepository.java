package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.BugSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugSnapshotRepository extends MongoRepository<BugSnapshot, String> {

    long countByStatusIn(List<String> statuses);

    List<BugSnapshot> findAllByAssigneeIdAndStatusIn(String assigneeId, List<String> statuses);

    List<BugSnapshot> findAllByProjectIdAndStatusIn(String projectId, List<String> statuses);
}
