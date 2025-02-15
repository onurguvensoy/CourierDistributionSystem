import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance with default config
const axiosInstance = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    withCredentials: true
});

// Add a request interceptor
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

// Add a response interceptor
axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            // Handle token expiration
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

const authService = {
    async login(username, password) {
        try {
            const response = await axiosInstance.post('/auth/login', {
                username,
                password
            });
            
            if (response.data.token) {
                localStorage.setItem('token', response.data.token);
                
                // Decode token and store user info
                const decodedToken = jwtDecode(response.data.token);
                const user = {
                    id: decodedToken.sub,
                    username: decodedToken.username,
                    role: decodedToken.role,
                    exp: decodedToken.exp
                };
                localStorage.setItem('user', JSON.stringify(user));
                
                return user;
            }
            throw new Error('No token received');
        } catch (error) {
            if (error.response) {
                throw new Error(error.response.data.message || 'Login failed');
            }
            throw new Error('Network error occurred');
        }
    },

    async signup(userData) {
        try {
            const response = await axiosInstance.post('/auth/signup', userData);
            return response.data;
        } catch (error) {
            if (error.response) {
                throw new Error(error.response.data.message || 'Signup failed');
            }
            throw new Error('Network error occurred');
        }
    },

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
    },

    getCurrentUser() {
        try {
            const userStr = localStorage.getItem('user');
            if (!userStr) return null;
            
            const user = JSON.parse(userStr);
            if (!user) return null;
            
            // Check if token is expired
            if (user.exp && user.exp * 1000 < Date.now()) {
                this.logout();
                return null;
            }
            
            return user;
        } catch (error) {
            return null;
        }
    },

    getToken() {
        return localStorage.getItem('token');
    },

    isAuthenticated() {
        const user = this.getCurrentUser();
        return !!user;
    },

    getRole() {
        const user = this.getCurrentUser();
        return user ? user.role : null;
    }
};

export default authService; 