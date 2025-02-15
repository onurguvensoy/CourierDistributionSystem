import React from 'react';
import { Routes, Route, Navigate, useLocation, Outlet } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import '@fortawesome/fontawesome-free/css/all.min.css';
import MainLayout from './components/layout/MainLayout';
import Login from './components/auth/Login';
import Signup from './components/auth/Signup';
import authService from './services/authService';

// Admin Pages
import AdminDashboard from './components/admin/Dashboard';
import Reports from './components/admin/Reports';

// Courier Pages
import CourierDashboard from './components/courier/Dashboard';
import Deliveries from './components/courier/Deliveries';
import DeliveryHistory from './components/courier/DeliveryHistory';

// Customer Pages
import CustomerDashboard from './components/customer/Dashboard';
import CustomerPackages from './components/customer/Packages';
import NewPackage from './components/customer/NewPackage';
import PackageTracking from './components/customer/PackageTracking';

// Common Pages
import Profile from './components/common/Profile';
import Settings from './components/common/Settings';

const ProtectedRoute = ({ children, allowedRoles }) => {
    const isAuthenticated = authService.isAuthenticated();
    const userRole = authService.getRole();
    const location = useLocation();

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (allowedRoles && (!userRole || !allowedRoles.includes(userRole))) {
        return <Navigate to="/" state={{ from: location }} replace />;
    }

    return children || <Outlet />;
};

const DefaultRoute = () => {
    const isAuthenticated = authService.isAuthenticated();
    const userRole = authService.getRole();
    const location = useLocation();

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (userRole) {
        return <Navigate to={`/${userRole.toLowerCase()}/dashboard`} replace />;
    }

    return <Navigate to="/login" replace />;
};

const App = () => {
    return (
        <>
            <Routes>
                {/* Public Routes */}
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />

                {/* Protected Routes */}
                <Route path="/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
                    {/* Admin Routes */}
                    <Route
                        path="admin/dashboard"
                        element={<ProtectedRoute allowedRoles={['ADMIN']}><AdminDashboard /></ProtectedRoute>}
                    />
                    <Route
                        path="admin/reports"
                        element={<ProtectedRoute allowedRoles={['ADMIN']}><Reports /></ProtectedRoute>}
                    />

                    {/* Courier Routes */}
                    <Route
                        path="courier/dashboard"
                        element={<ProtectedRoute allowedRoles={['COURIER']}><CourierDashboard /></ProtectedRoute>}
                    />
                    <Route
                        path="courier/deliveries"
                        element={<ProtectedRoute allowedRoles={['COURIER']}><Deliveries /></ProtectedRoute>}
                    />
                    <Route
                        path="courier/history"
                        element={<ProtectedRoute allowedRoles={['COURIER']}><DeliveryHistory /></ProtectedRoute>}
                    />

                    {/* Customer Routes */}
                    <Route
                        path="customer/dashboard"
                        element={<ProtectedRoute allowedRoles={['CUSTOMER']}><CustomerDashboard /></ProtectedRoute>}
                    />
                    <Route
                        path="customer/packages"
                        element={<ProtectedRoute allowedRoles={['CUSTOMER']}><CustomerPackages /></ProtectedRoute>}
                    />
                    <Route
                        path="customer/new-package"
                        element={<ProtectedRoute allowedRoles={['CUSTOMER']}><NewPackage /></ProtectedRoute>}
                    />
                    <Route
                        path="customer/tracking/:packageId"
                        element={<ProtectedRoute allowedRoles={['CUSTOMER']}><PackageTracking /></ProtectedRoute>}
                    />

                    {/* Common Routes */}
                    <Route path="profile" element={<Profile />} />
                    <Route path="settings" element={<Settings />} />

                    {/* Default Route */}
                    <Route index element={<DefaultRoute />} />
                </Route>

                {/* Catch all route */}
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
            <ToastContainer
                position="top-right"
                autoClose={5000}
                hideProgressBar={false}
                newestOnTop
                closeOnClick
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
            />
        </>
    );
};

export default App;
