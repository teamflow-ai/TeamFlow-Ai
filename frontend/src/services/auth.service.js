import axiosClient from './axiosClient';
import { API_ROUTES } from '../constants/app';

// Mirrors identity-service AuthController (/api/v1/auth):
//   POST /register  POST /login  POST /refresh-token
//   POST /logout    POST /logout-all  POST /change-password  GET /profile
const base = API_ROUTES.auth;

export const authService = {
  async login({ email, password }) {
    const { data } = await axiosClient.post(`${base}/login`, { email, password });
    return data; // AuthResponse { accessToken, refreshToken, tokenType, expiresIn, user }
  },

  async register({ firstName, lastName, email, password, roleName }) {
    const { data } = await axiosClient.post(`${base}/register`, {
      firstName,
      lastName,
      email,
      password,
      roleName,
    });
    return data;
  },

  async refreshToken(refreshToken) {
    const { data } = await axiosClient.post(`${base}/refresh-token`, { refreshToken });
    return data;
  },

  async getProfile() {
    const { data } = await axiosClient.get(`${base}/profile`);
    return data; // UserResponse
  },

  async logout(refreshToken) {
    if (!refreshToken) return;
    const { data } = await axiosClient.post(`${base}/logout`, { refreshToken });
    return data;
  },

  async logoutAll() {
    const { data } = await axiosClient.post(`${base}/logout-all`);
    return data;
  },

  async changePassword({ currentPassword, newPassword }) {
    const { data } = await axiosClient.post(`${base}/change-password`, {
      currentPassword,
      newPassword,
    });
    return data;
  },
};
