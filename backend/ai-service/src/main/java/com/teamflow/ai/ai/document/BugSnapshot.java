package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** ai-service's read model of a bug, built from {@code BugEvent} messages. */
@Document(collection = "bug_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class BugSnapshot {

    @Id
    private String bugId;

    private String title;
    private String projectId;
    private String assigneeId;
    private String status;
    private String severity;
    private Instant updatedAt;
}
