import React from 'react';
import { Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { RootState } from '../store';

const RoleBasedRedirect: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);
  
  const getDefaultRoute = () => {
    const role = user?.role?.toLowerCase();
    switch (role) {
      case 'customer':
        return '/customer/dashboard';
      case 'courier':
        return '/courier/dashboard';
      case 'admin':
        return '/admin/dashboard';
      default:
        return '/login';
    }
  };

  return <Navigate to={getDefaultRoute()} replace />;
};

export default RoleBasedRedirect; 