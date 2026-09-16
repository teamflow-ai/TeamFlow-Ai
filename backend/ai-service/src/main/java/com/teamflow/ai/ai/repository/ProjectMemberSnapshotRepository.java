package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.ProjectMemberSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberSnapshotRepository extends MongoRepository<ProjectMemberSnapshot, String> {
    List<ProjectMemberSnapshot> findAllByEmployeeId(String employeeId);
}
