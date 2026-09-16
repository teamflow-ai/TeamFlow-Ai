package com.teamflow.ai.identity.repository.spec;

import com.teamflow.ai.identity.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Composable filters for the employee search endpoint.
 *
 * <p>Kept as static predicates rather than a QueryDSL/Criteria builder wrapper
 * class: each filter is a one-liner, and {@link Specification#allOf} already
 * handles null-safe composition.
 */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Employee> nameOrEmailContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("workEmail")), pattern),
                cb.like(cb.lower(root.get("employeeCode")), pattern));
    }

    public static Specification<Employee> hasDepartment(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId);
    }

    public static Specification<Employee> hasTeam(UUID teamId) {
        if (teamId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("team").get("id"), teamId);
    }

    public static Specification<Employee> hasManager(UUID managerId) {
        if (managerId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("managerId"), managerId);
    }

    public static Specification<Employee> isActive(Boolean active) {
        if (active == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    public static Specification<Employee> hasSkill(String skill) {
        if (skill == null || skill.isBlank()) {
            return null;
        }
        String normalized = skill.trim().toUpperCase();
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.isMember(normalized, root.get("skills"));
        };
    }
}
