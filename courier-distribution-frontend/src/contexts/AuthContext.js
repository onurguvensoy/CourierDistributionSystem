import React, { createContext, useState, useContext, useEffect } from 'react';
import authService from '../services/authService';
import websocketService from '../services/websocketService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const initializeAuth = () => {
            const currentUser = authService.getCurrentUser();
            if (currentUser && authService.isTokenValid()) {
                setUser(currentUser);
                websocketService.connect();
            }
            setLoading(false);
        };

        initializeAuth();
    }, []);

    const login = async (username, password) => {
        try {
            const userData = await authService.login(username, password);
            setUser(userData);
            websocketService.connect();
            return userData;
        } catch (error) {
            throw error;
        }
    };

    const logout = () => {
        websocketService.disconnect();
        authService.logout();
        setUser(null);
    };

    const register = async (userData) => {
        try {
            return await authService.register(userData);
        } catch (error) {
            throw error;
        }
    };

    const value = {
        user,
        loading,
        login,
        logout,
        register,
        isAuthenticated: !!user,
        role: user?.role || null,
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export default AuthContext; 