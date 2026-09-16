package com.teamflow.ai.identity.dto.response;

import java.util.UUID;

public record TeamMembershipResponse(
        UUID teamId,
        String teamName,
        boolean primary
) {}
