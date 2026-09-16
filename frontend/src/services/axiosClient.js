import axios from 'axios';

/**
 * Single axios instance for the whole app, pointed at the api-gateway.
 * Every request goes through one of the gateway route groups (see
 * src/constants/app.js -> API_ROUTES) and comes back wrapped in the
 * platform-wide ApiResponse<T> envelope { status, success, message, data, errors, path }.
 * The response interceptor below unwraps to that envelope so call sites do
 * `const { data } = await axiosClient.get(...)` to reach the payload directly.
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const axiosClient = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 20000,
});

const TOKEN_KEY = 'teamflow_access_token';
const REFRESH_KEY = 'teamflow_refresh_token';

export const tokenStorage = {
  getAccess: () => localStorage.getItem(TOKEN_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  set: (accessToken, refreshToken) => {
    localStorage.setItem(TOKEN_KEY, accessToken);
    if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken);
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
  },
};

axiosClient.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let queue = [];

const resolveQueue = (error, token = null) => {
  queue.forEach((p) => (error ? p.reject(error) : p.resolve(token)));
  queue = [];
};

const normalizeError = (error) => {
  const status = error.response?.status;
  const payload = error.response?.data;
  const message =
    payload?.message ||
    (status === 0 || !error.response ? 'Cannot reach the server. Check your connection and try again.' : error.message) ||
    'Something went wrong';
  const normalized = new Error(message);
  normalized.status = status;
  normalized.errors = payload?.errors || [];
  normalized.raw = error;
  return normalized;
};

axiosClient.interceptors.response.use(
  (response) => response.data, // unwraps the ApiResponse<T> envelope
  async (error) => {
    const originalRequest = error.config || {};

    // 15-minute access tokens rotate against /auth/refresh-token; a 401 here
    // triggers exactly one in-flight refresh, queuing concurrent requests.
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/auth/refresh-token') &&
      !originalRequest.url?.includes('/auth/login') &&
      tokenStorage.getRefresh()
    ) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          queue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return axiosClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshed = await axios.post(`${BASE_URL}/auth/refresh-token`, {
          refreshToken: tokenStorage.getRefresh(),
        });
        const payload = refreshed.data?.data;
        tokenStorage.set(payload.accessToken, payload.refreshToken);
        resolveQueue(null, payload.accessToken);
        originalRequest.headers.Authorization = `Bearer ${payload.accessToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        resolveQueue(refreshError, null);
        tokenStorage.clear();
        localStorage.removeItem('teamflow_user');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
        return Promise.reject(normalizeError(refreshError));
      } finally {
        isRefreshing = false;
      }
    }

    if (error.response?.status === 403 && !originalRequest.url?.includes('/auth/')) {
      window.dispatchEvent(new CustomEvent('teamflow:forbidden'));
    }

    return Promise.reject(normalizeError(error));
  }
);

export default axiosClient;
