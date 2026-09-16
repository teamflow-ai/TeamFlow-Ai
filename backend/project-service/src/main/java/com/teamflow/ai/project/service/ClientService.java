package com.teamflow.ai.project.service;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.project.dto.request.CreateClientRequest;
import com.teamflow.ai.project.dto.request.UpdateClientRequest;
import com.teamflow.ai.project.dto.response.ClientResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClientService {

    ClientResponse create(CreateClientRequest request);

    ClientResponse update(UUID id, UpdateClientRequest request);

    ClientResponse get(UUID id);

    PageResponse<ClientResponse> list(Pageable pageable);

    void delete(UUID id);
}
