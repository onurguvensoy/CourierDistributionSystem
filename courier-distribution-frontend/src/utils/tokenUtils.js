import { Axios } from 'axios';
import { jwtDecode } from 'jwt-decode';

const REFRESH_THRESHOLD = 4.5 * 60 * 1000; // 4.5 minutes in milliseconds
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const axiosInstance = new Axios({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

let refreshTokenPromise = null;

export const setupTokenRefresh = (token) => {
    if (!token) return;

    try {
        const decoded = jwtDecode(token);
        const expiresIn = decoded.exp * 1000 - Date.now();
        
        if (expiresIn > REFRESH_THRESHOLD) {
            // Schedule refresh before token expires
            setTimeout(() => refreshToken(), expiresIn - REFRESH_THRESHOLD);
        } else {
            // Token is close to expiring, refresh now
            refreshToken();
        }
    } catch (error) {
        console.error('Error setting up token refresh:', error);
    }
};

export const refreshToken = async () => {
    // If there's already a refresh request in progress, return that promise
    if (refreshTokenPromise) {
        return refreshTokenPromise;
    }

    refreshTokenPromise = new Promise(async (resolve, reject) => {
        try {
            const currentToken = localStorage.getItem('token');
            
            if (!currentToken) {
                throw new Error('No token available');
            }

            const response = await axiosInstance.post(
                '/auth/refresh',
                {},
                {
                    headers: {
                        Authorization: `Bearer ${currentToken}`
                    }
                }
            );

            const newToken = response.data.token;
            localStorage.setItem('token', newToken);

            // Set up refresh for the new token
            setupTokenRefresh(newToken);

            resolve(newToken);
        } catch (error) {
            // If refresh fails, redirect to login
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
            reject(error);
        } finally {
            refreshTokenPromise = null;
        }
    });

    return refreshTokenPromise;
}; 