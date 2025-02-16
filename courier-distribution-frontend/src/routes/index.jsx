import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

// Auth Pages
import Login from '../pages/auth/Login';
import Signup from '../pages/auth/Signup';
import ForgotPassword from '../pages/auth/ForgotPassword';

// Layout Components
import PrivateRoute from '../components/common/PrivateRoute';
import PublicRoute from '../components/common/PublicRoute';
import MainLayout from '../components/layout/MainLayout';

// Admin Pages
import AdminDashboard from '../pages/admin/Dashboard';
import Reports from '../pages/admin/Reports';

// Courier Pages
import CourierDashboard from '../pages/courier/Dashboard';
import Deliveries from '../pages/courier/Deliveries';
import DeliveryHistory from '../pages/courier/DeliveryHistory';

// Customer Pages
import CustomerDashboard from '../pages/customer/Dashboard';
import Packages from '../pages/customer/Packages';
import NewPackage from '../pages/customer/NewPackage';
import PackageTracking from '../pages/customer/PackageTracking';

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