import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors project-service MilestoneController (/api/v1/milestones).
const base = API_ROUTES.milestones;

export const milestoneService = {
  async list({ projectId, page = 0, size = 50, sort } = {}) {
    const { data } = await axiosClient.get(base, { params: { projectId, page, size, sort } });
    return data;
  },
  async get(id) {
    const { data } = await axiosClient.get(`${base}/${id}`);
    return data;
  },
  async create(payload) {
    const { data } = await axiosClient.post(base, payload);
    return data;
  },
  async update(id, payload) {
    const { data } = await axiosClient.put(`${base}/${id}`, payload);
    return data;
  },
  async submitForReview(id, remarks) {
    const { data } = await axiosClient.post(`${base}/${id}/submit`, remarks ? { remarks } : undefined);
    return data; // ApprovalResponse
  },
  async remove(id) {
    await axiosClient.delete(`${base}/${id}`);
  },
};
