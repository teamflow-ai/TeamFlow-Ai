import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors project-service ReportController (/api/v1/reports) and
// identity-service DepartmentReportController (/api/v1/reports/departments).
// format=json returns the ApiResponse-wrapped payload; format=csv|xlsx returns a
// downloadable file, so those calls request a blob and bypass the JSON envelope.
const base = API_ROUTES.reports;

async function downloadFile(url, params, filename) {
  const response = await axiosClient.get(url, { params, responseType: 'blob', transformResponse: [(d) => d] });
  const blob = response instanceof Blob ? response : new Blob([response]);
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
}

export const reportService = {
  async projectReport(projectId) {
    const { data } = await axiosClient.get(`${base}/projects/${projectId}`, { params: { format: 'json' } });
    return data;
  },
  async sprintReport(sprintId) {
    const { data } = await axiosClient.get(`${base}/sprints/${sprintId}`, { params: { format: 'json' } });
    return data;
  },
  async worklogReport({ projectId, from, to }) {
    const { data } = await axiosClient.get(`${base}/worklogs`, { params: { projectId, from, to, format: 'json' } });
    return data;
  },
  async employeeProductivityReport(employeeId, { from, to }) {
    const { data } = await axiosClient.get(`${base}/employees/${employeeId}/productivity`, {
      params: { from, to, format: 'json' },
    });
    return data;
  },
  async departmentReport(departmentId) {
    const { data } = await axiosClient.get(`${API_ROUTES.departmentReports}/${departmentId}`, {
      params: { format: 'json' },
    });
    return data;
  },
  exportProjectReport: (projectId, format) =>
    downloadFile(`${base}/projects/${projectId}`, { format }, `project-report-${projectId}.${format}`),
  exportSprintReport: (sprintId, format) =>
    downloadFile(`${base}/sprints/${sprintId}`, { format }, `sprint-report-${sprintId}.${format}`),
  exportWorklogReport: (projectId, from, to, format) =>
    downloadFile(`${base}/worklogs`, { projectId, from, to, format }, `worklog-report-${projectId}.${format}`),
  exportEmployeeProductivityReport: (employeeId, from, to, format) =>
    downloadFile(
      `${base}/employees/${employeeId}/productivity`,
      { from, to, format },
      `employee-productivity-${employeeId}.${format}`
    ),
  exportDepartmentReport: (departmentId, format) =>
    downloadFile(`${API_ROUTES.departmentReports}/${departmentId}`, { format }, `department-report-${departmentId}.${format}`),
};
