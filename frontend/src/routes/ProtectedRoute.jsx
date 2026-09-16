import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import LoadingSpinner from '../components/common/LoadingSpinner';

export default function ProtectedRoute({ children, permission, anyPermission }) {
  const { isAuthenticated, hasPermission, hasAnyPermission, initializing } = useAuth();
  const location = useLocation();

  if (initializing) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <LoadingSpinner size="lg" label="Loading TeamFlow AI…" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }
  if (permission && !hasPermission(permission)) {
    return <Navigate to="/403" replace />;
  }
  if (anyPermission && !hasAnyPermission(anyPermission)) {
    return <Navigate to="/403" replace />;
  }
  return children;
}
