package com.teamflow.ai.identity.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.identity.dto.request.CreateTeamRequest;
import com.teamflow.ai.identity.dto.request.UpdateTeamRequest;
import com.teamflow.ai.identity.dto.response.TeamResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TeamService {

    TeamResponse create(CreateTeamRequest request);

    TeamResponse update(UUID id, UpdateTeamRequest request);

    TeamResponse get(UUID id);

    PageResponse<TeamResponse> list(UUID departmentId, Pageable pageable);

    void delete(UUID id);
}
