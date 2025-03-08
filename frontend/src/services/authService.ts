import { useHttpClient } from '../utils/http-client';
import { getStoredToken, setStoredToken, removeStoredToken } from '../utils/tokenUtils';

interface LoginResponse {
  token: string;
}

interface RefreshResponse {
  token: string;
}

export const useAuthService = () => {
  const { post } = useHttpClient();

  const login = async (username: string, password: string): Promise<void> => {
    const response = await post<LoginResponse>('/auth/login', { username, password }, { requiresAuth: false });
    setStoredToken(response.token);
  };

  const logout = (): void => {
    removeStoredToken();
  };

  const refreshToken = async (): Promise<void> => {
    const currentToken = getStoredToken();
    if (!currentToken) throw new Error('No token found');

    const response = await post<RefreshResponse>('/auth/refresh', {}, {
      headers: {
        'Authorization': `Bearer ${currentToken}`
      }
    });
    
    setStoredToken(response.token);
  };

  return {
    login,
    logout,
    refreshToken
  };
}; 