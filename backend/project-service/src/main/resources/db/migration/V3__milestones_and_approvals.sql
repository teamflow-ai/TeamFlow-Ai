-- =====================================================================
-- TeamFlow.AI :: project-service — Milestones and the generic Approval
-- Workflow Engine.
-- =====================================================================

CREATE TABLE milestones (
    id          CHAR(36)     NOT NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    project_id  CHAR(36)     NOT NULL,
    title       VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    due_date    DATE,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PLANNED',
    CONSTRAINT pk_milestones PRIMARY KEY (id),
    CONSTRAINT fk_milestones_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE = InnoDB;

CREATE INDEX idx_milestones_project ON milestones (project_id);
CREATE INDEX idx_milestones_status  ON milestones (status);
CREATE INDEX idx_milestones_deleted ON milestones (deleted);

-- One table for every approval type (milestone, task completion, project
-- closure, client approval, and any future type) — see ApprovalRequest's
-- javadoc for why this is deliberately generic rather than per-module.
CREATE TABLE approval_requests (
    id                  CHAR(36)     NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 0,
    deleted             BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          DATETIME(6)  NOT NULL,
    updated_at          DATETIME(6)  NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    approval_type       VARCHAR(30)  NOT NULL,
    reference_entity_id CHAR(36)     NOT NULL,
    project_id          CHAR(36),
    requested_by        CHAR(36)     NOT NULL,
    approver_id         CHAR(36),
    status              VARCHAR(25)  NOT NULL DEFAULT 'PENDING',
    remarks             VARCHAR(1000),
    requested_date      DATETIME(6)  NOT NULL,
    approved_date       DATETIME(6),
    rejected_date       DATETIME(6),
    CONSTRAINT pk_approval_requests PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE INDEX idx_approval_requests_type_status ON approval_requests (approval_type, status);
CREATE INDEX idx_approval_requests_reference   ON approval_requests (reference_entity_id);
CREATE INDEX idx_approval_requests_project     ON approval_requests (project_id);
CREATE INDEX idx_approval_requests_approver    ON approval_requests (approver_id);
