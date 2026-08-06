package com.teamflow.organization.mapper;

import com.teamflow.organization.dto.request.CreateOrganizationRequest;
import com.teamflow.organization.dto.request.UpdateOrganizationRequest;
import com.teamflow.organization.dto.response.OrganizationResponse;
import com.teamflow.organization.entity.Organization;
import com.teamflow.organization.enums.OrganizationStatus;
import org.springframework.stereotype.Component;

/**
 * Converts between {@link Organization} entities and their request/response DTOs.
 */
@Component
public class OrganizationMapper {

    public Organization toEntity(CreateOrganizationRequest request) {
        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setCode(request.getCode());
        organization.setDescription(request.getDescription());
        organization.setIndustry(request.getIndustry());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setWebsite(request.getWebsite());
        organization.setLogoUrl(request.getLogoUrl());
        organization.setAddressLine1(request.getAddressLine1());
        organization.setAddressLine2(request.getAddressLine2());
        organization.setCity(request.getCity());
        organization.setState(request.getState());
        organization.setCountry(request.getCountry());
        organization.setPostalCode(request.getPostalCode());
        organization.setTimezone(request.getTimezone());
        organization.setOwnerId(request.getOwnerId());
        organization.setStatus(OrganizationStatus.ACTIVE);
        return organization;
    }

    /**
     * Applies the editable fields of {@code request} onto {@code organization} in place.
     * {@code code} is immutable and is never modified here.
     */
    public void updateEntity(Organization organization, UpdateOrganizationRequest request) {
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setIndustry(request.getIndustry());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setWebsite(request.getWebsite());
        organization.setLogoUrl(request.getLogoUrl());
        organization.setAddressLine1(request.getAddressLine1());
        organization.setAddressLine2(request.getAddressLine2());
        organization.setCity(request.getCity());
        organization.setState(request.getState());
        organization.setCountry(request.getCountry());
        organization.setPostalCode(request.getPostalCode());
        organization.setTimezone(request.getTimezone());
        if (request.getOwnerId() != null) {
            organization.setOwnerId(request.getOwnerId());
        }
    }

    public OrganizationResponse toResponse(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .code(organization.getCode())
                .description(organization.getDescription())
                .industry(organization.getIndustry())
                .email(organization.getEmail())
                .phone(organization.getPhone())
                .website(organization.getWebsite())
                .logoUrl(organization.getLogoUrl())
                .addressLine1(organization.getAddressLine1())
                .addressLine2(organization.getAddressLine2())
                .city(organization.getCity())
                .state(organization.getState())
                .country(organization.getCountry())
                .postalCode(organization.getPostalCode())
                .timezone(organization.getTimezone())
                .ownerId(organization.getOwnerId())
                .status(organization.getStatus())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }

}
