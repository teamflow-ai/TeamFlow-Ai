package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ai-service's read model of an employee, built entirely from {@code EmployeeEvent}
 * messages published by identity-service.
 *
 * <p>This is the seam that lets the workload scorer avoid a synchronous call into
 * identity-service on every request: by the time a manager asks for a
 * recommendation, the skills and capacity it needs are already local.
 */
@Document(collection = "employee_profiles")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeProfile {

    @Id
    private String employeeId;

    private String fullName;
    private String email;
    private String departmentId;
    private Set<String> skills = new LinkedHashSet<>();
    private int weeklyCapacityHours = 40;
    private boolean active = true;
    private Instant updatedAt;
}
