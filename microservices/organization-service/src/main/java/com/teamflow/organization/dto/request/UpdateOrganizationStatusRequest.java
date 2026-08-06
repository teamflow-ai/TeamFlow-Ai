package com.teamflow.organization.dto.request;

import com.teamflow.organization.enums.OrganizationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for transitioning an organization's status
 * (e.g. activating, deactivating or suspending it).
 */
@Getter
@Setter
public class UpdateOrganizationStatusRequest {

    @NotNull(message = "Status is required")
    private OrganizationStatus status;

}
