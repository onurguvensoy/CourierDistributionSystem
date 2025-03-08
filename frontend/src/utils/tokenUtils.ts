import { jwtDecode } from 'jwt-decode';

export interface DecodedToken {
  exp: number;
  sub: string; // username
  role: string;
  userId: number;
  iat: number;
}

const REFRESH_THRESHOLD = 5 * 60 * 1000; // 5 minutes
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

let refreshTokenPromise: Promise<string> | null = null;

export const setupTokenRefresh = (token: string) => {
  if (!token) return;

  try {
    const decoded = jwtDecode<DecodedToken>(token);
    const expiresIn = (decoded?.exp || 0) * 1000 - Date.now();

    if (expiresIn > REFRESH_THRESHOLD) {
      setTimeout(() => refreshToken(), expiresIn - REFRESH_THRESHOLD);
    } else {
      refreshToken();
    }
  } catch (error) {
    console.error('Error setting up token refresh:', error);
  }
};

export const decodeToken = (token: string): DecodedToken | null => {
  try {
    return jwtDecode<DecodedToken>(token);
  } catch (error) {
    console.error('Error decoding token:', error);
    return null;
  }
};

export const isTokenExpired = (token: string): boolean => {
  const decoded = decodeToken(token);
  if (!decoded) return true;
  

  const expirationTime = decoded.exp * 1000;
  const currentTime = Date.now();
  
  return currentTime >= expirationTime;
};

export const getStoredToken = (): string | null => {
  return localStorage.getItem('token');
};

export const setStoredToken = (token: string): void => {
  localStorage.setItem('token', token);
};

export const removeStoredToken = (): void => {
  localStorage.removeItem('token');
};

export const refreshToken = async (): Promise<string> => {
  if (refreshTokenPromise) {
    return refreshTokenPromise;
  }

  refreshTokenPromise = new Promise(async (resolve, reject) => {
    try {
      const currentToken = getStoredToken();
      if (!currentToken) {
        throw new Error('No token available');
      }

      const response = await fetch(`${API_URL}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${currentToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error('Failed to refresh token');
      }

      const data = await response.json();
      const newToken = data.token;
      setStoredToken(newToken);
      setupTokenRefresh(newToken);
      resolve(newToken);
    } catch (error) {
      removeStoredToken();
      reject(error);
    } finally {
      refreshTokenPromise = null;
    }
  });

  return refreshTokenPromise;
};

export const getUsernameFromToken = (token: string | null): string | null => {
  if (!token) return null;
  const decoded = decodeToken(token);
  return decoded?.sub || null;
};