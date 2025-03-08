import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from '../components/ProtectedRoute';
import RoleBasedRoute from '../components/RoleBasedRoute';
import Login from '../pages/auth/Login';
import Signup from '../pages/auth/Signup';
import CustomerDashboard from '../pages/customer/Dashboard';
import CourierDashboard from '../pages/courier/Dashboard';
import CourierDeliveryHistory from '../pages/courier/DeliveryHistory';
import AdminDashboard from '../pages/admin/Dashboard';
import Profile from '../pages/common/Profile';
import Settings from '../pages/common/Settings';
import NewPackage from '../pages/customer/NewPackage';
import DeliveryHistory from '../pages/customer/DeliveryHistory';
import PackageTracking from '../pages/customer/PackageTracking';
import Packages from '../pages/customer/Packages';
import DashboardLayout from '../components/Layout/DashboardLayout';
import Users from '../pages/admin/Users';
import TrackPackageSearch from '../pages/customer/TrackPackageSearch';
import RoleBasedRedirect from '../components/RoleBasedRedirect';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      
      {/* Protected Routes with DashboardLayout */}
      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        {/* Customer Routes */}
        <Route path="/customer">
          <Route
            index
            element={
              <RoleBasedRoute allowedRoles={['CUSTOMER']}>
                <Navigate to="dashboard" replace />
              </RoleBasedRoute>
            }
          />
          <Route
            path="dashboard"
            element={
              <RoleBasedRoute allowedRoles={['CUSTOMER']}>
                <CustomerDashboard />
              </RoleBasedRoute>
            }
          />
          <Route
            path="packages"
            element={
              <RoleBasedRoute allowedRoles={['CUSTOMER']}>
                <Packages />
              </RoleBasedRoute>
            }
          />
          <Route
            path="new-package"
            element={
              <RoleBasedRoute allowedRoles={['CUSTOMER']}>
                <NewPackage />
              </RoleBasedRoute>
            }
          />
          <Route
            path="tracking"
            element={
              <RoleBasedRoute allowedRoles={['CUSTOMER']}>
                <TrackPackageSearch />
              </RoleBasedRoute>
            }
          />
          <Route
            path="tracking/:trackingNumber"
            element={
              <RoleBasedRoute allowedRoles={['CUSTOMER']}>
                <PackageTracking />
              </RoleBasedRoute>
            }
          />
          <Route
            path="history"
            element={
              <RoleBasedRoute allowedRoles={['CUSTOMER']}>
                <DeliveryHistory />
              </RoleBasedRoute>
            }
          />
        </Route>

        {/* Courier Routes */}
        <Route path="/courier">
          <Route
            index
            element={
              <RoleBasedRoute allowedRoles={['COURIER']}>
                <Navigate to="dashboard" replace />
              </RoleBasedRoute>
            }
          />
          <Route
            path="dashboard"
            element={
              <RoleBasedRoute allowedRoles={['COURIER']}>
                <CourierDashboard />
              </RoleBasedRoute>
            }
          />
          <Route
            path="history"
            element={
              <RoleBasedRoute allowedRoles={['COURIER']}>
                <CourierDeliveryHistory />
              </RoleBasedRoute>
            }
          />
        </Route>

        {/* Admin Routes */}
        <Route path="/admin">
          <Route
            index
            element={
              <RoleBasedRoute allowedRoles={['ADMIN']}>
                <Navigate to="dashboard" replace />
              </RoleBasedRoute>
            }
          />
          <Route
            path="dashboard"
            element={
              <RoleBasedRoute allowedRoles={['ADMIN']}>
                <AdminDashboard />
              </RoleBasedRoute>
            }
          />
          <Route
            path="users"
            element={
              <RoleBasedRoute allowedRoles={['ADMIN']}>
                <Users />
              </RoleBasedRoute>
            }
          />
        </Route>

        {/* Common Protected Routes */}
        <Route path="/profile" element={<Profile />} />
        <Route path="/settings" element={<Settings />} />
      </Route>

      {/* Redirect root based on role */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <RoleBasedRedirect />
          </ProtectedRoute>
        }
      />

      {/* Catch all */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default AppRoutes; 