import React, { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/auth.context';

interface RoleBasedRouteProps {
  children: React.ReactNode;
  allowedRoles: string[];
}

const RoleBasedRoute: React.FC<RoleBasedRouteProps> = ({ children, allowedRoles }) => {
  const location = useLocation();
  const { user, isAuthenticated, checkAuthorization } = useAuth();

  useEffect(() => {
    if (isAuthenticated && user?.role) {
      checkAuthorization(user.role);
    }
  }, [isAuthenticated, user?.role, checkAuthorization]);

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (!user?.role || !allowedRoles.includes(user.role)) {

    const roleBasedPath = user?.role?.toLowerCase() === 'customer' 
      ? '/customer/dashboard'
      : user?.role?.toLowerCase() === 'courier'
      ? '/courier/dashboard'
      : user?.role?.toLowerCase() === 'admin'
      ? '/admin/dashboard'
      : '/login';
    
    return <Navigate to={roleBasedPath} replace />;
  }

  return <>{children}</>;
};

export default RoleBasedRoute; 