package com.teamflow.ai.project.repository.spec;

import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.project.entity.Task;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Task> inProject(UUID projectId) {
        if (projectId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> inSprint(UUID sprintId) {
        if (sprintId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("sprint").get("id"), sprintId);
    }

    public static Specification<Task> assignedTo(UUID assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("assigneeId"), assigneeId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(Priority priority) {
        if (priority == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }
}
