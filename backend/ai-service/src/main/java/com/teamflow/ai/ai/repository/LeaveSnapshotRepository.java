package com.teamflow.ai.ai.repository;

import com.teamflow.ai.ai.document.LeaveSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveSnapshotRepository extends MongoRepository<LeaveSnapshot, String> {

    List<LeaveSnapshot> findAllByEmployeeId(String employeeId);

    List<LeaveSnapshot> findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date, LocalDate sameDate);
}
