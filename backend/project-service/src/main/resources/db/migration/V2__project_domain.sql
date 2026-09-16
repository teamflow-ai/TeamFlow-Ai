-- =====================================================================
-- TeamFlow.AI :: project-service delivery domain
-- =====================================================================
-- Adds sprints, tasks, bugs, work logs, meetings and project membership on
-- top of the V1 baseline (clients, projects).
--
-- Cross-service references (assignee_id, reporter_id, employee_id, and
-- similar) are plain CHAR(36) columns with no foreign key, for the same
-- reason documented in V1: identity-service owns those rows in a separate
-- schema and a database-level FK across a service boundary would couple
-- the two deployments permanently.
-- =====================================================================

CREATE TABLE sprints (
    id         CHAR(36)     NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    project_id CHAR(36)     NOT NULL,
    name       VARCHAR(150) NOT NULL,
    goal       VARCHAR(1000),
    status     VARCHAR(20)  NOT NULL DEFAULT 'PLANNED',
    start_date DATE,
    end_date   DATE,
    CONSTRAINT pk_sprints PRIMARY KEY (id),
    CONSTRAINT fk_sprints_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT ck_sprints_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
) ENGINE = InnoDB;

CREATE INDEX idx_sprints_project ON sprints (project_id);
CREATE INDEX idx_sprints_status  ON sprints (status);
CREATE INDEX idx_sprints_deleted ON sprints (deleted);

CREATE TABLE tasks (
    id               CHAR(36)     NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 0,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    project_id       CHAR(36)     NOT NULL,
    sprint_id        CHAR(36),
    title            VARCHAR(200) NOT NULL,
    description      VARCHAR(4000),
    assignee_id      CHAR(36),
    reporter_id      CHAR(36),
    status           VARCHAR(20)  NOT NULL DEFAULT 'BACKLOG',
    priority         VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    due_date         DATE,
    estimated_hours  DECIMAL(6, 2),
    actual_hours     DECIMAL(6, 2) NOT NULL DEFAULT 0,
    assignment_mode  VARCHAR(10)  NOT NULL DEFAULT 'MANUAL',
    CONSTRAINT pk_tasks PRIMARY KEY (id),
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_tasks_sprint  FOREIGN KEY (sprint_id)  REFERENCES sprints (id)
) ENGINE = InnoDB;

CREATE INDEX idx_tasks_project  ON tasks (project_id);
CREATE INDEX idx_tasks_sprint   ON tasks (sprint_id);
CREATE INDEX idx_tasks_assignee ON tasks (assignee_id);
CREATE INDEX idx_tasks_status   ON tasks (status);
CREATE INDEX idx_tasks_priority ON tasks (priority);
CREATE INDEX idx_tasks_due_date ON tasks (due_date);
CREATE INDEX idx_tasks_deleted  ON tasks (deleted);

-- Skills the task calls for, so the workload/assignment scorer can match them
-- against an employee's skill set the same way it already does for hiring.
CREATE TABLE task_required_skills (
    task_id CHAR(36)    NOT NULL,
    skill   VARCHAR(60) NOT NULL,
    CONSTRAINT pk_task_required_skills PRIMARY KEY (task_id, skill),
    CONSTRAINT fk_task_required_skills_task FOREIGN KEY (task_id) REFERENCES tasks (id)
) ENGINE = InnoDB;

CREATE TABLE task_comments (
    id         CHAR(36)     NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    task_id    CHAR(36)     NOT NULL,
    author_id  CHAR(36)     NOT NULL,
    comment    VARCHAR(2000) NOT NULL,
    CONSTRAINT pk_task_comments PRIMARY KEY (id),
    CONSTRAINT fk_task_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id)
) ENGINE = InnoDB;

CREATE INDEX idx_task_comments_task ON task_comments (task_id);

-- Append-only audit trail of status transitions. Written by the service layer on
-- every transition, never edited, which is why it carries no `deleted` flag.
CREATE TABLE task_status_history (
    id          CHAR(36)    NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    task_id     CHAR(36)    NOT NULL,
    from_status VARCHAR(20),
    to_status   VARCHAR(20) NOT NULL,
    changed_by  CHAR(36),
    CONSTRAINT pk_task_status_history PRIMARY KEY (id),
    CONSTRAINT fk_task_status_history_task FOREIGN KEY (task_id) REFERENCES tasks (id)
) ENGINE = InnoDB;

CREATE INDEX idx_task_status_history_task ON task_status_history (task_id);

-- Metadata only: no binary content is stored by this service. `file_url` points at
-- wherever the client uploaded the object (e.g. pre-signed object storage).
CREATE TABLE task_attachments (
    id          CHAR(36)     NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    task_id     CHAR(36)     NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    file_url    VARCHAR(1000) NOT NULL,
    uploaded_by CHAR(36)     NOT NULL,
    CONSTRAINT pk_task_attachments PRIMARY KEY (id),
    CONSTRAINT fk_task_attachments_task FOREIGN KEY (task_id) REFERENCES tasks (id)
) ENGINE = InnoDB;

CREATE INDEX idx_task_attachments_task ON task_attachments (task_id);

CREATE TABLE bugs (
    id           CHAR(36)     NOT NULL,
    version      BIGINT       NOT NULL DEFAULT 0,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    project_id   CHAR(36)     NOT NULL,
    task_id      CHAR(36),
    title        VARCHAR(200) NOT NULL,
    description  VARCHAR(4000),
    severity     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    reported_by  CHAR(36)     NOT NULL,
    assignee_id  CHAR(36),
    resolution   VARCHAR(2000),
    resolved_at  DATETIME(6),
    CONSTRAINT pk_bugs PRIMARY KEY (id),
    CONSTRAINT fk_bugs_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_bugs_task    FOREIGN KEY (task_id)    REFERENCES tasks (id)
) ENGINE = InnoDB;

CREATE INDEX idx_bugs_project  ON bugs (project_id);
CREATE INDEX idx_bugs_assignee ON bugs (assignee_id);
CREATE INDEX idx_bugs_status   ON bugs (status);
CREATE INDEX idx_bugs_severity ON bugs (severity);
CREATE INDEX idx_bugs_deleted  ON bugs (deleted);

CREATE TABLE work_logs (
    id          CHAR(36)     NOT NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    employee_id CHAR(36)     NOT NULL,
    task_id     CHAR(36),
    project_id  CHAR(36)     NOT NULL,
    log_date    DATE         NOT NULL,
    hours       DECIMAL(4, 2) NOT NULL,
    notes       VARCHAR(1000),
    CONSTRAINT pk_work_logs PRIMARY KEY (id),
    CONSTRAINT fk_work_logs_task    FOREIGN KEY (task_id)    REFERENCES tasks (id),
    CONSTRAINT fk_work_logs_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT ck_work_logs_hours CHECK (hours > 0 AND hours <= 24)
) ENGINE = InnoDB;

CREATE INDEX idx_work_logs_employee  ON work_logs (employee_id);
CREATE INDEX idx_work_logs_task      ON work_logs (task_id);
CREATE INDEX idx_work_logs_project   ON work_logs (project_id);
CREATE INDEX idx_work_logs_log_date  ON work_logs (log_date);

CREATE TABLE meetings (
    id                CHAR(36)     NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 0,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NOT NULL,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    project_id        CHAR(36),
    title             VARCHAR(200) NOT NULL,
    agenda            VARCHAR(2000),
    scheduled_at      DATETIME(6)  NOT NULL,
    duration_minutes  INT          NOT NULL DEFAULT 30,
    organizer_id      CHAR(36)     NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    notes             VARCHAR(4000),
    CONSTRAINT pk_meetings PRIMARY KEY (id),
    CONSTRAINT fk_meetings_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE = InnoDB;

CREATE INDEX idx_meetings_project      ON meetings (project_id);
CREATE INDEX idx_meetings_scheduled_at ON meetings (scheduled_at);
CREATE INDEX idx_meetings_status       ON meetings (status);

CREATE TABLE meeting_participants (
    meeting_id  CHAR(36) NOT NULL,
    employee_id CHAR(36) NOT NULL,
    CONSTRAINT pk_meeting_participants PRIMARY KEY (meeting_id, employee_id),
    CONSTRAINT fk_meeting_participants_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id)
) ENGINE = InnoDB;

-- Team members on a project, distinct from `projects.manager_id` (the one person
-- accountable for delivery). A project can have zero members recorded here yet
-- still exist, which is why this is a separate table rather than a required field.
CREATE TABLE project_members (
    project_id      CHAR(36)    NOT NULL,
    employee_id     CHAR(36)    NOT NULL,
    role_on_project VARCHAR(50),
    added_at        DATETIME(6) NOT NULL,
    CONSTRAINT pk_project_members PRIMARY KEY (project_id, employee_id),
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE = InnoDB;
