import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import authService from '../../services/authService';

const PrivateRoute = ({ allowedRoles }) => {
    const location = useLocation();
    const isAuthenticated = authService.isAuthenticated();
    const userRole = authService.getRole();

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (allowedRoles && (!userRole || !allowedRoles.includes(userRole))) {
        return <Navigate to="/" state={{ from: location }} replace />;
    }

    return <Outlet />;
};

export default PrivateRoute; 