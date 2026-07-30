package com.teamflow.organization.service.impl;

import com.teamflow.organization.dto.request.CreateOrganizationRequest;
import com.teamflow.organization.dto.request.UpdateOrganizationRequest;
import com.teamflow.organization.dto.request.UpdateOrganizationStatusRequest;
import com.teamflow.organization.dto.response.OrganizationResponse;
import com.teamflow.organization.entity.Organization;
import com.teamflow.organization.enums.OrganizationStatus;
import com.teamflow.organization.exception.DuplicateOrganizationException;
import com.teamflow.organization.exception.OrganizationNotFoundException;
import com.teamflow.organization.mapper.OrganizationMapper;
import com.teamflow.organization.repository.OrganizationRepository;
import com.teamflow.organization.service.interfaces.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Default implementation of {@link OrganizationService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        if (organizationRepository.existsByCode(request.getCode())) {
            throw new DuplicateOrganizationException(
                    "An organization with code '" + request.getCode() + "' already exists");
        }
        if (organizationRepository.existsByName(request.getName())) {
            throw new DuplicateOrganizationException(
                    "An organization with name '" + request.getName() + "' already exists");
        }

        Organization organization = organizationMapper.toEntity(request);
        Organization saved = organizationRepository.save(organization);
        log.info("Created organization [id={}, code={}]", saved.getId(), saved.getCode());
        return organizationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID id) {
        return organizationMapper.toResponse(findOrganizationOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationByCode(String code) {
        Organization organization = organizationRepository.findByCode(code)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "Organization not found with code '" + code + "'"));
        return organizationMapper.toResponse(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationResponse> getAllOrganizations(OrganizationStatus status, Pageable pageable) {
        Page<Organization> page = (status != null)
                ? organizationRepository.findByStatus(status, pageable)
                : organizationRepository.findAll(pageable);
        return page.map(organizationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationResponse> searchOrganizationsByName(String name, Pageable pageable) {
        return organizationRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(organizationMapper::toResponse);
    }

    @Override
    public OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request) {
        Organization organization = findOrganizationOrThrow(id);

        if (!organization.getName().equalsIgnoreCase(request.getName())
                && organizationRepository.existsByName(request.getName())) {
            throw new DuplicateOrganizationException(
                    "An organization with name '" + request.getName() + "' already exists");
        }

        organizationMapper.updateEntity(organization, request);
        Organization saved = organizationRepository.save(organization);
        log.info("Updated organization [id={}]", saved.getId());
        return organizationMapper.toResponse(saved);
    }

    @Override
    public OrganizationResponse updateOrganizationStatus(UUID id, UpdateOrganizationStatusRequest request) {
        Organization organization = findOrganizationOrThrow(id);
        organization.setStatus(request.getStatus());
        Organization saved = organizationRepository.save(organization);
        log.info("Updated organization status [id={}, status={}]", saved.getId(), saved.getStatus());
        return organizationMapper.toResponse(saved);
    }

    @Override
    public void deleteOrganization(UUID id) {
        Organization organization = findOrganizationOrThrow(id);
        organizationRepository.delete(organization);
        log.info("Deleted organization [id={}]", id);
    }

    private Organization findOrganizationOrThrow(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "Organization not found with id '" + id + "'"));
    }

}
