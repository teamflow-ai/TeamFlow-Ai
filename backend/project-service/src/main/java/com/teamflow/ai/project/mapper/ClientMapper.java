package com.teamflow.ai.project.mapper;

import com.teamflow.ai.project.dto.response.ClientResponse;
import com.teamflow.ai.project.entity.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientResponse toResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .code(client.getCode())
                .contactPerson(client.getContactPerson())
                .email(client.getEmail())
                .phone(client.getPhone())
                .country(client.getCountry())
                .active(client.isActive())
                .build();
    }
}
