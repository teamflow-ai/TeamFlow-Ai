INSERT IGNORE INTO employees (id, version, deleted, created_at, updated_at, created_by, employee_code,
                       first_name, last_name, work_email, designation, date_of_joining,
                       department_id, team_id, weekly_capacity_hours,
                       years_of_experience, annual_leave_balance, active)
VALUES ('f0000000-0000-4000-8000-000000000001', 0, FALSE, NOW(6), NOW(6), 'SYSTEM', 'EMP-9999',
        'Aaditya', 'Khansana', 'admin@teamflow.ai', 'Platform Administrator', '2024-01-15',
        'd0000000-0000-4000-8000-000000000001',
        'e0000000-0000-4000-8000-000000000001', 40, 5, 24, TRUE);

INSERT IGNORE INTO employee_skills (employee_id, skill) VALUES
 ('f0000000-0000-4000-8000-000000000001', 'JAVA'),
 ('f0000000-0000-4000-8000-000000000001', 'SPRING_BOOT'),
 ('f0000000-0000-4000-8000-000000000001', 'MYSQL');

INSERT IGNORE INTO users (id, version, deleted, created_at, updated_at, created_by, email, password_hash,
                   role_id, employee_id, enabled, email_verified, password_changed_at)
VALUES ('00000000-0000-4000-8000-000000000001', 0, FALSE, NOW(6), NOW(6), 'SYSTEM',
        'admin@teamflow.ai', '$2a$12$08lhQRrIkxvV8xloYXYjD.IzkPagMjZ5EviJXxjLnwZaLyCHR2iqq',
        'b0000000-0000-4000-8000-000000000001',
        'f0000000-0000-4000-8000-000000000001', TRUE, TRUE, NOW(6));
