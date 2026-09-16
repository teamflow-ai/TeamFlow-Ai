package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** ai-service's read model of a meeting, built from {@code MeetingEvent} messages. */
@Document(collection = "meeting_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class MeetingSnapshot {

    @Id
    private String meetingId;

    private String title;
    private String projectId;
    private Instant scheduledAt;
    private String status;
    private Instant updatedAt;
}
