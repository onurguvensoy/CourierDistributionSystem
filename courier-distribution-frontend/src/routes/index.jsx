import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from '../pages/auth/Login';
import Signup from '../pages/auth/Signup';
import ForgotPassword from '../pages/auth/ForgotPassword';
import Profile from '../components/common/Profile';
import Settings from '../components/common/Settings';

// Admin Components
import AdminDashboard from '../pages/admin/Dashboard';
import Reports from '../pages/admin/Reports';

// Courier Components
import CourierDashboard from '../components/courier/Dashboard';
import Deliveries from '../components/courier/Deliveries';
import DeliveryHistory from '../components/courier/DeliveryHistory';

// Customer Components
import CustomerDashboard from '../components/customer/Dashboard';
import Packages from '../components/customer/Packages';
import NewPackage from '../components/customer/NewPackage';
import PackageTracking from '../components/customer/PackageTracking';

// Layout Components
import PrivateRoute from './PrivateRoute';
import PublicRoute from './PublicRoute';
import MainLayout from '../components/layout/MainLayout';

const AppRoutes = () => {
    return (
        <Routes>
            {/* Public Routes */}
            <Route element={<PublicRoute />}>
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/forgot-password" element={<ForgotPassword />} />
            </Route>

            {/* Protected Routes */}
            <Route element={<PrivateRoute />}>
                <Route element={<MainLayout />}>
                    {/* Common Routes */}
                    <Route path="/profile" element={<Profile />} />
                    <Route path="/settings" element={<Settings />} />

                    {/* Admin Routes */}
                    <Route path="/admin">
                        <Route path="dashboard" element={<AdminDashboard />} />
                        <Route path="reports" element={<Reports />} />
                    </Route>

                    {/* Courier Routes */}
                    <Route path="/courier">
                        <Route path="dashboard" element={<CourierDashboard />} />
                        <Route path="deliveries" element={<Deliveries />} />
                        <Route path="history" element={<DeliveryHistory />} />
                    </Route>

                    {/* Customer Routes */}
                    <Route path="/customer">
                        <Route path="dashboard" element={<CustomerDashboard />} />
                        <Route path="packages" element={<Packages />} />
                        <Route path="new-package" element={<NewPackage />} />
                        <Route path="tracking/:packageId" element={<PackageTracking />} />
                    </Route>

                    {/* Redirect root to appropriate dashboard */}
                    <Route path="/" element={<Navigate to="/login" replace />} />
                </Route>
            </Route>
        </Routes>
    );
};

export default AppRoutes; 