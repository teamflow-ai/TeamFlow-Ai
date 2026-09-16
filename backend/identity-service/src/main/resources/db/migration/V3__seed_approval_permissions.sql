-- =====================================================================
-- Adds the permissions introduced by the generic Approval Workflow Engine.
-- Additive only: V2 is never edited once applied, per standard Flyway practice.
-- =====================================================================

INSERT INTO permissions (id, version, deleted, created_at, updated_at, created_by, name, description, category) VALUES
 ('a0000000-0000-4000-8000-000000000018', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'MANAGE_MILESTONES',       'Create and update project milestones',              'PROJECT'),
 ('a0000000-0000-4000-8000-000000000019', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'APPROVE_MILESTONE',       'Approve or reject a milestone marked ready',        'PROJECT'),
 ('a0000000-0000-4000-8000-000000000020', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'APPROVE_TASK_COMPLETION', 'Approve or return a task submitted for review',     'TASK'),
 ('a0000000-0000-4000-8000-000000000021', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'REQUEST_PROJECT_CLOSURE', 'Request that a project be closed',                  'PROJECT'),
 ('a0000000-0000-4000-8000-000000000022', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'APPROVE_PROJECT_CLOSURE', 'Approve or reject a project closure request',       'PROJECT'),
 ('a0000000-0000-4000-8000-000000000023', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'MANAGE_CLIENT_APPROVAL',  'Send deliverables for client approval and record the outcome', 'PROJECT');

-- SUPER_ADMIN already receives every permission via the SELECT * pattern in V2's
-- first INSERT, which only ran against permissions existing at that time; extend it here.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000001', id FROM permissions
WHERE name IN ('MANAGE_MILESTONES','APPROVE_MILESTONE','APPROVE_TASK_COMPLETION',
               'REQUEST_PROJECT_CLOSURE','APPROVE_PROJECT_CLOSURE','MANAGE_CLIENT_APPROVAL');

-- ADMIN: approves milestones and project closure (per role brief: "Admin reviews milestone / project closure").
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000003', id FROM permissions
WHERE name IN ('APPROVE_MILESTONE','APPROVE_PROJECT_CLOSURE');

-- PROJECT_MANAGER: manages milestones day to day, approves task completion, requests
-- project closure and manages client sign-off correspondence.
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'b0000000-0000-4000-8000-000000000004', id FROM permissions
WHERE name IN ('MANAGE_MILESTONES','APPROVE_TASK_COMPLETION','REQUEST_PROJECT_CLOSURE','MANAGE_CLIENT_APPROVAL');
