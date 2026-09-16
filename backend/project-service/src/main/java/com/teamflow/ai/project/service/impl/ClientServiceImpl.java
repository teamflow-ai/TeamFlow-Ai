package com.teamflow.ai.project.service.impl;

import com.teamflow.ai.common.dto.PageResponse;
import com.teamflow.ai.common.exception.DuplicateResourceException;
import com.teamflow.ai.common.exception.ResourceNotFoundException;
import com.teamflow.ai.project.dto.request.CreateClientRequest;
import com.teamflow.ai.project.dto.request.UpdateClientRequest;
import com.teamflow.ai.project.dto.response.ClientResponse;
import com.teamflow.ai.project.entity.Client;
import com.teamflow.ai.project.mapper.ClientMapper;
import com.teamflow.ai.project.repository.ClientRepository;
import com.teamflow.ai.project.service.ClientService;
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
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        String code = request.code().trim().toUpperCase();
        if (clientRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
            throw DuplicateResourceException.of("Client", "code", code);
        }
        Client client = new Client();
        client.setName(request.name().trim());
        client.setCode(code);
        client.setContactPerson(request.contactPerson());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client.setCountry(request.country());
        client.setActive(true);

        Client saved = clientRepository.save(client);
        log.info("Created client {} ({})", saved.getId(), saved.getCode());
        return clientMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ClientResponse update(UUID id, UpdateClientRequest request) {
        Client client = findOrThrow(id);
        client.setName(request.name().trim());
        client.setContactPerson(request.contactPerson());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client.setCountry(request.country());
        client.setActive(request.active());

        Client saved = clientRepository.save(client);
        log.info("Updated client {}", saved.getId());
        return clientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse get(UUID id) {
        return clientMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> list(Pageable pageable) {
        Page<Client> page = clientRepository.findAllByDeletedFalse(pageable);
        return PageResponse.from(page, clientMapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Client client = findOrThrow(id);
        client.setDeleted(true);
        clientRepository.save(client);
        log.info("Deleted client {}", id);
    }

    private Client findOrThrow(UUID id) {
        return clientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Client", id));
    }
}
