import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import { authService } from '../services/auth.service';
import { tokenStorage } from '../services/axiosClient';

export const AuthContext = createContext(null);

const USER_KEY = 'teamflow_user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem(USER_KEY);
    return stored ? JSON.parse(stored) : null;
  });
  const [initializing, setInitializing] = useState(true);

  const persistUser = (nextUser) => {
    setUser(nextUser);
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
  };

  useEffect(() => {
    // Rehydrate the profile from the backend on a hard refresh whenever an
    // access token exists — this is also what confirms the role/permissions
    // used for redirects and navigation are always backend-sourced, not
    // whatever was last cached client-side.
    let cancelled = false;
    (async () => {
      if (tokenStorage.getAccess()) {
        try {
          const profile = await authService.getProfile();
          if (!cancelled) persistUser(profile);
        } catch {
          if (!cancelled) {
            tokenStorage.clear();
            localStorage.removeItem(USER_KEY);
            setUser(null);
          }
        }
      }
      if (!cancelled) setInitializing(false);
    })();
    return () => { cancelled = true; };
  }, []);

  useEffect(() => {
    const onForbidden = () => {
      window.location.href = '/403';
    };
    window.addEventListener('teamflow:forbidden', onForbidden);
    return () => window.removeEventListener('teamflow:forbidden', onForbidden);
  }, []);

  const login = useCallback(async (email, password) => {
    const res = await authService.login({ email, password });
    tokenStorage.set(res.accessToken, res.refreshToken);
    persistUser(res.user);
    return res.user;
  }, []);

  const register = useCallback(async (payload) => {
    const res = await authService.register(payload);
    tokenStorage.set(res.accessToken, res.refreshToken);
    persistUser(res.user);
    return res.user;
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout(tokenStorage.getRefresh());
    } finally {
      tokenStorage.clear();
      localStorage.removeItem(USER_KEY);
      setUser(null);
    }
  }, []);

  const hasPermission = useCallback(
    (permission) => {
      if (!permission) return true;
      if (!user) return false;
      // Trust only the permissions the backend resolved and returned on the
      // profile — never a client-side role shortcut. If a role should imply
      // every permission, that's granted server-side via the role's seed data.
      return (user.permissions || []).includes(permission);
    },
    [user]
  );

  const hasAnyPermission = useCallback(
    (permissionList = []) => permissionList.length === 0 || permissionList.some(hasPermission),
    [hasPermission]
  );

  const refreshProfile = useCallback(async () => {
    const profile = await authService.getProfile();
    persistUser(profile);
    return profile;
  }, []);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user,
      initializing,
      login,
      register,
      logout,
      hasPermission,
      hasAnyPermission,
      refreshProfile,
    }),
    [user, initializing, login, register, logout, hasPermission, hasAnyPermission, refreshProfile]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
