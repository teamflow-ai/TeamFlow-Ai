package com.teamflow.ai.project.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.enums.ApprovalType;
import com.teamflow.ai.common.enums.MilestoneStatus;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.approval.dto.ApprovalResponse;
import com.teamflow.ai.project.approval.service.ApprovalService;
import com.teamflow.ai.project.dto.request.CreateMilestoneRequest;
import com.teamflow.ai.project.dto.request.UpdateMilestoneRequest;
import com.teamflow.ai.project.dto.response.MilestoneResponse;
import com.teamflow.ai.project.entity.Milestone;
import com.teamflow.ai.project.entity.Project;
import com.teamflow.ai.project.mapper.MilestoneMapper;
import com.teamflow.ai.project.repository.MilestoneRepository;
import com.teamflow.ai.project.repository.ProjectRepository;
import com.teamflow.ai.project.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneMapper milestoneMapper;
    private final ApprovalService approvalService;

    @Override
    @Transactional
    public MilestoneResponse create(CreateMilestoneRequest request) {
        Project project = projectRepository.findByIdAndDeletedFalse(request.projectId())
                .orElseThrow(() -> ResourceNotFoundException.of("Project", request.projectId()));

        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setTitle(request.title().trim());
        milestone.setDescription(request.description());
        milestone.setDueDate(request.dueDate());
        milestone.setStatus(MilestoneStatus.PLANNED);

        Milestone saved = milestoneRepository.save(milestone);
        log.info("Milestone {} created for project {}", saved.getId(), project.getId());
        return milestoneMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MilestoneResponse update(UUID id, UpdateMilestoneRequest request) {
        Milestone milestone = findOrThrow(id);
        if (milestone.getStatus() == MilestoneStatus.APPROVED) {
            throw new BusinessException("An approved milestone cannot be modified");
        }
        milestone.setTitle(request.title().trim());
        milestone.setDescription(request.description());
        milestone.setDueDate(request.dueDate());
        return milestoneMapper.toResponse(milestoneRepository.save(milestone));
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneResponse get(UUID id) {
        return milestoneMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MilestoneResponse> listForProject(UUID projectId, Pageable pageable) {
        Page<Milestone> page = milestoneRepository.findAllByProjectIdAndDeletedFalse(projectId, pageable);
        return PageResponse.from(page, milestoneMapper::toResponse);
    }

    @Override
    @Transactional
    public ApprovalResponse submitForReview(UUID id, UUID requestedBy, String remarks) {
        Milestone milestone = findOrThrow(id);
        if (milestone.getStatus() == MilestoneStatus.READY_FOR_REVIEW
                || milestone.getStatus() == MilestoneStatus.APPROVED) {
            throw new BusinessException(
                    "Milestone %s is already %s".formatted(id, milestone.getStatus()));
        }

        milestone.setStatus(MilestoneStatus.READY_FOR_REVIEW);
        milestoneRepository.save(milestone);
        log.info("Milestone {} marked ready for review by {}", id, requestedBy);

        return approvalService.request(ApprovalType.MILESTONE, milestone.getId(), milestone.getProject().getId(),
                requestedBy, null, remarks);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Milestone milestone = findOrThrow(id);
        milestone.setDeleted(true);
        milestoneRepository.save(milestone);
        log.info("Deleted milestone {}", id);
    }

    private Milestone findOrThrow(UUID id) {
        return milestoneRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Milestone", id));
    }
}
