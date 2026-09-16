-- =====================================================================
-- Seeds the 17 permissions, 12 roles and their default mapping.
-- =====================================================================
-- Roles and permissions are data, not enum constants, so an administrator can
-- re-map capabilities at runtime. The rows below are the shipped defaults;
-- `system_role = TRUE` marks the ones referenced by @PreAuthorize expressions,
-- which therefore must not be renamed or deleted.
--
-- UUIDs are literal rather than generated so this migration is repeatable and
-- produces identical ids across every environment, which keeps seed data and
-- integration-test fixtures aligned.
-- =====================================================================

INSERT INTO permissions (id, version, deleted, created_at, updated_at, created_by, name, description, category) VALUES
 ('a0000000-0000-4000-8000-000000000001', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'CREATE_PROJECT',      'Create new projects',                    'PROJECT'),
 ('a0000000-0000-4000-8000-000000000002', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'UPDATE_PROJECT',      'Modify existing projects',               'PROJECT'),
 ('a0000000-0000-4000-8000-000000000003', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'DELETE_PROJECT',      'Remove projects',                        'PROJECT'),
 ('a0000000-0000-4000-8000-000000000004', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'CREATE_EMPLOYEE',     'Create employee records',                'HR'),
 ('a0000000-0000-4000-8000-000000000005', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'UPDATE_EMPLOYEE',     'Modify employee records',                'HR'),
 ('a0000000-0000-4000-8000-000000000006', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'DELETE_EMPLOYEE',     'Remove employee records',                'HR'),
 ('a0000000-0000-4000-8000-000000000007', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'ASSIGN_TASK',         'Assign tasks to employees',              'TASK'),
 ('a0000000-0000-4000-8000-000000000008', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'UPDATE_TASK',         'Modify tasks',                           'TASK'),
 ('a0000000-0000-4000-8000-000000000009', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'DELETE_TASK',         'Remove tasks',                           'TASK'),
 ('a0000000-0000-4000-8000-000000000010', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'VIEW_REPORT',         'View generated reports',                 'REPORTING'),
 ('a0000000-0000-4000-8000-000000000011', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'GENERATE_REPORT',     'Generate new reports',                   'REPORTING'),
 ('a0000000-0000-4000-8000-000000000012', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'VIEW_ANALYTICS',      'View dashboards and analytics',          'ANALYTICS'),
 ('a0000000-0000-4000-8000-000000000013', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'APPROVE_LEAVE',       'Approve or reject leave requests',       'HR'),
 ('a0000000-0000-4000-8000-000000000014', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'VIEW_FINANCE',        'View financial data',                    'FINANCE'),
 ('a0000000-0000-4000-8000-000000000015', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'MANAGE_USERS',        'Administer user accounts',               'ADMIN'),
 ('a0000000-0000-4000-8000-000000000016', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'MANAGE_ROLES',        'Administer roles and permissions',       'ADMIN'),
 ('a0000000-0000-4000-8000-000000000017', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'MANAGE_DEPARTMENTS',  'Administer departments and teams',       'ADMIN');

INSERT INTO roles (id, version, deleted, created_at, updated_at, created_by, name, description, system_role) VALUES
 ('b0000000-0000-4000-8000-000000000001', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'SUPER_ADMIN',     'Platform administrator with unrestricted access', TRUE),
 ('b0000000-0000-4000-8000-000000000002', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'CEO',             'Executive with company-wide visibility',          TRUE),
 ('b0000000-0000-4000-8000-000000000003', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'ADMIN',           'Company administrator',                           TRUE),
 ('b0000000-0000-4000-8000-000000000004', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'PROJECT_MANAGER', 'Owns delivery of one or more projects',           TRUE),
 ('b0000000-0000-4000-8000-000000000005', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'TEAM_LEAD',       'Leads a team within a department',                TRUE),
 ('b0000000-0000-4000-8000-000000000006', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'DEVELOPER',       'Individual contributor building software',        TRUE),
 ('b0000000-0000-4000-8000-000000000007', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'QA',              'Quality assurance engineer',                      TRUE),
 ('b0000000-0000-4000-8000-000000000008', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'DEVOPS',          'Infrastructure and release engineer',             TRUE),
 ('b0000000-0000-4000-8000-000000000009', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'HR',              'Human resources',                                 TRUE),
 ('b0000000-0000-4000-8000-000000000010', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'FINANCE',         'Finance and budgeting',                           TRUE),
 ('b0000000-0000-4000-8000-000000000011', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'SALES',           'Sales and client acquisition',                    TRUE),
 ('b0000000-0000-4000-8000-000000000012', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'CLIENT',          'External client with read-only project access',   TRUE);

-- SUPER_ADMIN: every permission.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000001', id FROM permissions;

-- CEO: company-wide visibility, but not day-to-day administration.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000002', id FROM permissions
WHERE name IN ('VIEW_REPORT','GENERATE_REPORT','VIEW_ANALYTICS','VIEW_FINANCE');

-- ADMIN: company-wide administration, excluding finance.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000003', id FROM permissions
WHERE name IN ('CREATE_PROJECT','UPDATE_PROJECT','DELETE_PROJECT','CREATE_EMPLOYEE','UPDATE_EMPLOYEE',
               'DELETE_EMPLOYEE','MANAGE_USERS','MANAGE_ROLES','MANAGE_DEPARTMENTS','VIEW_REPORT','VIEW_ANALYTICS');

-- PROJECT_MANAGER: owns delivery; may assign work and approve their team's leave.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000004', id FROM permissions
WHERE name IN ('CREATE_PROJECT','UPDATE_PROJECT','ASSIGN_TASK','UPDATE_TASK','DELETE_TASK',
               'VIEW_REPORT','GENERATE_REPORT','VIEW_ANALYTICS','APPROVE_LEAVE');

-- TEAM_LEAD: assigns within the team, no project-level authority.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000005', id FROM permissions
WHERE name IN ('ASSIGN_TASK','UPDATE_TASK','VIEW_REPORT','VIEW_ANALYTICS');

-- DEVELOPER / QA / DEVOPS: update the work assigned to them.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name IN ('DEVELOPER','QA','DEVOPS') AND p.name IN ('UPDATE_TASK','VIEW_REPORT');

-- HR: people administration and the second leave-approval gate.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000009', id FROM permissions
WHERE name IN ('CREATE_EMPLOYEE','UPDATE_EMPLOYEE','DELETE_EMPLOYEE','APPROVE_LEAVE',
               'MANAGE_DEPARTMENTS','VIEW_REPORT','GENERATE_REPORT');

-- FINANCE
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000010', id FROM permissions
WHERE name IN ('VIEW_FINANCE','VIEW_REPORT','GENERATE_REPORT','VIEW_ANALYTICS');

-- SALES
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000011', id FROM permissions
WHERE name IN ('VIEW_REPORT','VIEW_ANALYTICS');

-- CLIENT: read-only reporting on their own engagements.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000012', id FROM permissions
WHERE name IN ('VIEW_REPORT');
