package com.teamflow.ai.project.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.SprintStatus;
import com.teamflow.ai.common.enums.TaskStatus;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.dto.request.CreateSprintRequest;
import com.teamflow.ai.project.dto.request.UpdateSprintRequest;
import com.teamflow.ai.project.dto.request.UpdateSprintStatusRequest;
import com.teamflow.ai.project.dto.response.SprintResponse;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.entity.Sprint;
import com.teamflow.ai.project.entity.Task;
import com.teamflow.ai.project.mapper.SprintMapper;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.repository.SprintRepository;
import com.teamflow.ai.project.repository.TaskRepository;
import com.teamflow.ai.project.service.SprintService;
import com.teamflow.ai.project.client.AiRecommendationClient;
import com.teamflow.ai.project.client.TaskAssignmentRecommendationRequest;
import com.teamflow.ai.project.dto.response.SprintCapacityReport;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SprintMapper sprintMapper;
    private final AiRecommendationClient aiRecommendationClient;

    @Override
    @Transactional
    public SprintResponse create(CreateSprintRequest request) {
        Project project = projectRepository.findByIdAndDeletedFalse(request.projectId())
                .orElseThrow(() -> ResourceNotFoundException.of("Project", request.projectId()));
        validateDateRange(request.startDate(), request.endDate());

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(request.name().trim());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());

        Sprint saved = sprintRepository.save(sprint);
        log.info("Created sprint {} in project {}", saved.getId(), project.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SprintResponse update(UUID id, UpdateSprintRequest request) {
        Sprint sprint = findOrThrow(id);
        validateDateRange(request.startDate(), request.endDate());

        sprint.setName(request.name().trim());
        sprint.setGoal(request.goal());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());

        Sprint saved = sprintRepository.save(sprint);
        log.info("Updated sprint {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SprintResponse> listForProject(UUID projectId, Pageable pageable) {
        Page<Sprint> page = sprintRepository.findAllByProjectIdAndDeletedFalse(projectId, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public SprintResponse updateStatus(UUID id, UpdateSprintStatusRequest request) {
        Sprint sprint = findOrThrow(id);
        if (sprint.getStatus() == SprintStatus.COMPLETED || sprint.getStatus() == SprintStatus.CANCELLED) {
            throw new BusinessException("A %s sprint's status cannot be changed further".formatted(sprint.getStatus()));
        }
        
        sprint.setStatus(request.status());
        
        // Handle Sprint Completion: Move uncompleted tasks to the backlog
        if (request.status() == SprintStatus.COMPLETED) {
            List<Task> tasksInSprint = taskRepository.findAll((root, query, cb) -> 
                cb.and(cb.equal(root.get("sprint").get("id"), id), cb.equal(root.get("deleted"), false))
            );
            
            for (Task t : tasksInSprint) {
                if (!t.getStatus().isTerminal()) {
                    t.setStatus(TaskStatus.BACKLOG);
                    t.setSprint(null);
                    taskRepository.save(t);
                    log.info("Task {} returned to backlog upon sprint completion", t.getId());
                }
            }
        }
        
        Sprint saved = sprintRepository.save(sprint);
        log.info("Sprint {} status changed to {}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Sprint sprint = findOrThrow(id);
        if (taskRepository.countBySprintIdAndDeletedFalse(id) > 0) {
            throw new BusinessException("Cannot remove a sprint that still has tasks assigned to it");
        }
        sprint.setDeleted(true);
        sprintRepository.save(sprint);
        log.info("Deleted sprint {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintCapacityReport getCapacityReport(UUID id) {
        Sprint sprint = findOrThrow(id);
        List<Task> tasks = taskRepository.findAll((root, query, cb) -> 
            cb.and(cb.equal(root.get("sprint").get("id"), id), cb.equal(root.get("deleted"), false))
        );

        java.math.BigDecimal totalEstimated = java.math.BigDecimal.ZERO;
        java.util.Map<UUID, java.math.BigDecimal> assignedHoursMap = new java.util.HashMap<>();
        
        for (Task task : tasks) {
            java.math.BigDecimal est = task.getEstimatedHours() != null ? task.getEstimatedHours() : java.math.BigDecimal.ZERO;
            totalEstimated = totalEstimated.add(est);
            if (task.getAssigneeId() != null) {
                assignedHoursMap.merge(task.getAssigneeId(), est, java.math.BigDecimal::add);
            }
        }

        List<com.teamflow.ai.project.client.AssignmentCandidateResponse> candidates = java.util.List.of();
        try {
            TaskAssignmentRecommendationRequest dummyRequest = new TaskAssignmentRecommendationRequest(
                    null, sprint.getProject().getId(), java.util.Set.of(), null, null, null);
            candidates = aiRecommendationClient.getAssignmentCandidates(dummyRequest).getData();
        } catch (FeignException ex) {
            log.warn("ai-service unreachable while fetching capacity for sprint {}", id);
        }

        java.util.Map<UUID, Integer> capacityMap = new java.util.HashMap<>();
        java.util.Map<UUID, String> nameMap = new java.util.HashMap<>();
        for (com.teamflow.ai.project.client.AssignmentCandidateResponse c : candidates) {
            capacityMap.put(c.employeeId(), c.weeklyCapacityHours());
            nameMap.put(c.employeeId(), c.employeeName());
        }

        int totalTeamCapacity = 0;
        boolean overallOverloaded = false;
        java.util.List<SprintCapacityReport.MemberCapacity> memberReports = new java.util.ArrayList<>();
        
        for (java.util.Map.Entry<UUID, java.math.BigDecimal> entry : assignedHoursMap.entrySet()) {
            UUID empId = entry.getKey();
            java.math.BigDecimal assigned = entry.getValue();
            int cap = capacityMap.getOrDefault(empId, 40); // default to 40 if unknown
            totalTeamCapacity += cap;
            
            boolean overloaded = assigned.doubleValue() > cap;
            if (overloaded) overallOverloaded = true;
            
            memberReports.add(SprintCapacityReport.MemberCapacity.builder()
                    .employeeId(empId)
                    .employeeName(nameMap.getOrDefault(empId, "Unknown Member"))
                    .weeklyCapacity(cap)
                    .assignedHours(assigned)
                    .isOverloaded(overloaded)
                    .build());
        }

        return SprintCapacityReport.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getName())
                .totalEstimatedHours(totalEstimated)
                .totalTeamCapacityHours(totalTeamCapacity)
                .isOverloaded(overallOverloaded)
                .memberCapacities(memberReports)
                .build();
    }

    // ------------------------------------------------------------------

    private Sprint findOrThrow(UUID id) {
        return sprintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Sprint", id));
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException("End date cannot be before the start date");
        }
    }

    private SprintResponse toResponse(Sprint sprint) {
        long taskCount = taskRepository.countBySprintIdAndDeletedFalse(sprint.getId());
        long completedTaskCount = taskRepository.countBySprintIdAndStatusAndDeletedFalse(sprint.getId(), TaskStatus.DONE);
        return sprintMapper.toResponse(sprint, taskCount, completedTaskCount);
    }
}
