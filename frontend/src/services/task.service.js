import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors project-service TaskController (/api/v1/tasks).
const base = API_ROUTES.tasks;

export const taskService = {
  async list({ projectId, sprintId, assigneeId, status, priority, page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(base, {
      params: { projectId, sprintId, assigneeId, status, priority, page, size, sort },
    });
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
  async updateStatus(id, status) {
    const { data } = await axiosClient.patch(`${base}/${id}/status`, { status });
    return data;
  },
  async recommendations(id) {
    const { data } = await axiosClient.get(`${base}/${id}/recommendations`);
    return data;
  },
  async candidates(id) {
    const { data } = await axiosClient.get(`${base}/${id}/candidates`);
    return data;
  },
  async assign(id, { assigneeId, mode }) {
    const { data } = await axiosClient.patch(`${base}/${id}/assign`, { assigneeId, mode });
    return data;
  },
  async assignSprint(id, sprintId) {
    const { data } = await axiosClient.patch(`${base}/${id}/sprint`, { sprintId });
    return data;
  },
  async addComment(id, comment) {
    const { data } = await axiosClient.post(`${base}/${id}/comments`, { comment });
    return data;
  },
  async listComments(id, { page = 0, size = 50 } = {}) {
    const { data } = await axiosClient.get(`${base}/${id}/comments`, { params: { page, size } });
    return data;
  },
  async listHistory(id) {
    const { data } = await axiosClient.get(`${base}/${id}/history`);
    return data;
  },
  async addAttachment(id, { fileName, fileUrl }) {
    const { data } = await axiosClient.post(`${base}/${id}/attachments`, { fileName, fileUrl });
    return data;
  },
  async listAttachments(id) {
    const { data } = await axiosClient.get(`${base}/${id}/attachments`);
    return data;
  },
  async getDependencies(id) {
    const { data } = await axiosClient.get(`${base}/${id}/dependencies`);
    return data;
  },
  async addDependency(id, dependsOnId) {
    const { data } = await axiosClient.post(`${base}/${id}/dependencies/${dependsOnId}`);
    return data;
  },
  async removeDependency(id, dependsOnId) {
    const { data } = await axiosClient.delete(`${base}/${id}/dependencies/${dependsOnId}`);
    return data;
  },
  async remove(id) {
    await axiosClient.delete(`${base}/${id}`);
  },
};
