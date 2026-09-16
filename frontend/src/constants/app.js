// Mirrors api-gateway's application.yml route predicates 1:1.
// identity-service  -> /auth, /employees, /departments, /teams, /roles, /permissions, /leaves, /reports/departments
// project-service   -> /projects, /sprints, /tasks, /meetings, /worklogs, /bugs, /clients, /milestones,
//                       /approvals, /search, /reports
// ai-service         -> /ai, /dashboard, /analytics, /notifications, /audit
export const API_ROUTES = {
  auth: '/auth',
  employees: '/employees',
  departments: '/departments',
  teams: '/teams',
  roles: '/roles',
  permissions: '/permissions',
  leaves: '/leaves',
  projects: '/projects',
  sprints: '/sprints',
  tasks: '/tasks',
  meetings: '/meetings',
  worklogs: '/worklogs',
  bugs: '/bugs',
  clients: '/clients',
  milestones: '/milestones',
  approvals: '/approvals',
  search: '/search',
  reports: '/reports',
  departmentReports: '/reports/departments',
  ai: '/ai',
  dashboard: '/dashboard',
  analytics: '/analytics',
  notifications: '/notifications',
  audit: '/audit',
};

export const ORGANIZATION = {
  name: 'TeamFlow AI',
  industry: 'Software Development',
  country: 'India',
  state: 'Maharashtra',
  city: 'Pune',
  timezone: 'Asia/Kolkata',
};

export const APP_NAME = 'TeamFlow AI';

// Cosmetic login-flow role choices only (see pages/auth/Login.jsx) — the backend
// user profile's actual `role` always decides authorization and redirect target.
export const LOGIN_ROLE_OPTIONS = [
  { value: 'ADMIN', label: 'Administrator' },
  { value: 'PROJECT_MANAGER', label: 'Project Manager' },
  { value: 'EMPLOYEE', label: 'Employee' },
];
