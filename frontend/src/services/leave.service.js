import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors identity-service LeaveController (/api/v1/leaves).
const base = API_ROUTES.leaves;

export const leaveService = {
  async create({ leaveType, startDate, endDate, reason }) {
    const { data } = await axiosClient.post(base, { leaveType, startDate, endDate, reason });
    return data;
  },
  async myLeaves({ page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(`${base}/me`, { params: { page, size, sort } });
    return data;
  },
  async pending({ page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(`${base}/pending`, { params: { page, size, sort } });
    return data;
  },
  async get(id) {
    const { data } = await axiosClient.get(`${base}/${id}`);
    return data;
  },
  async managerDecision(id, { approve, comment }) {
    const { data } = await axiosClient.post(`${base}/${id}/manager-decision`, { approve, comment });
    return data;
  },
  async hrDecision(id, { approve, comment }) {
    const { data } = await axiosClient.post(`${base}/${id}/hr-decision`, { approve, comment });
    return data;
  },
  async cancel(id) {
    const { data } = await axiosClient.post(`${base}/${id}/cancel`);
    return data;
  },
};
