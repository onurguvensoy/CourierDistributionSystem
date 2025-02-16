import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const PublicRoute = () => {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user'));

    if (token && user) {
        const role = user.role.toLowerCase();
        return <Navigate to={`/${role}/dashboard`} replace />;
    }

    return <Outlet />;
};

export default PublicRoute; 