import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors ai-service DashboardController (/api/v1/dashboard).
const base = API_ROUTES.dashboard;

export const dashboardService = {
  async summary() {
    const { data } = await axiosClient.get(`${base}/summary`);
    return data; // DashboardSummaryResponse
  },
  async workload() {
    const { data } = await axiosClient.get(`${base}/workload`);
    return data; // EmployeeWorkloadResponse[]
  },
};
