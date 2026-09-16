package com.teamflow.ai.project.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.BugStatus;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.dto.request.AssignBugRequest;
import com.teamflow.ai.project.dto.request.CreateBugRequest;
import com.teamflow.ai.project.dto.request.UpdateBugRequest;
import com.teamflow.ai.project.dto.request.UpdateBugStatusRequest;
import com.teamflow.ai.project.dto.response.BugResponse;
import com.teamflow.ai.project.entity.Bug;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.Task;
import com.teamflow.ai.project.mapper.BugMapper;
import com.teamflow.ai.project.messaging.ProjectEventPublisher;
import com.teamflow.ai.project.repository.BugRepository;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.repository.TaskRepository;
import com.teamflow.ai.project.service.BugService;
import com.teamflow.ai.project.service.EmployeeLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final EmployeeLookupService employeeLookupService;
    private final BugMapper bugMapper;
    private final ProjectEventPublisher eventPublisher;

    @Override
    @Transactional
    public BugResponse create(CreateBugRequest request, UUID reportedBy) {
        Project project = projectRepository.findByIdAndDeletedFalse(request.projectId())
                .orElseThrow(() -> ResourceNotFoundException.of("Project", request.projectId()));
        Task task = resolveTask(request.taskId(), project.getId());
        if (request.assigneeId() != null) {
            employeeLookupService.requireExisting(request.assigneeId());
        }

        Bug bug = new Bug();
        bug.setProject(project);
        bug.setTask(task);
        bug.setTitle(request.title().trim());
        bug.setDescription(request.description());
        bug.setSeverity(request.severity() != null ? request.severity() : bug.getSeverity());
        bug.setReportedBy(reportedBy);
        bug.setAssigneeId(request.assigneeId());

        Bug saved = bugRepository.save(bug);
        log.info("Reported bug {} in project {}", saved.getId(), project.getId());
        eventPublisher.bugReported(saved);
        if (saved.getAssigneeId() != null) {
            eventPublisher.bugAssigned(saved);
            eventPublisher.notify(saved.getAssigneeId(), "Bug assigned",
                    "You were assigned bug \"%s\"".formatted(saved.getTitle()), "BUG_ASSIGNED", "/bugs/" + saved.getId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BugResponse update(UUID id, UpdateBugRequest request) {
        Bug bug = findOrThrow(id);
        bug.setTitle(request.title().trim());
        bug.setDescription(request.description());
        bug.setSeverity(request.severity() != null ? request.severity() : bug.getSeverity());

        Bug saved = bugRepository.save(bug);
        log.info("Updated bug {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BugResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BugResponse> listForProject(UUID projectId, Pageable pageable) {
        Page<Bug> page = bugRepository.findAllByProjectIdAndDeletedFalse(projectId, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public BugResponse assign(UUID id, AssignBugRequest request) {
        Bug bug = findOrThrow(id);
        employeeLookupService.requireExisting(request.assigneeId());
        bug.setAssigneeId(request.assigneeId());
        if (bug.getStatus() == BugStatus.OPEN) {
            bug.setStatus(BugStatus.IN_PROGRESS);
        }

        Bug saved = bugRepository.save(bug);
        log.info("Bug {} assigned to {}", saved.getId(), saved.getAssigneeId());
        eventPublisher.bugAssigned(saved);
        eventPublisher.notify(saved.getAssigneeId(), "Bug assigned",
                "You were assigned bug \"%s\"".formatted(saved.getTitle()), "BUG_ASSIGNED", "/bugs/" + saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BugResponse updateStatus(UUID id, UpdateBugStatusRequest request) {
        Bug bug = findOrThrow(id);
        bug.setStatus(request.status());
        if (request.status() == BugStatus.RESOLVED || request.status() == BugStatus.CLOSED) {
            bug.setResolution(request.resolution());
            bug.setResolvedAt(Instant.now());
        }

        Bug saved = bugRepository.save(bug);
        log.info("Bug {} status changed to {}", saved.getId(), saved.getStatus());
        if (request.status() == BugStatus.RESOLVED) {
            eventPublisher.bugResolved(saved);
            eventPublisher.notify(saved.getReportedBy(), "Bug resolved",
                    "\"%s\" was marked resolved".formatted(saved.getTitle()), "BUG_RESOLVED", "/bugs/" + saved.getId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Bug bug = findOrThrow(id);
        bug.setDeleted(true);
        bugRepository.save(bug);
        log.info("Deleted bug {}", id);
    }

    // ------------------------------------------------------------------

    private Bug findOrThrow(UUID id) {
        return bugRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Bug", id));
    }

    private Task resolveTask(UUID taskId, UUID projectId) {
        if (taskId == null) {
            return null;
        }
        Task task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> ResourceNotFoundException.of("Task", taskId));
        if (!task.getProject().getId().equals(projectId)) {
            throw new BusinessException("That task does not belong to the specified project");
        }
        return task;
    }

    private BugResponse toResponse(Bug bug) {
        String reportedByName = employeeLookupService.tryResolveName(bug.getReportedBy());
        String assigneeName = employeeLookupService.tryResolveName(bug.getAssigneeId());
        return bugMapper.toResponse(bug, reportedByName, assigneeName);
    }
}
