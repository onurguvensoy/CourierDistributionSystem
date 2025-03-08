import axios from 'axios';
import type { AuthFormFields } from '../../components/common/AuthForm';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

interface SignupResponse {
  token: string;
  username: string;
  role: string;
  userId: string;
}

interface RefreshTokenResponse {
  token: string;
  username: string;
  role: string;
  userId: string;
}

export const authApi = {
  async login(credentials: Pick<AuthFormFields, 'username' | 'password'>): Promise<LoginResponse> {
    try {
      const response = await axiosInstance.post<LoginResponse>('/auth/login', credentials);
      
      if (!response.data.token) {
        throw new Error('No token received from server');
      }
      localStorage.setItem('token', response.data.token);
      axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`;
      
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.error('Login error:', error.response?.data);
        if (error.response?.status === 403) {
          throw new Error('Invalid username or password');
        }
        throw new Error(error.response?.data?.message || 'Login failed');
      }
      throw error;
    }
  },

  async signup(userData: {
    username: string;
    email: string;
    password: string;
    role: string;
  }): Promise<SignupResponse> {
    try {
      const response = await axiosInstance.post<SignupResponse>('/auth/signup', userData);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.error('Signup error:', error.response?.data);
        throw new Error(error.response?.data?.message || 'Signup failed');
      }
      throw error;
    }
  },

  async logout() {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        await axiosInstance.post('/auth/logout', {}, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem('token');
      delete axiosInstance.defaults.headers.common['Authorization'];
    }
  },

  getStoredToken() {
    return localStorage.getItem('token');
  },

  setAuthHeader(token: string) {
    axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  },

  async refreshToken(): Promise<RefreshTokenResponse> {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('No token found');
      }

      const response = await axiosInstance.post<RefreshTokenResponse>('/auth/refresh', {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!response.data.token) {
        throw new Error('No token received from server');
      }

      // Update token in localStorage and axios header
      localStorage.setItem('token', response.data.token);
      axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`;

      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.error('Token refresh error:', error.response?.data);
        throw new Error(error.response?.data?.message || 'Token refresh failed');
      }
      throw error;
    }
  }
};

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 403) {
      console.error('Authentication failed:', error.response.data);
      localStorage.removeItem('token');
      delete axiosInstance.defaults.headers.common['Authorization'];
    }
    return Promise.reject(error);
  }
);


axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
); 