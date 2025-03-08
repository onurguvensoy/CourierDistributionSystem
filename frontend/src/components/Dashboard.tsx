import { FC, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/auth.context';
import { isTokenExpired, getStoredToken } from '../utils/tokenUtils';

interface DashboardProps {
  children: React.ReactNode;
}

export const Dashboard: FC<DashboardProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, refreshToken } = useAuth();

  useEffect(() => {
    const validateToken = async () => {
      const token = getStoredToken();
      if (!token) {
        console.log('No token found, redirecting to login');
        navigate('/login', { replace: true });
        return;
      }

      if (isTokenExpired(token)) {
        try {
          console.log('Token expired, attempting refresh');
          await refreshToken();
        } catch (error) {
          console.error('Token refresh failed:', error);
          navigate('/login', { replace: true });
        }
      }
    };

    validateToken();
    
    // Check token every minute
    const interval = setInterval(validateToken, 60000);
    return () => clearInterval(interval);
  }, [navigate, refreshToken]);

  useEffect(() => {

    if (user) {
      const role = user.role.toLowerCase();
      const currentPath = location.pathname;
      if (!currentPath.includes(`/${role}/`)) {
        console.log('User does not have access to this route, redirecting to their dashboard');
        navigate(`/${role}/dashboard`, { replace: true });
      }
    }
  }, [user, location, navigate]);

  if (!user) {
    return null;
  }

  return <>{children}</>;
}; 