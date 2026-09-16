package com.teamflow.ai.identity.util;

import com.teamflow.ai.identity.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Assigns the next sequential employee code.
 *
 * <p>Shared by self-registration and HR-initiated creation so the two paths can
 * never diverge on format. A simple count-based sequence is sufficient here: codes
 * are a human-readable label, not a concurrency-sensitive identifier (the database
 * primary key already fills that role).
 */
@Component
@RequiredArgsConstructor
public class EmployeeCodeGenerator {

    private final EmployeeRepository employeeRepository;

    public String next() {
        long existing = employeeRepository.count();
        String code;
        do {
            existing++;
            code = "EMP-%04d".formatted(existing);
        } while (employeeRepository.existsByEmployeeCodeIgnoreCase(code));
        return code;
    }
}
