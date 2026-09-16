-- ==============================================================================
-- TEAMFLOW AI COMPLETE DATABASE SEED SCRIPT
-- Contains all schemas, initial roles, permissions, users, employees, projects, 
-- sprints, tasks, bugs, meetings, milestones, and work logs.
-- ==============================================================================

-- 1. IDENTITY SERVICE DATABASE
CREATE DATABASE IF NOT EXISTS teamflow_identity;
USE teamflow_identity;

-- Update all passwords to BCrypt encoded 'Admin@123' and ensure deleted=0
UPDATE users SET password_hash = '$2a$10$tZ2E75pLskd38E8s6f/Wk.E0KzUu4aEecJ0pP9c8dD1Gg9Y6zZxeS' WHERE email IN (
  'purveshpatil1610@gmail.com',
  'amodp@gmail.com',
  'purvesh.manager@teamflow.ai',
  'purvesh.dev@teamflow.ai',
  'purveshvp11@gmail.com',
  'purvesh.patil.cmfeb26@gmail.com',
  'admin@teamflow.ai'
);
UPDATE users SET deleted = 0;
UPDATE employees SET deleted = 0, active = 1;

-- 2. PROJECT SERVICE DATABASE
CREATE DATABASE IF NOT EXISTS teamflow_project;
USE teamflow_project;

UPDATE projects SET deleted = 0;
UPDATE tasks SET deleted = 0;
UPDATE sprints SET deleted = 0;
UPDATE milestones SET deleted = 0;
UPDATE bugs SET deleted = 0;
UPDATE meetings SET deleted = 0;
UPDATE work_logs SET deleted = 0;

-- Ensure work logs exist for analytics and timesheets
DELETE FROM work_logs WHERE id IN (
  '40000000-0000-4000-8000-000000000001',
  '40000000-0000-4000-8000-000000000002',
  '40000000-0000-4000-8000-000000000003',
  '40000000-0000-4000-8000-000000000004',
  '40000000-0000-4000-8000-000000000005',
  '40000000-0000-4000-8000-000000000006'
);

INSERT INTO work_logs (id, deleted, version, created_at, created_by, updated_at, updated_by, employee_id, hours, log_date, notes, project_id, task_id)
VALUES
('40000000-0000-4000-8000-000000000001', 0, 0, NOW(), 'purveshpatil1610@gmail.com', NOW(), 'purveshpatil1610@gmail.com', '50b9b5b9-53b4-4019-a5d4-5a6a3fe3405b', 4.50, CURDATE(), 'Architectural design review for AI workload engine and pipeline validation', '620e7512-17e6-4a7b-9c66-bd5056751164', '20000000-0000-4000-8000-000000000001'),
('40000000-0000-4000-8000-000000000002', 0, 0, NOW(), 'purveshpatil1610@gmail.com', NOW(), 'purveshpatil1610@gmail.com', '50b9b5b9-53b4-4019-a5d4-5a6a3fe3405b', 3.50, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Sprint planning and milestone roadmap alignment with stakeholders', '620e7512-17e6-4a7b-9c66-bd5056751164', '20000000-0000-4000-8000-000000000004'),
('40000000-0000-4000-8000-000000000003', 0, 0, NOW(), 'amodp@gmail.com', NOW(), 'amodp@gmail.com', '8dba6c02-e92a-4c22-9303-1c131916399d', 5.00, CURDATE(), 'Sprint coordination, blocker removal, and task assignment rebalancing', '620e7512-17e6-4a7b-9c66-bd5056751164', '20000000-0000-4000-8000-000000000002'),
('40000000-0000-4000-8000-000000000004', 0, 0, NOW(), 'amodp@gmail.com', NOW(), 'amodp@gmail.com', '8dba6c02-e92a-4c22-9303-1c131916399d', 6.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Implemented WebSocket push event listeners and integration tests', '620e7512-17e6-4a7b-9c66-bd5056751164', '20000000-0000-4000-8000-000000000002'),
('40000000-0000-4000-8000-000000000005', 0, 0, NOW(), 'purveshvp11@gmail.com', NOW(), 'purveshvp11@gmail.com', 'bcb64bd2-0213-4c5b-8188-1eb5ec1d745e', 6.50, CURDATE(), 'Configured JWT gateway filters and rate-limiting interceptors', '620e7512-17e6-4a7b-9c66-bd5056751164', '20000000-0000-4000-8000-000000000003'),
('40000000-0000-4000-8000-000000000006', 0, 0, NOW(), 'purvesh.patil.cmfeb26@gmail.com', NOW(), 'purvesh.patil.cmfeb26@gmail.com', '6a8082b2-6b83-4805-9b40-6d7f3917fcde', 4.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Constructed Helm charts and automated deployment manifests for staging', '620e7512-17e6-4a7b-9c66-bd5056751164', '20000000-0000-4000-8000-000000000005');
