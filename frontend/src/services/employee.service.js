import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors identity-service EmployeeController (/api/v1/employees).
const base = API_ROUTES.employees;

export const employeeService = {
  async list({ query, departmentId, teamId, managerId, active, skill, page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(base, {
      params: { query, departmentId, teamId, managerId, active, skill, page, size, sort },
    });
    return data; // PageResponse<EmployeeResponse>
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
  async activate(id) {
    const { data } = await axiosClient.patch(`${base}/${id}/activate`);
    return data;
  },
  async deactivate(id) {
    const { data } = await axiosClient.patch(`${base}/${id}/deactivate`);
    return data;
  },
  async remove(id) {
    await axiosClient.delete(`${base}/${id}`);
  },
};
