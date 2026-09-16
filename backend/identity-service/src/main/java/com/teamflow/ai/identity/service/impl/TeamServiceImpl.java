package com.teamflow.ai.identity.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.exception.BusinessException;
import com.teamflow.ai.common.exception.DuplicateResourceException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.identity.dto.request.CreateTeamRequest;
import com.teamflow.ai.identity.dto.request.UpdateTeamRequest;
import com.teamflow.ai.identity.dto.response.TeamResponse;
import com.teamflow.ai.identity.entity.Department;
import com.teamflow.ai.identity.entity.Employee;
import com.teamflow.ai.identity.entity.Team;
import com.teamflow.ai.identity.mapper.TeamMapper;
import com.teamflow.ai.identity.repository.DepartmentRepository;
import com.teamflow.ai.identity.repository.EmployeeRepository;
import com.teamflow.ai.identity.repository.TeamRepository;
import com.teamflow.ai.identity.service.TeamService;
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
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamMapper teamMapper;

    @Override
    @Transactional
    public TeamResponse create(CreateTeamRequest request) {
        Department department = departmentRepository.findByIdAndDeletedFalse(request.departmentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Department", request.departmentId()));

        if (teamRepository.existsByDepartmentIdAndNameIgnoreCaseAndDeletedFalse(
                department.getId(), request.name().trim())) {
            throw DuplicateResourceException.of("Team", "name", request.name());
        }

        Team team = new Team();
        team.setName(request.name().trim());
        team.setDescription(request.description());
        team.setDepartment(department);
        team.setLeadEmployeeId(resolveLead(request.leadEmployeeId()));

        Team saved = teamRepository.save(team);
        log.info("Created team {} in department {}", saved.getId(), department.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TeamResponse update(UUID id, UpdateTeamRequest request) {
        Team team = findOrThrow(id);

        if (!team.getName().equalsIgnoreCase(request.name().trim())
                && teamRepository.existsByDepartmentIdAndNameIgnoreCaseAndDeletedFalse(
                        team.getDepartment().getId(), request.name().trim())) {
            throw DuplicateResourceException.of("Team", "name", request.name());
        }

        team.setName(request.name().trim());
        team.setDescription(request.description());
        team.setLeadEmployeeId(resolveLead(request.leadEmployeeId()));

        Team saved = teamRepository.save(team);
        log.info("Updated team {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeamResponse> list(UUID departmentId, Pageable pageable) {
        Page<Team> page = departmentId != null
                ? teamRepository.findAllByDepartmentIdAndDeletedFalse(departmentId, pageable)
                : teamRepository.findAllByDeletedFalse(pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Team team = findOrThrow(id);
        if (employeeRepository.countByTeamIdAndDeletedFalse(id) > 0) {
            throw new BusinessException("Cannot remove a team that still has members assigned to it");
        }
        team.setDeleted(true);
        teamRepository.save(team);
        log.info("Deleted team {}", id);
    }

    // ------------------------------------------------------------------

    private Team findOrThrow(UUID id) {
        return teamRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Team", id));
    }

    private UUID resolveLead(UUID leadEmployeeId) {
        if (leadEmployeeId == null) {
            return null;
        }
        if (employeeRepository.findByIdAndDeletedFalse(leadEmployeeId).isEmpty()) {
            throw ResourceNotFoundException.of("Employee", leadEmployeeId);
        }
        return leadEmployeeId;
    }

    private TeamResponse toResponse(Team team) {
        String leadName = team.getLeadEmployeeId() == null ? null
                : employeeRepository.findByIdAndDeletedFalse(team.getLeadEmployeeId())
                        .map(Employee::getFullName).orElse(null);
        long memberCount = employeeRepository.countByTeamIdAndDeletedFalse(team.getId());
        return teamMapper.toResponse(team, leadName, memberCount);
    }
}
