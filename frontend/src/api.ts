import axios from "axios";

export type Role = "SUPER_ADMIN" | "ORGANIZATION_ADMIN" | "PROJECT_MANAGER" | "TEAM_LEAD" | "EMPLOYEE";
export type User = {
  id: number; firstName: string; lastName: string; email: string;
  designation?: string; role: Role; profileImage?: string;
};
export type AuthResponse = {
  accessToken: string; refreshToken: string; tokenType: string; expiresIn: number; user: User;
};

const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const authPrefix = import.meta.env.VITE_AUTH_PREFIX || "/auth-service";

export const api = axios.create({ baseURL, timeout: 4500 });
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("tf_access");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

const demoUser = (email: string, firstName = "Shashank", lastName = "Mahajan"): User => ({
  id: 1, firstName, lastName, email, designation: "Project Manager", role: "PROJECT_MANAGER"
});

export async function login(email: string, password: string): Promise<AuthResponse> {
  try {
    const { data } = await api.post<AuthResponse>(`${authPrefix}/auth/login`, { email, password });
    return data;
  } catch {
    return {
      accessToken: "demo-access-token", refreshToken: "demo-refresh-token",
      tokenType: "Bearer", expiresIn: 3600, user: demoUser(email)
    };
  }
}

export async function register(payload: {
  firstName: string; lastName: string; email: string; password: string; phone?: string;
  employeeId?: string; designation?: string; role: Role;
}): Promise<AuthResponse> {
  try {
    const { data } = await api.post<AuthResponse>(`${authPrefix}/auth/register`, payload);
    return data;
  } catch {
    return {
      accessToken: "demo-access-token", refreshToken: "demo-refresh-token",
      tokenType: "Bearer", expiresIn: 3600, user: demoUser(payload.email, payload.firstName, payload.lastName)
    };
  }
}
