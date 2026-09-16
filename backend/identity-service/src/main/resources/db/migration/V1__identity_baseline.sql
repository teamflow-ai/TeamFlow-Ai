-- =====================================================================
-- TeamFlow.AI :: identity-service baseline schema
-- =====================================================================
-- Conventions applied throughout:
--   * Primary keys are CHAR(36) UUIDs, matching @JdbcTypeCode(VARCHAR) on BaseEntity.
--   * Every table carries version / deleted / audit columns from AuditableEntity.
--   * Deletes are soft; the `deleted` flag is indexed because every query filters it.
--   * TeamFlow.AI is a single-company system: there is no organization/tenant
--     table, and natural keys (department code, employee code) are unique
--     company-wide rather than scoped to a tenant.
-- =====================================================================

CREATE TABLE permissions (
    id          CHAR(36)     NOT NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    category    VARCHAR(50),
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uk_permissions_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE roles (
    id          CHAR(36)     NOT NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    system_role BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE role_permissions (
    role_id       CHAR(36) NOT NULL,
    permission_id CHAR(36) NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT uk_role_permissions UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role       FOREIGN KEY (role_id)       REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE = InnoDB;

CREATE TABLE departments (
    id               CHAR(36)     NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 0,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    name             VARCHAR(100) NOT NULL,
    code             VARCHAR(20)  NOT NULL,
    description      VARCHAR(500),
    head_employee_id CHAR(36),
    annual_budget    DECIMAL(15, 2),
    CONSTRAINT pk_departments PRIMARY KEY (id),
    CONSTRAINT uk_departments_code UNIQUE (code)
) ENGINE = InnoDB;

CREATE INDEX idx_departments_deleted ON departments (deleted);

CREATE TABLE teams (
    id               CHAR(36)     NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 0,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    name             VARCHAR(100) NOT NULL,
    description      VARCHAR(500),
    department_id    CHAR(36)     NOT NULL,
    lead_employee_id CHAR(36),
    CONSTRAINT pk_teams PRIMARY KEY (id),
    CONSTRAINT uk_teams_department_name UNIQUE (department_id, name),
    CONSTRAINT fk_teams_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE = InnoDB;

CREATE INDEX idx_teams_department ON teams (department_id);
CREATE INDEX idx_teams_deleted    ON teams (deleted);

CREATE TABLE employees (
    id                    CHAR(36)     NOT NULL,
    version               BIGINT       NOT NULL DEFAULT 0,
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            DATETIME(6)  NOT NULL,
    updated_at            DATETIME(6)  NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    employee_code         VARCHAR(30)  NOT NULL,
    first_name            VARCHAR(50)  NOT NULL,
    last_name             VARCHAR(50)  NOT NULL,
    work_email            VARCHAR(150) NOT NULL,
    phone                 VARCHAR(20),
    designation           VARCHAR(100),
    date_of_joining       DATE,
    date_of_birth         DATE,
    profile_image_url     VARCHAR(500),
    department_id         CHAR(36),
    team_id               CHAR(36),
    manager_id            CHAR(36),
    weekly_capacity_hours INT          NOT NULL DEFAULT 40,
    years_of_experience   INT,
    annual_leave_balance  INT          NOT NULL DEFAULT 24,
    hourly_rate           DECIMAL(10, 2),
    active                BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_employees PRIMARY KEY (id),
    CONSTRAINT uk_employees_code       UNIQUE (employee_code),
    CONSTRAINT uk_employees_work_email UNIQUE (work_email),
    CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_employees_team       FOREIGN KEY (team_id)       REFERENCES teams (id)
) ENGINE = InnoDB;

CREATE INDEX idx_employees_department ON employees (department_id);
CREATE INDEX idx_employees_team       ON employees (team_id);
CREATE INDEX idx_employees_manager    ON employees (manager_id);
CREATE INDEX idx_employees_deleted    ON employees (deleted);

-- Skills live in a side table so the workload/assignment scorer can intersect them in SQL.
CREATE TABLE employee_skills (
    employee_id CHAR(36)    NOT NULL,
    skill       VARCHAR(60) NOT NULL,
    CONSTRAINT pk_employee_skills PRIMARY KEY (employee_id, skill),
    CONSTRAINT fk_employee_skills_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
) ENGINE = InnoDB;

CREATE INDEX idx_employee_skills_skill ON employee_skills (skill);

CREATE TABLE users (
    id                    CHAR(36)     NOT NULL,
    version               BIGINT       NOT NULL DEFAULT 0,
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            DATETIME(6)  NOT NULL,
    updated_at            DATETIME(6)  NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    email                 VARCHAR(150) NOT NULL,
    password_hash         VARCHAR(100) NOT NULL,
    role_id               CHAR(36)     NOT NULL,
    employee_id           CHAR(36),
    enabled               BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at         DATETIME(6),
    password_changed_at   DATETIME(6),
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    lockout_expires_at    DATETIME(6),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email    UNIQUE (email),
    CONSTRAINT uk_users_employee UNIQUE (employee_id),
    CONSTRAINT fk_users_role     FOREIGN KEY (role_id)     REFERENCES roles (id),
    CONSTRAINT fk_users_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
) ENGINE = InnoDB;

CREATE INDEX idx_users_role    ON users (role_id);
CREATE INDEX idx_users_deleted ON users (deleted);

-- Only a SHA-256 digest of each refresh token is stored, so a database
-- disclosure does not hand an attacker usable credentials.
CREATE TABLE refresh_tokens (
    id         CHAR(36)     NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    token_hash VARCHAR(64)  NOT NULL,
    user_id    CHAR(36)     NOT NULL,
    issued_at  DATETIME(6)  NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    user_agent VARCHAR(255),
    ip_address VARCHAR(45),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_refresh_tokens_user    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);

CREATE TABLE leave_requests (
    id                  CHAR(36)     NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 0,
    deleted             BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          DATETIME(6)  NOT NULL,
    updated_at          DATETIME(6)  NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    employee_id         CHAR(36)     NOT NULL,
    leave_type          VARCHAR(30)  NOT NULL,
    start_date          DATE         NOT NULL,
    end_date            DATE         NOT NULL,
    reason              VARCHAR(500) NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    manager_approver_id CHAR(36),
    manager_approved_at DATETIME(6),
    hr_approver_id      CHAR(36),
    hr_approved_at      DATETIME(6),
    decision_comment    VARCHAR(500),
    CONSTRAINT pk_leave_requests PRIMARY KEY (id),
    CONSTRAINT fk_leave_requests_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT ck_leave_requests_dates CHECK (end_date >= start_date)
) ENGINE = InnoDB;

CREATE INDEX idx_leave_requests_employee ON leave_requests (employee_id);
CREATE INDEX idx_leave_requests_status   ON leave_requests (status);
CREATE INDEX idx_leave_requests_dates    ON leave_requests (start_date, end_date);
