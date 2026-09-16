package com.teamflow.ai.ai.mapper;

import com.teamflow.ai.ai.document.NotificationDocument;
import com.teamflow.ai.ai.dto.response.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(NotificationDocument document) {
        return NotificationResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .body(document.getBody())
                .category(document.getCategory())
                .targetUrl(document.getTargetUrl())
                .read(document.isRead())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
