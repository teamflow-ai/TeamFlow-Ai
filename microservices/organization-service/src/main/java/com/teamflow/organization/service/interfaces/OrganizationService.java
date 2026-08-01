package com.teamflow.organization.service.interfaces;

import com.teamflow.organization.dto.request.CreateOrganizationRequest;
import com.teamflow.organization.dto.request.UpdateOrganizationRequest;
import com.teamflow.organization.dto.request.UpdateOrganizationStatusRequest;
import com.teamflow.organization.dto.response.OrganizationResponse;
import com.teamflow.organization.enums.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Business operations for managing organizations.
 */
public interface OrganizationService {

    OrganizationResponse createOrganization(CreateOrganizationRequest request);

    OrganizationResponse getOrganizationById(UUID id);

    OrganizationResponse getOrganizationByCode(String code);

    Page<OrganizationResponse> getAllOrganizations(OrganizationStatus status, Pageable pageable);

    Page<OrganizationResponse> searchOrganizationsByName(String name, Pageable pageable);

    OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request);

    OrganizationResponse updateOrganizationStatus(UUID id, UpdateOrganizationStatusRequest request);

    void deleteOrganization(UUID id);

}
