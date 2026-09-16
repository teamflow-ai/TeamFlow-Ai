import axiosClient from './axiosClient';

// Mirrors identity-service RoleController (/api/v1/roles, /api/v1/permissions).
export const roleService = {
  async listRoles() {
    const { data } = await axiosClient.get('/roles');
    return data; // RoleResponse[]
  },
  async listPermissions() {
    const { data } = await axiosClient.get('/permissions');
    return data; // PermissionResponse[]
  },
};
