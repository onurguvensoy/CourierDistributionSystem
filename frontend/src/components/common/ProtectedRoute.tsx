import { useEffect } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from '../../store';
import { getDashboardPath } from '../../utils/navigation';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles = []
}) => {
  const { user, isAuthenticated } = useSelector((state: RootState) => state.auth);
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {

    if (isAuthenticated && user?.role) {
      const correctPath = getDashboardPath(user.role);
      const currentPath = location.pathname;
      

      if (currentPath.includes('/dashboard') && currentPath !== correctPath) {
        navigate(correctPath);
      }
    }
  }, [user?.role, isAuthenticated, location.pathname, navigate]);

  if (!isAuthenticated) {

    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles.length > 0 && user?.role && !allowedRoles.includes(user.role)) {

    return <Navigate to={getDashboardPath(user.role)} replace />;
  }

  return <>{children}</>;
}; 