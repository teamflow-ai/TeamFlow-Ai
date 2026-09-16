package com.teamflow.ai.project.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.ProjectStatus;
import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.DuplicateResourceException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.approval.dto.ApprovalResponse;
import com.teamflow.ai.project.approval.service.ApprovalService;
import com.teamflow.ai.project.dto.request.AddProjectMemberRequest;
import com.teamflow.ai.project.dto.request.CreateProjectRequest;
import com.teamflow.ai.project.dto.request.UpdateProjectRequest;
import com.teamflow.ai.project.dto.request.UpdateProjectStatusRequest;
import com.teamflow.ai.project.dto.response.ProjectResponse;
import com.teamflow.ai.project.entity.Client;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.ProjectMember;
import com.teamflow.ai.project.mapper.ProjectMapper;
import com.teamflow.ai.project.messaging.ProjectEventPublisher;
import com.teamflow.ai.project.repository.ClientRepository;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.repository.TaskRepository;
import com.teamflow.ai.project.repository.spec.ProjectSpecifications;
import com.teamflow.ai.project.service.EmployeeLookupService;
import com.teamflow.ai.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final TaskRepository taskRepository;
    private final EmployeeLookupService employeeLookupService;
    private final ProjectMapper projectMapper;
    private final ProjectEventPublisher eventPublisher;
    private final ApprovalService approvalService;

    @Override
    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        String code = request.code().trim().toUpperCase();
        if (projectRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
            throw DuplicateResourceException.of("Project", "code", code);
        }
        employeeLookupService.requireExisting(request.managerId());
        validateDateRange(request.startDate(), request.endDate());

        Project project = new Project();
        project.setName(request.name().trim());
        project.setCode(code);
        project.setDescription(request.description());
        project.setClient(resolveClient(request.clientId()));
        project.setManagerId(request.managerId());
        project.setPriority(request.priority() != null ? request.priority() : project.getPriority());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setBudget(request.budget());

        Project saved = projectRepository.save(project);
        log.info("Created project {} ({})", saved.getId(), saved.getCode());
        eventPublisher.projectCreated(saved);
        
        // Auto-assign the manager as a project member and return the updated response
        return this.addMember(saved.getId(), new AddProjectMemberRequest(request.managerId(), "PROJECT_MANAGER", 40));
    }

    @Override
    @Transactional
    public ProjectResponse update(UUID id, UpdateProjectRequest request) {
        Project project = findOrThrow(id);
        employeeLookupService.requireExisting(request.managerId());
        validateDateRange(request.startDate(), request.endDate());

        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setClient(resolveClient(request.clientId()));
        project.setManagerId(request.managerId());
        project.setPriority(request.priority() != null ? request.priority() : project.getPriority());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setBudget(request.budget());

        Project saved = projectRepository.save(project);
        log.info("Updated project {}", saved.getId());
        eventPublisher.projectUpdated(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> search(String query, ProjectStatus status, UUID managerId, Pageable pageable) {
        Specification<Project> spec = Specification.allOf(
                Stream.of(
                        ProjectSpecifications.notDeleted(),
                        ProjectSpecifications.nameOrCodeContains(query),
                        ProjectSpecifications.hasStatus(status),
                        ProjectSpecifications.hasManager(managerId)
                ).filter(Objects::nonNull).collect(Collectors.toList())
        );
        Page<Project> page = projectRepository.findAll(spec, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public ProjectResponse updateStatus(UUID id, UpdateProjectStatusRequest request) {
        Project project = findOrThrow(id);
        if (project.getStatus() == ProjectStatus.COMPLETED || project.getStatus() == ProjectStatus.CANCELLED) {
            throw new BusinessException("A %s project's status cannot be changed further".formatted(project.getStatus()));
        }
        project.setStatus(request.status());
        Project saved = projectRepository.save(project);
        log.info("Project {} status changed to {}", saved.getId(), saved.getStatus());
        if (saved.getStatus() == ProjectStatus.COMPLETED) {
            eventPublisher.projectCompleted(saved);
        } else {
            eventPublisher.projectUpdated(saved);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponse addMember(UUID id, AddProjectMemberRequest request) {
        Project project = findOrThrow(id);
        employeeLookupService.requireExisting(request.employeeId());

        boolean alreadyMember = project.getMembers().stream()
                .anyMatch(member -> member.getEmployeeId().equals(request.employeeId()));
        if (alreadyMember) {
            throw new BusinessException("This employee is already a member of the project");
        }

        Integer hours = request.allocatedHours() != null ? request.allocatedHours() : 40;
        project.getMembers().add(new ProjectMember(request.employeeId(), request.roleOnProject(), Instant.now(), hours));
        Project saved = projectRepository.save(project);
        log.info("Added member {} to project {}", request.employeeId(), id);
        eventPublisher.projectMemberAdded(saved, request.employeeId(), hours);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponse removeMember(UUID id, UUID employeeId) {
        Project project = findOrThrow(id);
        boolean removed = project.getMembers().removeIf(member -> member.getEmployeeId().equals(employeeId));
        if (!removed) {
            throw new BusinessException("This employee is not a member of the project");
        }
        Project saved = projectRepository.save(project);
        log.info("Removed member {} from project {}", employeeId, id);
        eventPublisher.projectMemberRemoved(saved, employeeId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ApprovalResponse requestClosure(UUID id, UUID requestedBy, String remarks) {
        Project project = findOrThrow(id);
        if (project.getStatus() == ProjectStatus.COMPLETED || project.getStatus() == ProjectStatus.CANCELLED) {
            throw new BusinessException("Project %s is already %s".formatted(id, project.getStatus()));
        }
        log.info("Project {} closure requested by {}", id, requestedBy);
        return approvalService.request(ApprovalType.PROJECT_CLOSURE, project.getId(), project.getId(),
                requestedBy, null, remarks);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Project project = findOrThrow(id);
        project.setDeleted(true);
        projectRepository.save(project);
        log.info("Deleted project {}", id);
    }

    // ------------------------------------------------------------------

    private Project findOrThrow(UUID id) {
        return projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", id));
    }

    private Client resolveClient(UUID clientId) {
        if (clientId == null) {
            return null;
        }
        return clientRepository.findByIdAndDeletedFalse(clientId)
                .orElseThrow(() -> ResourceNotFoundException.of("Client", clientId));
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException("End date cannot be before the start date");
        }
    }

    private ProjectResponse toResponse(Project project) {
        String clientName = project.getClient() != null ? project.getClient().getName() : null;
        String managerName = employeeLookupService.tryResolveName(project.getManagerId());
        long totalTasks = taskRepository.countByProjectIdAndDeletedFalse(project.getId());
        long doneTasks = taskRepository.countByProjectIdAndStatusAndDeletedFalse(project.getId(), TaskStatus.DONE);
        int progress = totalTasks == 0 ? 0 : (int) Math.round((doneTasks * 100.0) / totalTasks);
        return projectMapper.toResponse(project, clientName, managerName,
                employeeLookupService::tryResolveName, progress);
    }
}
