import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors project-service ClientController (/api/v1/clients).
const base = API_ROUTES.clients;

export const clientService = {
  async list({ page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(base, { params: { page, size, sort } });
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
  async remove(id) {
    await axiosClient.delete(`${base}/${id}`);
  },
};
