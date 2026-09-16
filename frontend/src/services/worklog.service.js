import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors project-service WorkLogController (/api/v1/worklogs).
const base = API_ROUTES.worklogs;

export const worklogService = {
  async create(payload) {
    const { data } = await axiosClient.post(base, payload);
    return data;
  },
  async update(id, payload) {
    const { data } = await axiosClient.put(`${base}/${id}`, payload);
    return data;
  },
  async get(id) {
    const { data } = await axiosClient.get(`${base}/${id}`);
    return data;
  },
  async mine({ page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(`${base}/me`, { params: { page, size, sort } });
    return data;
  },
  async list({ projectId, taskId, page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(base, { params: { projectId, taskId, page, size, sort } });
    return data;
  },
  async remove(id) {
    await axiosClient.delete(`${base}/${id}`);
  },
};
