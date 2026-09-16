import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors ai-service NotificationController (/api/v1/notifications).
const base = API_ROUTES.notifications;

export const notificationService = {
  async list({ unreadOnly = false, page = 0, size = 20, sort } = {}) {
    const { data } = await axiosClient.get(base, { params: { unreadOnly, page, size, sort } });
    return data;
  },
  async unreadCount() {
    const { data } = await axiosClient.get(`${base}/unread-count`);
    return data;
  },
  async markRead(id) {
    const { data } = await axiosClient.patch(`${base}/${id}/read`);
    return data;
  },
  async markAllRead() {
    const { data } = await axiosClient.patch(`${base}/read-all`);
    return data;
  },
  async remove(id) {
    await axiosClient.delete(`${base}/${id}`);
  },
};
