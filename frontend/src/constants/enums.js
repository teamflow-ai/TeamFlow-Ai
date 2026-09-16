/**
 * Mirrors com.teamflow.ai.common.enums and PermissionNames from the backend
 * common-lib exactly, so the UI never invents a status the API doesn't know.
 */

export const TASK_STATUS = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'BLOCKED'];

export const PROJECT_STATUS = ['PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED'];

export const SPRINT_STATUS = ['PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED'];

export const BUG_STATUS = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED'];

export const MEETING_STATUS = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

export const LEAVE_STATUS = ['PENDING', 'MANAGER_APPROVED', 'APPROVED', 'REJECTED', 'CANCELLED'];

export const LEAVE_TYPE = ['CASUAL', 'SICK', 'EARNED', 'UNPAID', 'MATERNITY', 'PATERNITY'];

export const PRIORITY = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export const ASSIGNMENT_MODE = ['MANUAL', 'SMART'];

export const BURNOUT_RISK = ['LOW', 'MODERATE', 'HIGH', 'CRITICAL'];

export const APPROVAL_TYPE = ['MILESTONE', 'TASK_COMPLETION', 'PROJECT_CLOSURE', 'CLIENT_APPROVAL'];

export const APPROVAL_STATUS = ['PENDING', 'APPROVED', 'REJECTED', 'RETURNED_FOR_CHANGES'];

export const MILESTONE_STATUS = ['PLANNED', 'IN_PROGRESS', 'READY_FOR_REVIEW', 'APPROVED', 'REJECTED'];

// Maps a status/priority value to the visual badge tone used everywhere.
export const STATUS_TONE = {
  // Task
  BACKLOG: 'neutral',
  TODO: 'info',
  IN_PROGRESS: 'primary',
  IN_REVIEW: 'warning',
  DONE: 'success',
  BLOCKED: 'danger',
  // Project
  PLANNING: 'info',
  ACTIVE: 'primary',
  ON_HOLD: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'danger',
  // Sprint reuses ACTIVE/COMPLETED/CANCELLED/PLANNED
  PLANNED: 'info',
  // Bug
  OPEN: 'danger',
  RESOLVED: 'success',
  CLOSED: 'neutral',
  REOPENED: 'warning',
  // Meeting
  SCHEDULED: 'info',
  // Leave / Approval
  PENDING: 'warning',
  MANAGER_APPROVED: 'primary',
  APPROVED: 'success',
  REJECTED: 'danger',
  RETURNED_FOR_CHANGES: 'warning',
  // Priority
  LOW: 'neutral',
  MEDIUM: 'info',
  HIGH: 'warning',
  CRITICAL: 'danger',
  // Burnout risk
  MODERATE: 'warning',
  // Milestone
  READY_FOR_REVIEW: 'warning',
};

// PermissionNames.java — the frontend never invents these strings.
export const PERMISSIONS = {
  CREATE_PROJECT: 'CREATE_PROJECT',
  UPDATE_PROJECT: 'UPDATE_PROJECT',
  DELETE_PROJECT: 'DELETE_PROJECT',
  CREATE_EMPLOYEE: 'CREATE_EMPLOYEE',
  UPDATE_EMPLOYEE: 'UPDATE_EMPLOYEE',
  DELETE_EMPLOYEE: 'DELETE_EMPLOYEE',
  ASSIGN_TASK: 'ASSIGN_TASK',
  UPDATE_TASK: 'UPDATE_TASK',
  DELETE_TASK: 'DELETE_TASK',
  VIEW_REPORT: 'VIEW_REPORT',
  GENERATE_REPORT: 'GENERATE_REPORT',
  VIEW_ANALYTICS: 'VIEW_ANALYTICS',
  APPROVE_LEAVE: 'APPROVE_LEAVE',
  VIEW_FINANCE: 'VIEW_FINANCE',
  MANAGE_USERS: 'MANAGE_USERS',
  MANAGE_ROLES: 'MANAGE_ROLES',
  MANAGE_DEPARTMENTS: 'MANAGE_DEPARTMENTS',
  MANAGE_MILESTONES: 'MANAGE_MILESTONES',
  APPROVE_MILESTONE: 'APPROVE_MILESTONE',
  APPROVE_TASK_COMPLETION: 'APPROVE_TASK_COMPLETION',
  REQUEST_PROJECT_CLOSURE: 'REQUEST_PROJECT_CLOSURE',
  APPROVE_PROJECT_CLOSURE: 'APPROVE_PROJECT_CLOSURE',
  MANAGE_CLIENT_APPROVAL: 'MANAGE_CLIENT_APPROVAL',
};

// Any one of these grants access to the Approval Center / decide action.
export const APPROVER_PERMISSIONS = [
  PERMISSIONS.APPROVE_MILESTONE,
  PERMISSIONS.APPROVE_TASK_COMPLETION,
  PERMISSIONS.APPROVE_PROJECT_CLOSURE,
  PERMISSIONS.MANAGE_CLIENT_APPROVAL,
];

export const humanize = (value) =>
  (value || '')
    .toLowerCase()
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ');

export const canTaskTransition = (from, to) => {
  if (from === to) return false;
  switch (from) {
    case 'BACKLOG': return to === 'TODO' || to === 'CANCELLED';
    case 'TODO': return to === 'IN_PROGRESS' || to === 'BLOCKED' || to === 'CANCELLED';
    case 'IN_PROGRESS': return to === 'IN_REVIEW' || to === 'BLOCKED' || to === 'TODO' || to === 'CANCELLED';
    case 'IN_REVIEW': return to === 'DONE' || to === 'IN_PROGRESS' || to === 'BLOCKED';
    case 'BLOCKED': return to === 'TODO' || to === 'IN_PROGRESS' || to === 'CANCELLED';
    case 'DONE': 
    case 'CANCELLED': return to === 'TODO';
    default: return false;
  }
};
