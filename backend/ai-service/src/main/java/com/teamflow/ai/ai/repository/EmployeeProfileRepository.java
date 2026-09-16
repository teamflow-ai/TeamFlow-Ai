package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.EmployeeProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeProfileRepository extends MongoRepository<EmployeeProfile, String> {

    List<EmployeeProfile> findAllByActiveTrue();
}
