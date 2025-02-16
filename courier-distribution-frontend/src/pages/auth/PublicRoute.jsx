import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import authService from '../../services/authService';

const PublicRoute = () => {
    const location = useLocation();
    const isAuthenticated = authService.isAuthenticated();
    const userRole = authService.getRole();

    if (isAuthenticated) {
        const from = location.state?.from?.pathname || `/${userRole.toLowerCase()}/dashboard`;
        return <Navigate to={from} replace />;
    }

    return <Outlet />;
};

export default PublicRoute; 