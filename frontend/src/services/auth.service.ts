import axios from 'axios';
import { getStoredToken, setStoredToken, removeStoredToken } from '../utils/tokenUtils';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';


export interface AuthService {
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
}

export const useAuthService = (): AuthService => {
  const login = async (username: string, password: string): Promise<void> => {
    try {
      const response = await axios.post(
        `${API_URL}/auth/login`,
        { username, password }
      );
      
      if (response.data.token) {
        setStoredToken(response.data.token);
      } else {
        throw new Error('No token in response');
      }
    } catch (error) {
      console.error('Login error:', error);
      removeStoredToken();
      throw error;
    }
  };

  const logout = (): void => {
    removeStoredToken();
  };

  const refreshToken = async (): Promise<void> => {
    try {
      const currentToken = getStoredToken();
      if (!currentToken) {
        throw new Error('No token found');
      }

      const response = await axios.post(
        `${API_URL}/auth/refresh`,
        {},
        {
          headers: {
            'Authorization': `Bearer ${currentToken}`,
            'Content-Type': 'application/json'
          }
        }
      );

      if (response.data.token) {
        setStoredToken(response.data.token);
      } else {
        throw new Error('No token in refresh response');
      }
    } catch (error) {
      console.error('Token refresh error:', error);
      removeStoredToken();
      throw error;
    }
  };

  return {
    login,
    logout,
    refreshToken
  };
};

// Default export for backward compatibility
export default useAuthService; 