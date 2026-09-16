package com.teamflow.ai.ai.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * One row per approval-request state transition.
 *
 * <p>Doubles as both things the product brief describes separately —
 * "Approval History" (who requested/decided what, when, with what remarks) and
 * the generic "Audit Log" for approval actions — since both are, mechanically,
 * a record of one state transition. See {@code ApprovalEvent}'s javadoc.
 */
@Document(collection = "audit_history")
@Getter
@Setter
@NoArgsConstructor
public class AuditHistory {

    @Id
    private String id;

    /** e.g. "MILESTONE", "TASK_COMPLETION", "PROJECT_CLOSURE", "CLIENT_APPROVAL". */
    @Indexed
    private String entityType;

    @Indexed
    private String entityId;

    private String approvalRequestId;
    private String projectId;
    private String requestedBy;
    private String performedBy;
    private String previousStatus;
    private String newStatus;
    private String remarks;

    @Indexed
    private Instant timestamp;
}
