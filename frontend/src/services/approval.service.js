import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors project-service ApprovalController (/api/v1/approvals) — the
// Approval Workflow Engine's generic view/decision surface.
const base = API_ROUTES.approvals;

export const approvalService = {
  async list({ approvalType, status, approverId, projectId, page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(base, {
      params: { approvalType, status, approverId, projectId, page, size, sort },
    });
    return data;
  },
  async search(params) {
    return this.list(params);
  },
  async get(id) {
    const { data } = await axiosClient.get(`${base}/${id}`);
    return data;
  },
  async decide(id, { decision, remarks }) {
    const { data } = await axiosClient.patch(`${base}/${id}/decide`, { decision, remarks });
    return data;
  },
};
