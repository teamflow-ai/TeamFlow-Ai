import {
  LayoutDashboard, Building2, Users2, UsersRound, FolderKanban, ListChecks,
  Bug, CalendarClock, Clock3, Briefcase, CalendarOff, BarChart3, Settings,
  Flag, ClipboardCheck, Sparkles, LineChart, Bot
} from 'lucide-react';
import { PERMISSIONS, APPROVER_PERMISSIONS } from '../../constants/enums';

// Single source of truth for sidebar + mobile nav. `permission: null` = visible to everyone signed in.
// `anyPermission` = visible if the user holds at least one of the listed permissions.
export const NAV_SECTIONS = [
  {
    label: 'Overview',
    items: [{ label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard, permission: null }],
  },
  {
    label: 'Workforce',
    items: [
      { label: 'Employees', to: '/employees', icon: Users2, permission: null },
      { label: 'Departments', to: '/departments', icon: Building2, permission: PERMISSIONS.MANAGE_DEPARTMENTS },
      { label: 'Teams', to: '/teams', icon: UsersRound, permission: PERMISSIONS.MANAGE_DEPARTMENTS },
      { label: 'Leave requests', to: '/leaves', icon: CalendarOff, permission: null },
    ],
  },
  {
    label: 'Delivery',
    items: [
      { label: 'Clients', to: '/clients', icon: Briefcase, permission: null },
      { label: 'Projects', to: '/projects', icon: FolderKanban, permission: null },
      { label: 'Tasks', to: '/tasks', icon: ListChecks, permission: null },
      { label: 'Bugs', to: '/bugs', icon: Bug, permission: null },
      { label: 'Meetings', to: '/meetings', icon: CalendarClock, permission: null },
      { label: 'Milestones', to: '/milestones', icon: Flag, permission: null },
      { label: 'Work logs', to: '/worklogs', icon: Clock3, permission: null },
      { label: 'Approvals', to: '/approvals', icon: ClipboardCheck, anyPermission: APPROVER_PERMISSIONS },
    ],
  },
  {
    label: 'Insights',
    items: [
      { label: 'Reports', to: '/reports', icon: BarChart3, permission: PERMISSIONS.VIEW_ANALYTICS },
      { label: 'Analytics', to: '/analytics', icon: LineChart, permission: PERMISSIONS.VIEW_ANALYTICS },
      { label: 'AI Insights', to: '/ai-insights', icon: Sparkles, permission: PERMISSIONS.ASSIGN_TASK },
      { label: 'Policy Assistant', to: '/policy-assistant', icon: Bot, permission: null },
    ],
  },
  {
    label: 'Workspace',
    items: [{ label: 'Settings', to: '/settings', icon: Settings, permission: null }],
  },
];
