package com.teamflow.ai.project.repository.spec;

import com.teamflow.ai.common.enums.ProjectStatus;
import com.teamflow.ai.project.entity.Project;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<Project> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Project> nameOrCodeContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("code")), pattern));
    }

    public static Specification<Project> hasStatus(ProjectStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Project> hasManager(UUID managerId) {
        if (managerId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("managerId"), managerId);
    }
}
