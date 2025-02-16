import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Signup = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        role: 'CUSTOMER',
        phoneNumber: '',
        vehicleType: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const validateForm = () => {
        // Username validation (3-50 characters)
        if (!formData.username || formData.username.length < 3 || formData.username.length > 50) {
            toast.error('Username must be between 3 and 50 characters');
            return false;
        }

        // Password validation (min 6 characters)
        if (!formData.password || formData.password.length < 6) {
            toast.error('Password must be at least 6 characters');
            return false;
        }

        // Password match validation
        if (formData.password !== formData.confirmPassword) {
            toast.error('Passwords do not match');
            return false;
        }

        // Email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!formData.email || !emailRegex.test(formData.email)) {
            toast.error('Please enter a valid email address');
            return false;
        }

        // Phone number validation (international format)
        const phoneRegex = /^\+?[1-9]\d{1,14}$/;
        if (!formData.phoneNumber || !phoneRegex.test(formData.phoneNumber)) {
            toast.error('Please enter a valid phone number (e.g., +1234567890)');
            return false;
        }

        // Vehicle type validation for couriers
        if (formData.role === 'COURIER') {
            if (!['MOTORCYCLE', 'CAR', 'VAN'].includes(formData.vehicleType)) {
                toast.error('Please select a valid vehicle type');
                return false;
            }
        }

        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }

        setLoading(true);

        try {
            // Prepare signup data
            const { confirmPassword, ...signupData } = formData;

            // Only include vehicleType if role is COURIER
            const finalSignupData = {
                ...signupData,
                vehicleType: signupData.role === 'COURIER' ? signupData.vehicleType : undefined
            };

            console.log('Signup request data:', finalSignupData); // Debug log

            // Make signup request
            const signupResponse = await axios.post(`${API_URL}/auth/signup`, finalSignupData);

            if (signupResponse.data.message) {
                // If signup is successful, attempt automatic login
                try {
                    const loginResponse = await axios.post(`${API_URL}/auth/login`, {
                        username: formData.username,
                        password: formData.password
                    });

                    const { token, username, role, userId } = loginResponse.data;

                    // Store auth data
                    const userData = {
                        username,
                        role,
                        userId,
                        lastLogin: new Date().toISOString()
                    };

                    localStorage.setItem('token', token);
                    localStorage.setItem('user', JSON.stringify(userData));

                    // Set up axios defaults
                    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

                    // Show success message
                    toast.success('Account created and logged in successfully!');

                    // Redirect based on role
                    navigate(`/${role.toLowerCase()}/dashboard`);
                } catch (loginError) {
                    console.error('Auto-login error:', loginError.response?.data || loginError);
                    // If auto-login fails, just redirect to login page
                    toast.success('Account created successfully! Please login.');
                    navigate('/login');
                }
            }
        } catch (error) {
            console.error('Signup error details:', {
                response: error.response?.data,
                status: error.response?.status,
                data: formData
            });

            let errorMessage = 'Registration failed';
            
            if (error.response?.data) {
                // Handle specific error messages from the backend
                if (error.response.data.error === 'USER_EXISTS') {
                    errorMessage = 'Username or email already exists';
                } else if (error.response.data.error === 'INVALID_DATA') {
                    errorMessage = error.response.data.message || 'Invalid registration data';
                } else {
                    errorMessage = error.response.data.message || error.response.data.error;
                }

                // Log validation errors if present
                if (error.response.data.errors) {
                    console.error('Validation errors:', error.response.data.errors);
                    errorMessage = Object.values(error.response.data.errors).join(', ');
                }
            }
            
            toast.error(errorMessage);

            // Clear password fields on error
            setFormData(prev => ({
                ...prev,
                password: '',
                confirmPassword: ''
            }));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-gradient-primary min-vh-100 d-flex align-items-center">
            <div className="container">
                <div className="card o-hidden border-0 shadow-lg my-5">
                    <div className="card-body p-0">
                        <div className="row">
                            <div className="col-lg-5 d-none d-lg-block bg-register-image"
                                 style={{
                                     backgroundImage: 'url(/images/delivery-register.jpg)',
                                     backgroundPosition: 'center',
                                     backgroundSize: 'cover'
                                 }}></div>
                            <div className="col-lg-7">
                                <div className="p-5">
                                    <div className="text-center">
                                        <h1 className="h4 text-gray-900 mb-4">Create an Account!</h1>
                                    </div>
                                    <form className="user" onSubmit={handleSubmit}>
                                        <div className="form-group row">
                                            <div className="col-sm-6 mb-3 mb-sm-0">
                                                <input
                                                    type="text"
                                                    className="form-control form-control-user"
                                                    id="username"
                                                    name="username"
                                                    placeholder="Username (3-50 characters)"
                                                    value={formData.username}
                                                    onChange={handleChange}
                                                    required
                                                    minLength="3"
                                                    maxLength="50"
                                                    disabled={loading}
                                                />
                                            </div>
                                            <div className="col-sm-6">
                                                <input
                                                    type="email"
                                                    className="form-control form-control-user"
                                                    id="email"
                                                    name="email"
                                                    placeholder="Email Address"
                                                    value={formData.email}
                                                    onChange={handleChange}
                                                    required
                                                    disabled={loading}
                                                />
                                            </div>
                                        </div>
                                        <div className="form-group row">
                                            <div className="col-sm-6 mb-3 mb-sm-0">
                                                <input
                                                    type="password"
                                                    className="form-control form-control-user"
                                                    id="password"
                                                    name="password"
                                                    placeholder="Password (min 6 characters)"
                                                    value={formData.password}
                                                    onChange={handleChange}
                                                    required
                                                    minLength="6"
                                                    disabled={loading}
                                                />
                                            </div>
                                            <div className="col-sm-6">
                                                <input
                                                    type="password"
                                                    className="form-control form-control-user"
                                                    id="confirmPassword"
                                                    name="confirmPassword"
                                                    placeholder="Repeat Password"
                                                    value={formData.confirmPassword}
                                                    onChange={handleChange}
                                                    required
                                                    disabled={loading}
                                                />
                                            </div>
                                        </div>
                                        <div className="form-group row">
                                            <div className="col-sm-6 mb-3 mb-sm-0">
                                                <select
                                                    className="form-control form-control-user"
                                                    id="role"
                                                    name="role"
                                                    value={formData.role}
                                                    onChange={handleChange}
                                                    disabled={loading}
                                                >
                                                    <option value="CUSTOMER">Customer</option>
                                                    <option value="COURIER">Courier</option>
                                                </select>
                                            </div>
                                            <div className="col-sm-6">
                                                <input
                                                    type="tel"
                                                    className="form-control form-control-user"
                                                    id="phoneNumber"
                                                    name="phoneNumber"
                                                    placeholder="Phone Number (+1234567890)"
                                                    value={formData.phoneNumber}
                                                    onChange={handleChange}
                                                    required
                                                    pattern="^\+?[1-9]\d{1,14}$"
                                                    disabled={loading}
                                                />
                                            </div>
                                        </div>
                                        {formData.role === 'COURIER' && (
                                            <div className="form-group">
                                                <select
                                                    className="form-control form-control-user"
                                                    id="vehicleType"
                                                    name="vehicleType"
                                                    value={formData.vehicleType}
                                                    onChange={handleChange}
                                                    required
                                                    disabled={loading}
                                                >
                                                    <option value="">Select Vehicle Type</option>
                                                    <option value="MOTORCYCLE">Motorcycle</option>
                                                    <option value="CAR">Car</option>
                                                    <option value="VAN">Van</option>
                                                </select>
                                            </div>
                                        )}
                                        <button
                                            type="submit"
                                            className="btn btn-primary btn-user btn-block"
                                            disabled={loading}
                                        >
                                            {loading ? (
                                                <span>
                                                    <i className="fas fa-spinner fa-spin me-2"></i>
                                                    Registering...
                                                </span>
                                            ) : (
                                                'Register Account'
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
                                        <Link className="small" to="/login">
                                            Already have an account? Login!
                                        </Link>
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

export default Signup; 