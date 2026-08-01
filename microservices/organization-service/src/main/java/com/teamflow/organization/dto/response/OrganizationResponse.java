package com.teamflow.organization.dto.response;

import com.teamflow.organization.enums.OrganizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Representation of an organization returned to API clients.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private UUID id;
    private String name;
    private String code;
    private String description;
    private String industry;
    private String email;
    private String phone;
    private String website;
    private String logoUrl;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String timezone;
    private Long ownerId;
    private OrganizationStatus status;
    private Instant createdAt;
    private Instant updatedAt;

}
