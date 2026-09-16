package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** A single in-app notification, delivered to one employee. */
@Document(collection = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class NotificationDocument {

    @Id
    private String id;

    @Indexed
    private String recipientEmployeeId;

    private String title;
    private String body;
    private String category;
    private String targetUrl;
    private boolean read = false;
    private Instant createdAt;
}
