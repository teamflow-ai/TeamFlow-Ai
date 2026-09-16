import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors project-service BugController (/api/v1/bugs).
const base = API_ROUTES.bugs;

export const bugService = {
  async list({ projectId, page = 0, size = 20, sort } = {}) {
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
  async assign(id, assigneeId) {
    const { data } = await axiosClient.patch(`${base}/${id}/assign`, { assigneeId });
    return data;
  },
  async updateStatus(id, { status, resolution }) {
    const { data } = await axiosClient.patch(`${base}/${id}/status`, { status, resolution });
    return data;
  },
  async remove(id) {
    await axiosClient.delete(`${base}/${id}`);
  },
};
