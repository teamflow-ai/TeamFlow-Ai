package com.teamflow.ai.identity.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.exception.DuplicateResourceException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.identity.dto.request.CreateEmployeeRequest;
import com.teamflow.ai.identity.dto.request.UpdateEmployeeRequest;
import com.teamflow.ai.identity.dto.response.EmployeeResponse;
import com.teamflow.ai.identity.entity.Department;
import com.teamflow.ai.identity.entity.Employee;
import com.teamflow.ai.identity.entity.Team;
import com.teamflow.ai.identity.mapper.EmployeeMapper;
import com.teamflow.ai.identity.messaging.EmployeeEventPublisher;
import com.teamflow.ai.identity.repository.DepartmentRepository;
import com.teamflow.ai.identity.repository.EmployeeRepository;
import com.teamflow.ai.identity.repository.TeamRepository;
import com.teamflow.ai.identity.repository.spec.EmployeeSpecifications;
import com.teamflow.ai.identity.service.EmployeeService;
import com.teamflow.ai.identity.util.EmployeeCodeGenerator;
import com.teamflow.ai.common.constant.RoleNames;
import com.teamflow.ai.identity.entity.Role;
import com.teamflow.ai.identity.entity.User;
import com.teamflow.ai.identity.repository.RoleRepository;
import com.teamflow.ai.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final EmployeeCodeGenerator employeeCodeGenerator;
    private final EmployeeMapper employeeMapper;
    private final EmployeeEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        String workEmail = request.workEmail().trim().toLowerCase();
        if (employeeRepository.existsByWorkEmailIgnoreCaseAndDeletedFalse(workEmail)) {
            throw DuplicateResourceException.of("Employee", "workEmail", workEmail);
        }

        Employee employee = new Employee();
        employee.setEmployeeCode(employeeCodeGenerator.next());
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setWorkEmail(workEmail);
        employee.setPhone(request.phone());
        if (request.designation() != null && request.designation().trim().equalsIgnoreCase("Super Admin")) {
            throw new com.teamflow.ai.common.exception.BusinessException("Cannot set designation to 'Super Admin'");
        }
        employee.setDesignation(request.designation());
        employee.setDateOfJoining(request.dateOfJoining());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setDepartment(resolveDepartment(request.departmentId()));
        updateTeams(employee, request.teamId(), request.secondaryTeamIds());
        employee.setManagerId(resolveManager(request.managerId()));
        if (request.skills() != null) {
            employee.setSkills(normalizeSkills(request.skills()));
        }
        if (request.weeklyCapacityHours() != null) {
            employee.setWeeklyCapacityHours(request.weeklyCapacityHours());
        }
        employee.setYearsOfExperience(request.yearsOfExperience());
        employee.setActive(true);

        Employee saved = employeeRepository.save(employee);
        log.info("Created employee {} ({})", saved.getId(), saved.getEmployeeCode());

        // Provision User Account
        String roleName = request.roleName();
        if (roleName == null || roleName.trim().isEmpty()) {
            roleName = RoleNames.DEVELOPER;
        } else {
            roleName = roleName.trim().toUpperCase();
        }
        
        Role role = roleRepository.findByNameWithPermissions(roleName)
                .orElseThrow(() -> ResourceNotFoundException.of("Role", request.roleName()));

        User user = new User();
        user.setEmail(workEmail);
        user.setPasswordHash(passwordEncoder.encode("Welcome@123"));
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setPasswordChangedAt(Instant.now());
        user.setEmployee(saved);
        userRepository.save(user);

        eventPublisher.employeeCreated(saved);
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse update(UUID id, UpdateEmployeeRequest request) {
        Employee employee = findActiveOrThrow(id);

        String workEmail = request.workEmail().trim().toLowerCase();
        if (!workEmail.equalsIgnoreCase(employee.getWorkEmail())
                && employeeRepository.existsByWorkEmailIgnoreCaseAndDeletedFalse(workEmail)) {
            throw DuplicateResourceException.of("Employee", "workEmail", workEmail);
        }

        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setWorkEmail(workEmail);
        employee.setPhone(request.phone());
        if (request.designation() != null && request.designation().trim().equalsIgnoreCase("Super Admin")) {
            throw new com.teamflow.ai.common.exception.BusinessException("Cannot set designation to 'Super Admin'");
        }
        employee.setDesignation(request.designation());
        employee.setDateOfJoining(request.dateOfJoining());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setDepartment(resolveDepartment(request.departmentId()));
        updateTeams(employee, request.teamId(), request.secondaryTeamIds());
        employee.setManagerId(resolveManager(request.managerId()));
        if (request.skills() != null) {
            employee.setSkills(normalizeSkills(request.skills()));
        }
        if (request.weeklyCapacityHours() != null) {
            employee.setWeeklyCapacityHours(request.weeklyCapacityHours());
        }
        employee.setYearsOfExperience(request.yearsOfExperience());

        Employee saved = employeeRepository.save(employee);
        
        // Update User Account Role if provided
        if (request.roleName() != null && !request.roleName().trim().isEmpty()) {
            String roleName = request.roleName().trim().toUpperCase();
            userRepository.findActiveByEmployeeIdWithRole(saved.getId()).ifPresent(user -> {
                if (!user.getRole().getName().equals(roleName)) {
                    Role newRole = roleRepository.findByNameWithPermissions(roleName)
                            .orElseThrow(() -> ResourceNotFoundException.of("Role", roleName));
                    user.setRole(newRole);
                    userRepository.save(user);
                    log.info("Updated role for user {} to {}", user.getId(), roleName);
                }
            });
        }

        log.info("Updated employee {}", saved.getId());
        eventPublisher.employeeUpdated(saved);
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse get(UUID id) {
        Employee employee = findActiveOrThrow(id);
        String managerName = resolveManagerName(employee.getManagerId());
        return employeeMapper.toResponse(employee, managerName);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(String query, UUID departmentId, UUID teamId, UUID managerId,
                                                 Boolean active, String skill, Pageable pageable) {
        Specification<Employee> spec = Specification.allOf(
                Stream.of(
                        EmployeeSpecifications.notDeleted(),
                        EmployeeSpecifications.nameOrEmailContains(query),
                        EmployeeSpecifications.hasDepartment(departmentId),
                        EmployeeSpecifications.hasTeam(teamId),
                        EmployeeSpecifications.hasManager(managerId),
                        EmployeeSpecifications.isActive(active),
                        EmployeeSpecifications.hasSkill(skill)
                ).filter(Objects::nonNull).collect(Collectors.toList())
        );

        Page<Employee> page = employeeRepository.findAll(spec, pageable);
        return PageResponse.from(page, employeeMapper::toResponse);
    }

    @Override
    @Transactional
    public EmployeeResponse setActive(UUID id, boolean active) {
        Employee employee = findActiveOrThrow(id);
        employee.setActive(active);
        Employee saved = employeeRepository.save(employee);
        log.info("Employee {} marked {}", saved.getId(), active ? "active" : "inactive");
        eventPublisher.employeeUpdated(saved);
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Employee employee = findActiveOrThrow(id);
        employee.setDeleted(true);
        employee.setActive(false);
        employeeRepository.save(employee);
        log.info("Deleted employee {}", employee.getId());
        eventPublisher.employeeDeleted(employee);
    }

    // ------------------------------------------------------------------

    private Employee findActiveOrThrow(UUID id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Employee", id));
    }

    private Department resolveDepartment(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", departmentId));
    }

    private Team resolveTeam(UUID teamId) {
        if (teamId == null) {
            return null;
        }
        return teamRepository.findByIdAndDeletedFalse(teamId)
                .orElseThrow(() -> ResourceNotFoundException.of("Team", teamId));
    }

    private void updateTeams(Employee employee, UUID primaryTeamId, Set<UUID> secondaryTeamIds) {
        if (employee.getTeams() == null) {
            employee.setTeams(new java.util.LinkedHashSet<>());
        } else {
            employee.getTeams().clear();
        }
        
        if (primaryTeamId != null) {
            Team primaryTeam = resolveTeam(primaryTeamId);
            employee.getTeams().add(new com.teamflow.ai.identity.entity.EmployeeTeamMembership(employee, primaryTeam, true));
        }
        
        if (secondaryTeamIds != null) {
            for (UUID secId : secondaryTeamIds) {
                if (secId != null && !secId.equals(primaryTeamId)) {
                    Team secTeam = resolveTeam(secId);
                    employee.getTeams().add(new com.teamflow.ai.identity.entity.EmployeeTeamMembership(employee, secTeam, false));
                }
            }
        }
    }

    private UUID resolveManager(UUID managerId) {
        if (managerId == null) {
            return null;
        }
        if (employeeRepository.findByIdAndDeletedFalse(managerId).isEmpty()) {
            throw ResourceNotFoundException.of("Employee", managerId);
        }
        return managerId;
    }

    private String resolveManagerName(UUID managerId) {
        if (managerId == null) {
            return null;
        }
        return employeeRepository.findByIdAndDeletedFalse(managerId)
                .map(Employee::getFullName)
                .orElse(null);
    }

    private LinkedHashSet<String> normalizeSkills(Set<String> skills) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String skill : skills) {
            if (skill != null && !skill.isBlank()) {
                normalized.add(skill.trim().toUpperCase());
            }
        }
        return normalized;
    }
}
