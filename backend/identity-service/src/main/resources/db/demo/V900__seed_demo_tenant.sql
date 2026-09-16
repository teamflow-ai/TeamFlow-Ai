-- =====================================================================
-- Demo company data and bootstrap administrator.
-- =====================================================================
-- Applied only under the `demo` Flyway location so production installations
-- never receive a known-password account. See application.yml.
--
-- Password for every seeded account is: Admin@123
-- The literal below is a BCrypt(strength 12) digest of that string. It must be
-- changed immediately on any non-local deployment.
-- =====================================================================

INSERT INTO departments (id, version, deleted, created_at, updated_at, created_by, name, code,
                         description, annual_budget)
VALUES ('d0000000-0000-4000-8000-000000000001', 0, FALSE, NOW(6), NOW(6), 'SYSTEM',
        'Engineering', 'ENG', 'Product engineering', 12000000.00),
       ('d0000000-0000-4000-8000-000000000002', 0, FALSE, NOW(6), NOW(6), 'SYSTEM',
        'Quality Assurance', 'QA', 'Test and release quality', 4000000.00);

INSERT INTO teams (id, version, deleted, created_at, updated_at, created_by, name, description, department_id)
VALUES ('e0000000-0000-4000-8000-000000000001', 0, FALSE, NOW(6), NOW(6), 'SYSTEM',
        'Platform', 'Core platform and services', 'd0000000-0000-4000-8000-000000000001');

INSERT INTO employees (id, version, deleted, created_at, updated_at, created_by, employee_code,
                       first_name, last_name, work_email, designation, date_of_joining,
                       department_id, team_id, weekly_capacity_hours,
                       years_of_experience, annual_leave_balance, active)
VALUES ('f0000000-0000-4000-8000-000000000001', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'EMP-0001',
        'Ankit', 'Bamanpalli', 'admin@teamflow.ai', 'Platform Administrator', '2024-01-15',
        'd0000000-0000-4000-8000-000000000001',
        'e0000000-0000-4000-8000-000000000001', 40, 5, 24, TRUE);

INSERT INTO employee_skills (employee_id, skill) VALUES
 ('f0000000-0000-4000-8000-000000000001', 'JAVA'),
 ('f0000000-0000-4000-8000-000000000001', 'SPRING_BOOT'),
 ('f0000000-0000-4000-8000-000000000001', 'MYSQL');

INSERT INTO users (id, version, deleted, created_at, updated_at, created_by, email, password_hash,
                   role_id, employee_id, enabled, email_verified, password_changed_at)
VALUES ('00000000-0000-4000-8000-000000000001', 0, FALSE, NOW(6), NOW(6), 'SYSTEM',
        'admin@teamflow.ai', '$2a$12$08lhQRrIkxvV8xloYXYjD.IzkPagMjZ5EviJXxjLnwZaLyCHR2iqq',
        'b0000000-0000-4000-8000-000000000001',
        'f0000000-0000-4000-8000-000000000001', TRUE, TRUE, NOW(6));
