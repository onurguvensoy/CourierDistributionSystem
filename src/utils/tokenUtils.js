import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const axiosInstance = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

let refreshTimeout;

export const setupTokenRefresh = (token) => {
    if (refreshTimeout) {
        clearTimeout(refreshTimeout);
    }

    try {
        const decoded = jwtDecode(token);
        const expiresIn = decoded.exp * 1000 - Date.now();
        const refreshTime = expiresIn - 60000; // Refresh 1 minute before expiration

        if (refreshTime > 0) {
            refreshTimeout = setTimeout(refreshToken, refreshTime);
        }
    } catch (error) {
        console.error('Error setting up token refresh:', error);
    }
};

export const refreshToken = async () => {
    try {
        const currentToken = localStorage.getItem('token');
        
        if (!currentToken) {
            throw new Error('No token available');
        }

        const response = await axiosInstance.post('/auth/refresh', null, {
            headers: {
                'Authorization': `Bearer ${currentToken}`
            }
        });

        if (response.data && response.data.token) {
            const newToken = response.data.token;
            localStorage.setItem('token', newToken);
            
            // Update user info with new token data
            const decoded = jwtDecode(newToken);
            const user = {
                id: decoded.sub,
                username: decoded.username,
                role: decoded.role,
                exp: decoded.exp
            };
            localStorage.setItem('user', JSON.stringify(user));
            
            // Setup next refresh
            setupTokenRefresh(newToken);
            
            return newToken;
        }
        throw new Error('No token in refresh response');
    } catch (error) {
        console.error('Token refresh failed:', error);
        // If refresh fails, logout user
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
        throw error;
    }
};

export const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return token ? { Authorization: `Bearer ${token}` } : {};
}; 