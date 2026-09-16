package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.ProjectSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectSnapshotRepository extends MongoRepository<ProjectSnapshot, String> {

    long countByStatus(String status);

    List<ProjectSnapshot> findAllByStatus(String status);
}
