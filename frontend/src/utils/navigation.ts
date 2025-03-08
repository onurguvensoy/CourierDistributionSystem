import { NavigateFunction } from 'react-router-dom';

export const getDashboardPath = (role: string | null): string => {
  switch (role?.toUpperCase()) {
    case 'COURIER':
      return '/courier/dashboard';
    case 'CUSTOMER':
      return '/customer/dashboard';
    case 'ADMIN':
      return '/admin/dashboard';
    default:
      return '/login';
  }
};

export const navigateToDashboard = (
  navigate: NavigateFunction,
  role: string | null,
  fallbackPath: string = '/login'
): void => {
  const dashboardPath = getDashboardPath(role);
  navigate(dashboardPath || fallbackPath);
}; 