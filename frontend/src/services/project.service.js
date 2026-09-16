import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors project-service ProjectController (/api/v1/projects).
const base = API_ROUTES.projects;

export const projectService = {
  async list({ query, status, managerId, page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(base, { params: { query, status, managerId, page, size, sort } });
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
  async addMember(id, { employeeId, roleOnProject }) {
    const { data } = await axiosClient.post(`${base}/${id}/members`, { employeeId, roleOnProject });
    return data;
  },
  async removeMember(id, employeeId) {
    const { data } = await axiosClient.delete(`${base}/${id}/members/${employeeId}`);
    return data;
  },
  async requestClosure(id, remarks) {
    const { data } = await axiosClient.post(`${base}/${id}/request-closure`, { remarks });
    return data;
  },
  async remove(id) {
    await axiosClient.delete(`${base}/${id}`);
  },
  async health(id) {
    const { data } = await axiosClient.get(`${base}/${id}/health`);
    return data;
  },
};
