import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Login = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        password: '',
    });
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const setupAxiosInterceptors = (token) => {
        // Set default authorization header
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

        // Add response interceptor for handling token-related errors
        axios.interceptors.response.use(
            (response) => response,
            (error) => {
                if (error.response?.status === 401 || error.response?.status === 403) {
                    // Clear auth data and redirect to login
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    delete axios.defaults.headers.common['Authorization'];
                    navigate('/login');
                    toast.error('Session expired. Please login again.');
                }
                return Promise.reject(error);
            }
        );
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            // Validate input
            if (!formData.username.trim() || !formData.password.trim()) {
                toast.error('Please enter both username and password');
                return;
            }

            const response = await axios.post(`${API_URL}/auth/login`, formData);
            const { token, username, role, userId } = response.data;
            
            if (!token || !username || !role) {
                throw new Error('Invalid response from server');
            }

            // Store auth data
            const userData = {
                username,
                role,
                userId,
                lastLogin: new Date().toISOString()
            };

            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(userData));

            // Setup axios interceptors
            setupAxiosInterceptors(token);

            // Determine redirect path based on role
            let redirectPath;
            switch (role.toUpperCase()) {
                case 'ADMIN':
                    redirectPath = '/admin/dashboard';
                    break;
                case 'COURIER':
                    redirectPath = '/courier/dashboard';
                    break;
                case 'CUSTOMER':
                    redirectPath = '/customer/dashboard';
                    break;
                default:
                    redirectPath = '/';
            }

            toast.success('Login successful!');
            navigate(redirectPath);
        } catch (error) {
            let errorMessage = 'Failed to login';
            
            if (error.response) {
                // Handle specific error messages from the server
                errorMessage = error.response.data?.message || 
                             error.response.data?.error || 
                             'Authentication failed';
                             
                if (error.response.status === 401) {
                    errorMessage = 'Invalid username or password';
                }
            }

            toast.error(errorMessage);
            console.error('Login error:', error);

            // Clear password field on error
            setFormData(prev => ({
                ...prev,
                password: ''
            }));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-gradient-primary min-vh-100 d-flex align-items-center">
            <div className="container">
                <div className="row justify-content-center">
                    <div className="col-xl-10 col-lg-12 col-md-9">
                        <div className="card o-hidden border-0 shadow-lg my-5">
                            <div className="card-body p-0">
                                <div className="row">
                                    <div className="col-lg-6 d-none d-lg-block bg-login-image"
                                         style={{
                                             backgroundImage: 'url(/images/delivery-background.jpg)',
                                             backgroundPosition: 'center',
                                             backgroundSize: 'cover'
                                         }}></div>
                                    <div className="col-lg-6">
                                        <div className="p-5">
                                            <div className="text-center">
                                                <h1 className="h4 text-gray-900 mb-4">Welcome Back!</h1>
                                            </div>
                                            <form className="user" onSubmit={handleSubmit}>
                                                <div className="form-group mb-3">
                                                    <input
                                                        type="text"
                                                        className="form-control form-control-user"
                                                        id="username"
                                                        name="username"
                                                        placeholder="Enter Username..."
                                                        value={formData.username}
                                                        onChange={handleChange}
                                                        required
                                                        disabled={loading}
                                                    />
                                                </div>
                                                <div className="form-group mb-3">
                                                    <input
                                                        type="password"
                                                        className="form-control form-control-user"
                                                        id="password"
                                                        name="password"
                                                        placeholder="Password"
                                                        value={formData.password}
                                                        onChange={handleChange}
                                                        required
                                                        disabled={loading}
                                                    />
                                                </div>
                                                <div className="form-group mb-3">
                                                    <div className="custom-control custom-checkbox small">
                                                        <input
                                                            type="checkbox"
                                                            className="custom-control-input"
                                                            id="customCheck"
                                                        />
                                                        <label
                                                            className="custom-control-label"
                                                            htmlFor="customCheck"
                                                        >
                                                            Remember Me
                                                        </label>
                                                    </div>
                                                </div>
                                                <button
                                                    type="submit"
                                                    className="btn btn-primary btn-user btn-block w-100"
                                                    disabled={loading}
                                                >
                                                    {loading ? (
                                                        <span>
                                                            <i className="fas fa-spinner fa-spin me-2"></i>
                                                            Logging in...
                                                        </span>
                                                    ) : (
                                                        'Login'
                                                    )}
                                                </button>
                                            </form>
                                            <hr />
                                            <div className="text-center">
                                                <Link className="small" to="/forgot-password">
                                                    Forgot Password?
                                                </Link>
                                            </div>
                                            <div className="text-center">
                                                <Link className="small" to="/signup">
                                                    Create an Account!
                                                </Link>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login; 