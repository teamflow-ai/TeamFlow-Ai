import { Routes, Route, Navigate } from 'react-router-dom';
import AuthLayout from '../components/layouts/AuthLayout';
import MainLayout from '../components/layouts/MainLayout';
import ProtectedRoute from './ProtectedRoute';

import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import ForgotPassword from '../pages/auth/ForgotPassword';
import ResetPassword from '../pages/auth/ResetPassword';

import Dashboard from '../pages/dashboard/Dashboard';
import EmployeeList from '../pages/employee/EmployeeList';
import EmployeeDetail from '../pages/employee/EmployeeDetail';
import DepartmentList from '../pages/organization/department/DepartmentList';
import TeamList from '../pages/organization/team/TeamList';
import LeaveList from '../pages/leave/LeaveList';
import ClientList from '../pages/clients/ClientList';
import ProjectList from '../pages/project/ProjectList';
import ProjectDetail from '../pages/project/ProjectDetail';
import KanbanBoard from '../pages/project/KanbanBoard';
import TaskList from '../pages/task/TaskList';
import TaskDetail from '../pages/task/TaskDetail';
import BugList from '../pages/bug/BugList';
import MeetingList from '../pages/meeting/MeetingList';
import MilestoneList from '../pages/milestone/MilestoneList';
import WorkLogList from '../pages/worklog/WorkLogList';
import ApprovalCenter from '../pages/approval/ApprovalCenter';
import AIInsights from '../pages/ai/AIInsights';
import PolicyAssistant from '../pages/ai/PolicyAssistant';
import SearchResults from '../pages/search/SearchResults';
import Reports from '../pages/reports/Reports';
import Analytics from '../pages/analytics/Analytics';
import NotificationsPage from '../pages/notifications/NotificationsPage';
import Settings from '../pages/settings/Settings';
import Profile from '../pages/profile/Profile';
import NotFound from '../pages/misc/NotFound';
import Forbidden from '../pages/misc/Forbidden';

import { PERMISSIONS, APPROVER_PERMISSIONS } from '../constants/enums';

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
      </Route>

      <Route path="/403" element={<Forbidden />} />

      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Dashboard />} />

        <Route path="/employees" element={<EmployeeList />} />
        <Route path="/employees/:id" element={<EmployeeDetail />} />
        <Route
          path="/departments"
          element={<ProtectedRoute permission={PERMISSIONS.MANAGE_DEPARTMENTS}><DepartmentList /></ProtectedRoute>}
        />
        <Route
          path="/teams"
          element={<ProtectedRoute permission={PERMISSIONS.MANAGE_DEPARTMENTS}><TeamList /></ProtectedRoute>}
        />
        <Route path="/leaves" element={<LeaveList />} />

        <Route path="/clients" element={<ClientList />} />
        <Route path="/projects" element={<ProjectList />} />
        <Route path="/projects/:id" element={<ProjectDetail />} />
        <Route path="/projects/:id/kanban" element={<KanbanBoard />} />
        <Route path="/tasks" element={<TaskList />} />
        <Route path="/tasks/:id" element={<TaskDetail />} />
        <Route path="/bugs" element={<BugList />} />
        <Route path="/meetings" element={<MeetingList />} />
        <Route path="/milestones" element={<MilestoneList />} />
        <Route path="/worklogs" element={<WorkLogList />} />
        <Route
          path="/approvals"
          element={<ProtectedRoute anyPermission={APPROVER_PERMISSIONS}><ApprovalCenter /></ProtectedRoute>}
        />
        <Route
          path="/approvals/:id"
          element={<ProtectedRoute anyPermission={APPROVER_PERMISSIONS}><ApprovalCenter /></ProtectedRoute>}
        />

        <Route
          path="/reports"
          element={<ProtectedRoute permission={PERMISSIONS.VIEW_ANALYTICS}><Reports /></ProtectedRoute>}
        />
        <Route
          path="/analytics"
          element={<ProtectedRoute permission={PERMISSIONS.VIEW_ANALYTICS}><Analytics /></ProtectedRoute>}
        />
        <Route
          path="/ai-insights"
          element={<ProtectedRoute permission={PERMISSIONS.ASSIGN_TASK}><AIInsights /></ProtectedRoute>}
        />
        <Route path="/policy-assistant" element={<PolicyAssistant />} />
        <Route path="/search" element={<SearchResults />} />

        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/settings" element={<Settings />} />
        <Route path="/profile" element={<Profile />} />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
