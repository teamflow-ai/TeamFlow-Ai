-- =====================================================================
-- TeamFlow.AI :: project-service baseline
-- =====================================================================
-- Only the tables needed for the skeleton to start under
-- `ddl-auto: validate`. The remaining delivery-domain tables (sprints,
-- tasks, bugs, worklogs, meetings) arrive in later migrations alongside
-- their entities, so schema and mapping are always introduced together.
--
-- Cross-service references such as manager_id are plain CHAR(36) columns
-- with no foreign key: identity-service owns those rows in a separate
-- schema, and a database-level FK across a service boundary would couple
-- the two deployments permanently.
--
-- TeamFlow.AI is a single-company system, so there is no organization_id
-- scoping column anywhere in this schema.
-- =====================================================================

CREATE TABLE clients (
    id              CHAR(36)     NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(30)  NOT NULL,
    contact_person  VARCHAR(100),
    email           VARCHAR(150),
    phone           VARCHAR(20),
    country         VARCHAR(100),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_clients PRIMARY KEY (id),
    CONSTRAINT uk_clients_code UNIQUE (code)
) ENGINE = InnoDB;

CREATE INDEX idx_clients_deleted ON clients (deleted);

CREATE TABLE projects (
    id              CHAR(36)     NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    client_id       CHAR(36),
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(30)  NOT NULL,
    description     VARCHAR(2000),
    -- A project cannot start without a manager; enforced in the service layer
    -- because the referenced employee lives in another service's schema.
    manager_id      CHAR(36)     NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PLANNING',
    priority        VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    start_date      DATE,
    end_date        DATE,
    budget          DECIMAL(15, 2),
    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT uk_projects_code UNIQUE (code),
    CONSTRAINT fk_projects_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT ck_projects_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
) ENGINE = InnoDB;

CREATE INDEX idx_projects_manager ON projects (manager_id);
CREATE INDEX idx_projects_status  ON projects (status);
CREATE INDEX idx_projects_deleted ON projects (deleted);
