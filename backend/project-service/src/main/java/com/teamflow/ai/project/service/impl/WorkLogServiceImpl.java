package com.teamflow.ai.project.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.dto.request.CreateWorkLogRequest;
import com.teamflow.ai.project.dto.request.UpdateWorkLogRequest;
import com.teamflow.ai.project.dto.response.WorkLogResponse;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.Task;
import com.teamflow.ai.project.entity.WorkLog;
import com.teamflow.ai.project.mapper.WorkLogMapper;
import com.teamflow.ai.project.messaging.ProjectEventPublisher;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.repository.TaskRepository;
import com.teamflow.ai.project.repository.WorkLogRepository;
import com.teamflow.ai.project.service.EmployeeLookupService;
import com.teamflow.ai.project.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkLogServiceImpl implements WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final EmployeeLookupService employeeLookupService;
    private final WorkLogMapper workLogMapper;
    private final ProjectEventPublisher eventPublisher;

    @Override
    @Transactional
    public WorkLogResponse create(CreateWorkLogRequest request, UUID employeeId) {
        Project project = projectRepository.findByIdAndDeletedFalse(request.projectId())
                .orElseThrow(() -> ResourceNotFoundException.of("Project", request.projectId()));
        Task task = resolveTask(request.taskId(), project.getId());

        WorkLog workLog = new WorkLog();
        workLog.setEmployeeId(employeeId);
        workLog.setProject(project);
        workLog.setTask(task);
        workLog.setLogDate(request.logDate());
        workLog.setHours(request.hours());
        workLog.setNotes(request.notes());

        WorkLog saved = workLogRepository.save(workLog);
        adjustTaskActualHours(task, request.hours());
        log.info("Employee {} logged {}h on project {}", employeeId, request.hours(), project.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public WorkLogResponse update(UUID id, UpdateWorkLogRequest request, UUID employeeId) {
        WorkLog workLog = findOrThrow(id);
        requireOwner(workLog, employeeId);

        BigDecimal delta = request.hours().subtract(workLog.getHours());
        workLog.setLogDate(request.logDate());
        workLog.setHours(request.hours());
        workLog.setNotes(request.notes());

        WorkLog saved = workLogRepository.save(workLog);
        adjustTaskActualHours(workLog.getTask(), delta);
        log.info("Updated work log {}", id);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkLogResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkLogResponse> listForEmployee(UUID employeeId, Pageable pageable) {
        Page<WorkLog> page = workLogRepository.findAllByEmployeeIdAndDeletedFalse(employeeId, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkLogResponse> listForProject(UUID projectId, Pageable pageable) {
        Page<WorkLog> page = projectId == null
                ? workLogRepository.findAllByDeletedFalse(pageable)
                : workLogRepository.findAllByProjectIdAndDeletedFalse(projectId, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkLogResponse> listForTask(UUID taskId, Pageable pageable) {
        Page<WorkLog> page = workLogRepository.findAllByTaskIdAndDeletedFalse(taskId, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID employeeId) {
        WorkLog workLog = findOrThrow(id);
        requireOwner(workLog, employeeId);
        workLog.setDeleted(true);
        workLogRepository.save(workLog);
        adjustTaskActualHours(workLog.getTask(), workLog.getHours().negate());
        log.info("Deleted work log {}", id);
    }

    // ------------------------------------------------------------------

    private WorkLog findOrThrow(UUID id) {
        return workLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("WorkLog", id));
    }

    private void requireOwner(WorkLog workLog, UUID employeeId) {
        if (!workLog.getEmployeeId().equals(employeeId)) {
            throw new BusinessException("You can only edit your own work logs");
        }
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

    /** Keeps {@code Task.actualHours} — a workload-scoring input — in step with logged effort. */
    private void adjustTaskActualHours(Task task, BigDecimal delta) {
        if (task == null || delta.signum() == 0) {
            return;
        }
        BigDecimal updated = task.getActualHours().add(delta).max(BigDecimal.ZERO);
        task.setActualHours(updated);
        Task saved = taskRepository.save(task);
        eventPublisher.taskStatusChanged(saved);
    }

    private WorkLogResponse toResponse(WorkLog workLog) {
        return workLogMapper.toResponse(workLog, employeeLookupService.tryResolveName(workLog.getEmployeeId()));
    }
}
