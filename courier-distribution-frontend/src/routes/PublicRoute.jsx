import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import authService from '../services/authService';

const PublicRoute = () => {
    const isAuthenticated = authService.isAuthenticated();
    const userRole = authService.getRole()?.toLowerCase();
    
    if (isAuthenticated) {
        return <Navigate to={`/${userRole}/dashboard`} replace />;
    }
    
    return <Outlet />;
};

export default PublicRoute; 