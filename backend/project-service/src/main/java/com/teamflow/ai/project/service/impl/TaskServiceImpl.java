package com.teamflow.ai.project.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.AssignmentMode;
import com.teamflow.ai.common.enums.Priority;
import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ErrorCode;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.client.AiRecommendationClient;
import com.teamflow.ai.project.client.TaskAssignmentRecommendation;
import com.teamflow.ai.project.client.TaskAssignmentRecommendationRequest;
import com.teamflow.ai.project.dto.request.AddTaskAttachmentRequest;
import com.teamflow.ai.project.dto.request.AddTaskCommentRequest;
import com.teamflow.ai.project.dto.request.AssignTaskRequest;
import com.teamflow.ai.project.dto.request.CreateTaskRequest;
import com.teamflow.ai.project.dto.request.UpdateTaskRequest;
import com.teamflow.ai.project.dto.request.UpdateTaskStatusRequest;
import com.teamflow.ai.project.dto.response.TaskAttachmentResponse;
import com.teamflow.ai.project.dto.response.TaskCommentResponse;
import com.teamflow.ai.project.dto.response.TaskHistoryResponse;
import com.teamflow.ai.project.dto.response.TaskResponse;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.Sprint;
import com.teamflow.ai.project.entity.Task;
import com.teamflow.ai.project.entity.TaskAttachment;
import com.teamflow.ai.project.entity.TaskComment;
import com.teamflow.ai.project.entity.TaskStatusHistory;
import com.teamflow.ai.project.mapper.TaskMapper;
import com.teamflow.ai.project.messaging.ProjectEventPublisher;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.repository.SprintRepository;
import com.teamflow.ai.project.repository.TaskAttachmentRepository;
import com.teamflow.ai.project.repository.TaskCommentRepository;
import com.teamflow.ai.project.repository.TaskRepository;
import com.teamflow.ai.project.repository.TaskStatusHistoryRepository;
import com.teamflow.ai.project.repository.TaskDependencyRepository;
import com.teamflow.ai.project.repository.spec.TaskSpecifications;
import com.teamflow.ai.project.dto.response.TaskDependencyResponse;
import com.teamflow.ai.project.entity.TaskDependency;
import com.teamflow.ai.project.service.EmployeeLookupService;
import com.teamflow.ai.project.service.TaskService;
import com.teamflow.ai.project.approval.event.TaskSubmittedForReviewEvent;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskDependencyRepository taskDependencyRepository;
    private final EmployeeLookupService employeeLookupService;
    private final AiRecommendationClient aiRecommendationClient;
    private final com.teamflow.ai.project.client.LeaveClient leaveClient;
    private final TaskMapper taskMapper;
    private final ProjectEventPublisher eventPublisher;
    /**
     * Deliberately a plain Spring {@link ApplicationEventPublisher} rather than a direct
     * dependency on ApprovalService: the approval engine's handler for TASK_COMPLETION calls
     * back into {@link TaskService} to reuse the validated status-transition logic below, and
     * injecting ApprovalService here would create a circular bean graph
     * (TaskServiceImpl -> ApprovalService -> TaskCompletionApprovalHandler -> TaskService).
     * Publishing a local event and letting a separate listener talk to ApprovalService breaks
     * the cycle while keeping both sides decoupled.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request, UUID reporterId) {
        Project project = projectRepository.findByIdAndDeletedFalse(request.projectId())
                .orElseThrow(() -> ResourceNotFoundException.of("Project", request.projectId()));
        Sprint sprint = resolveSprint(request.sprintId(), project.getId());

        Task task = new Task();
        task.setProject(project);
        task.setSprint(sprint);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : task.getPriority());
        task.setDueDate(request.dueDate());
        task.setEstimatedHours(request.estimatedHours());
        task.setReporterId(reporterId);
        if (request.requiredSkills() != null) {
            task.setRequiredSkills(normalizeSkills(request.requiredSkills()));
        }

        // Strict HR validation for initial assignment
        if (task.getAssigneeId() != null && task.getDueDate() != null) {
            try {
                Boolean isOnLeave = leaveClient.checkOverlap(task.getAssigneeId(), task.getDueDate()).getData();
                if (Boolean.TRUE.equals(isOnLeave)) {
                    throw new BusinessException("Cannot create task: Assignee is on approved leave on the due date.");
                }
            } catch (feign.FeignException e) {
                log.warn("identity-service unreachable during leave validation: {}", e.getMessage());
            }
        }

        Task saved = taskRepository.save(task);
        log.info("Created task {} in project {}", saved.getId(), project.getId());
        eventPublisher.taskCreated(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse update(UUID id, UpdateTaskRequest request) {
        Task task = findOrThrow(id);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : task.getPriority());
        task.setDueDate(request.dueDate());
        task.setEstimatedHours(request.estimatedHours());
        if (request.requiredSkills() != null) {
            task.setRequiredSkills(normalizeSkills(request.requiredSkills()));
        }

        Task saved = taskRepository.save(task);
        log.info("Updated task {}", saved.getId());
        eventPublisher.taskStatusChanged(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse assignToSprint(UUID id, com.teamflow.ai.project.dto.request.AssignSprintRequest request) {
        Task task = findOrThrow(id);
        
        if (request.sprintId() == null) {
            task.setSprint(null);
            log.info("Task {} removed from sprint", id);
        } else {
            Sprint sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
            
            if (!sprint.getProject().getId().equals(task.getProject().getId())) {
                throw new BusinessException("Sprint and task must belong to the same project");
            }
            
            task.setSprint(sprint);
            log.info("Task {} assigned to sprint {}", id, sprint.getId());
        }
        
        Task saved = taskRepository.save(task);
        eventPublisher.taskStatusChanged(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> search(UUID projectId, UUID sprintId, UUID assigneeId, TaskStatus status,
                                             Priority priority, Pageable pageable) {
        Specification<Task> spec = Specification.allOf(
                Stream.of(
                        TaskSpecifications.notDeleted(),
                        TaskSpecifications.inProject(projectId),
                        TaskSpecifications.inSprint(sprintId),
                        TaskSpecifications.assignedTo(assigneeId),
                        TaskSpecifications.hasStatus(status),
                        TaskSpecifications.hasPriority(priority)
                ).filter(Objects::nonNull).collect(Collectors.toList())
        );
        Page<Task> page = taskRepository.findAll(spec, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(UUID id, UpdateTaskStatusRequest request, UUID changedBy) {
        Task task = findOrThrow(id);
        TaskStatus from = task.getStatus();
        TaskStatus to = request.status();

        if (!from.canTransitionTo(to)) {
            throw new BusinessException("Cannot move a task from %s to %s".formatted(from, to));
        }

        if (to == TaskStatus.IN_REVIEW || to == TaskStatus.DONE) {
            List<TaskDependency> dependencies = taskDependencyRepository.findAllByTaskId(id);
            for (TaskDependency dep : dependencies) {
                Task blockingTask = taskRepository.findByIdAndDeletedFalse(dep.getDependsOnTaskId())
                        .orElseThrow(() -> new BusinessException("Dependency task not found"));
                if (blockingTask.getStatus() != TaskStatus.DONE) {
                    throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, 
                        "Cannot transition task: dependent task '" + blockingTask.getTitle() + "' is not DONE");
                }
            }
        }

        task.setStatus(to);
        Task saved = taskRepository.save(task);
        taskStatusHistoryRepository.save(new TaskStatusHistory(saved, from, to, changedBy));
        log.info("Task {} status changed {} -> {}", saved.getId(), from, to);

        eventPublisher.taskStatusChanged(saved);
        if (to == TaskStatus.DONE) {
            eventPublisher.notify(saved.getReporterId(), "Task completed",
                    "\"%s\" was marked done".formatted(saved.getTitle()), "TASK_COMPLETED",
                    "/tasks/" + saved.getId());
        }
        if (to == TaskStatus.IN_REVIEW) {
            applicationEventPublisher.publishEvent(new TaskSubmittedForReviewEvent(
                    saved.getId(), saved.getProject().getId(), saved.getProject().getManagerId(), changedBy));
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse assign(UUID id, AssignTaskRequest request) {
        Task task = findOrThrow(id);
        employeeLookupService.requireExisting(request.assigneeId());

        // Validate project membership
        boolean isManager = task.getProject().getManagerId().equals(request.assigneeId());
        boolean isMember = task.getProject().getMembers().stream()
                .anyMatch(m -> m.getEmployeeId().equals(request.assigneeId()));
        
        if (!isManager && !isMember) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, 
                "Cannot assign task to an employee who is not a member of the project team.");
        }

        // Strict HR validation before assigning
        if (task.getDueDate() != null) {
            try {
                Boolean isOnLeave = leaveClient.checkOverlap(request.assigneeId(), task.getDueDate()).getData();
                if (Boolean.TRUE.equals(isOnLeave)) {
                    throw new BusinessException("Cannot assign task: Employee is on approved leave on the due date.");
                }
            } catch (feign.FeignException e) {
                log.warn("identity-service unreachable during leave validation: {}", e.getMessage());
            }
        }

        // Validate leave and capacity using the new AI workspace endpoint
        try {
            TaskAssignmentRecommendationRequest aiReq = new TaskAssignmentRecommendationRequest(
                    task.getId(), task.getProject().getId(), task.getRequiredSkills(),
                    task.getPriority(), task.getEstimatedHours(), task.getDueDate());
            
            List<com.teamflow.ai.project.client.AssignmentCandidateResponse> candidates = 
                    aiRecommendationClient.getAssignmentCandidates(aiReq).getData();
                    
            candidates.stream()
                    .filter(c -> c.employeeId().equals(request.assigneeId()))
                    .findFirst()
                    .ifPresent(candidate -> {
                        if (candidate.onLeaveToday()) {
                            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, 
                                "Cannot assign task: Employee is currently on approved leave.");
                        }
                    });
        } catch (FeignException ex) {
            log.warn("ai-service unreachable during assignment validation for task {}", id);
            // We allow assignment to proceed if AI service is down to not block critical work
        }

        task.setAssigneeId(request.assigneeId());
        task.setAssignmentMode(request.mode() != null ? request.mode() : AssignmentMode.MANUAL);
        if (task.getStatus() == TaskStatus.BACKLOG) {
            TaskStatus from = task.getStatus();
            task.setStatus(TaskStatus.TODO);
            taskStatusHistoryRepository.save(new TaskStatusHistory(task, from, TaskStatus.TODO, request.assigneeId()));
        }

        Task saved = taskRepository.save(task);
        log.info("Task {} assigned to {} ({})", saved.getId(), saved.getAssigneeId(), saved.getAssignmentMode());
        eventPublisher.taskAssigned(saved);
        eventPublisher.notify(saved.getAssigneeId(), "New task assigned",
                "You were assigned \"%s\"".formatted(saved.getTitle()), "TASK_ASSIGNED", "/tasks/" + saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAssignmentRecommendation> recommendAssignees(UUID id) {
        Task task = findOrThrow(id);
        TaskAssignmentRecommendationRequest request = new TaskAssignmentRecommendationRequest(
                task.getId(), task.getProject().getId(), task.getRequiredSkills(),
                task.getPriority(), task.getEstimatedHours(), task.getDueDate());
        try {
            return aiRecommendationClient.recommendAssignees(request).getData();
        } catch (FeignException ex) {
            log.warn("ai-service unreachable while recommending assignees for task {}: {}", id, ex.getMessage());
            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR,
                    "The recommendation engine is unavailable right now; you can still assign this task manually");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.teamflow.ai.project.client.AssignmentCandidateResponse> getAssignmentCandidates(UUID id) {
        Task task = findOrThrow(id);
        TaskAssignmentRecommendationRequest request = new TaskAssignmentRecommendationRequest(
                task.getId(), task.getProject().getId(), task.getRequiredSkills(),
                task.getPriority(), task.getEstimatedHours(), task.getDueDate());
        
        List<com.teamflow.ai.project.client.AssignmentCandidateResponse> allCandidates;
        try {
            allCandidates = aiRecommendationClient.getAssignmentCandidates(request).getData();
        } catch (FeignException ex) {
            log.warn("ai-service unreachable while fetching candidates for task {}: {}", id, ex.getMessage());
            throw new BusinessException(ErrorCode.REMOTE_SERVICE_ERROR,
                    "The assignment workspace data is unavailable right now");
        }

        Set<UUID> memberIds = task.getProject().getMembers().stream()
                .map(com.teamflow.ai.project.entity.ProjectMember::getEmployeeId)
                .collect(Collectors.toSet());
        
        memberIds.add(task.getProject().getManagerId());

        return allCandidates.stream()
                .filter(candidate -> memberIds.contains(candidate.employeeId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskCommentResponse addComment(UUID id, AddTaskCommentRequest request, UUID authorId) {
        Task task = findOrThrow(id);
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthorId(authorId);
        comment.setComment(request.comment().trim());

        TaskComment saved = taskCommentRepository.save(comment);
        log.info("Comment added to task {}", id);
        return taskMapper.toResponse(saved, employeeLookupService.tryResolveName(authorId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskCommentResponse> listComments(UUID id, Pageable pageable) {
        Page<TaskComment> page = taskCommentRepository.findAllByTaskIdAndDeletedFalseOrderByCreatedAtDesc(id, pageable);
        return PageResponse.from(page, comment -> taskMapper.toResponse(comment,
                employeeLookupService.tryResolveName(comment.getAuthorId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> listHistory(UUID id) {
        return taskStatusHistoryRepository.findAllByTaskIdOrderByCreatedAtDesc(id).stream()
                .map(history -> taskMapper.toResponse(history, employeeLookupService.tryResolveName(history.getChangedBy())))
                .toList();
    }

    @Override
    @Transactional
    public TaskAttachmentResponse addAttachment(UUID id, AddTaskAttachmentRequest request, UUID uploadedBy) {
        Task task = findOrThrow(id);
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTask(task);
        attachment.setFileName(request.fileName().trim());
        attachment.setFileUrl(request.fileUrl().trim());
        attachment.setUploadedBy(uploadedBy);

        TaskAttachment saved = taskAttachmentRepository.save(attachment);
        log.info("Attachment recorded on task {}", id);
        return taskMapper.toResponse(saved, employeeLookupService.tryResolveName(uploadedBy));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAttachmentResponse> listAttachments(UUID id) {
        return taskAttachmentRepository.findAllByTaskIdOrderByCreatedAtDesc(id).stream()
                .map(attachment -> taskMapper.toResponse(attachment,
                        employeeLookupService.tryResolveName(attachment.getUploadedBy())))
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Task task = findOrThrow(id);
        task.setDeleted(true);
        taskRepository.save(task);
        log.info("Deleted task {}", id);
    }

    @Override
    @Transactional
    public TaskDependencyResponse addDependency(UUID taskId, UUID dependsOnTaskId) {
        if (taskId.equals(dependsOnTaskId)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "A task cannot depend on itself");
        }
        Task task = findOrThrow(taskId);
        Task blockingTask = findOrThrow(dependsOnTaskId);

        if (!task.getProject().getId().equals(blockingTask.getProject().getId())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Dependencies must be within the same project");
        }

        if (taskDependencyRepository.existsByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Dependency already exists");
        }

        // Circular dependency check (DFS)
        checkCircularDependency(dependsOnTaskId, taskId, 0);

        TaskDependency dependency = TaskDependency.builder()
                .taskId(taskId)
                .dependsOnTaskId(dependsOnTaskId)
                .build();
        
        TaskDependency saved = taskDependencyRepository.save(dependency);
        
        return TaskDependencyResponse.builder()
                .id(saved.getId())
                .taskId(saved.getTaskId())
                .dependsOnTaskId(saved.getDependsOnTaskId())
                .dependsOnTaskTitle(blockingTask.getTitle())
                .dependsOnTaskStatus(blockingTask.getStatus().name())
                .build();
    }

    @Override
    @Transactional
    public void removeDependency(UUID taskId, UUID dependsOnTaskId) {
        taskDependencyRepository.deleteByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> listDependencies(UUID taskId) {
        return taskDependencyRepository.findAllByTaskId(taskId).stream()
                .map(dep -> {
                    Task blockingTask = findOrThrow(dep.getDependsOnTaskId());
                    return TaskDependencyResponse.builder()
                            .id(dep.getId())
                            .taskId(dep.getTaskId())
                            .dependsOnTaskId(dep.getDependsOnTaskId())
                            .dependsOnTaskTitle(blockingTask.getTitle())
                            .dependsOnTaskStatus(blockingTask.getStatus().name())
                            .build();
                })
                .toList();
    }

    // ------------------------------------------------------------------

    private void checkCircularDependency(UUID currentTaskId, UUID targetTaskId, int depth) {
        if (depth > 20) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Dependency chain too deep");
        }
        if (currentTaskId.equals(targetTaskId)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Circular dependency detected! This would create an infinite loop.");
        }
        
        List<TaskDependency> deps = taskDependencyRepository.findAllByTaskId(currentTaskId);
        for (TaskDependency dep : deps) {
            checkCircularDependency(dep.getDependsOnTaskId(), targetTaskId, depth + 1);
        }
    }

    private Task findOrThrow(UUID id) {
        return taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Task", id));
    }

    private Sprint resolveSprint(UUID sprintId, UUID projectId) {
        if (sprintId == null) {
            return null;
        }
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(sprintId)
                .orElseThrow(() -> ResourceNotFoundException.of("Sprint", sprintId));
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BusinessException("That sprint does not belong to the specified project");
        }
        return sprint;
    }

    private Set<String> normalizeSkills(Set<String> skills) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String skill : skills) {
            if (skill != null && !skill.isBlank()) {
                normalized.add(skill.trim().toUpperCase());
            }
        }
        return normalized;
    }

    private TaskResponse toResponse(Task task) {
        String assigneeName = employeeLookupService.tryResolveName(task.getAssigneeId());
        String reporterName = employeeLookupService.tryResolveName(task.getReporterId());
        return taskMapper.toResponse(task, assigneeName, reporterName);
    }
}
