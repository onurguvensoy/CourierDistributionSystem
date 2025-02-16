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
        address: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        if (formData.password !== formData.confirmPassword) {
            toast.error('Passwords do not match');
            setLoading(false);
            return;
        }

        try {
            const { confirmPassword, ...signupData } = formData;
            const response = await axios.post(`${API_URL}/auth/register`, signupData);
            
            const { token, username, role, userId } = response.data;
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify({ username, role, userId }));
            
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            
            toast.success('Registration successful!');
            navigate(`/${role.toLowerCase()}/dashboard`);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Registration failed');
            console.error('Registration error:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <div className="row justify-content-center">
                <div className="col-xl-10 col-lg-12 col-md-9">
                    <div className="card o-hidden border-0 shadow-lg my-5">
                        <div className="card-body p-0">
                            <div className="row">
                                <div className="col-lg-5 d-none d-lg-block bg-register-image"></div>
                                <div className="col-lg-7">
                                    <div className="p-5">
                                        <div className="text-center">
                                            <h1 className="h4 text-gray-900 mb-4">Create an Account!</h1>
                                        </div>
                                        <form className="user" onSubmit={handleSubmit}>
                                            <div className="form-group">
                                                <input
                                                    type="text"
                                                    className="form-control form-control-user"
                                                    placeholder="Username"
                                                    name="username"
                                                    value={formData.username}
                                                    onChange={handleChange}
                                                    required
                                                />
                                            </div>
                                            <div className="form-group">
                                                <input
                                                    type="email"
                                                    className="form-control form-control-user"
                                                    placeholder="Email Address"
                                                    name="email"
                                                    value={formData.email}
                                                    onChange={handleChange}
                                                    required
                                                />
                                            </div>
                                            <div className="form-group row">
                                                <div className="col-sm-6 mb-3 mb-sm-0">
                                                    <input
                                                        type="password"
                                                        className="form-control form-control-user"
                                                        placeholder="Password"
                                                        name="password"
                                                        value={formData.password}
                                                        onChange={handleChange}
                                                        required
                                                    />
                                                </div>
                                                <div className="col-sm-6">
                                                    <input
                                                        type="password"
                                                        className="form-control form-control-user"
                                                        placeholder="Confirm Password"
                                                        name="confirmPassword"
                                                        value={formData.confirmPassword}
                                                        onChange={handleChange}
                                                        required
                                                    />
                                                </div>
                                            </div>
                                            <div className="form-group">
                                                <input
                                                    type="tel"
                                                    className="form-control form-control-user"
                                                    placeholder="Phone Number"
                                                    name="phoneNumber"
                                                    value={formData.phoneNumber}
                                                    onChange={handleChange}
                                                    required
                                                />
                                            </div>
                                            <div className="form-group">
                                                <textarea
                                                    className="form-control"
                                                    placeholder="Address"
                                                    name="address"
                                                    value={formData.address}
                                                    onChange={handleChange}
                                                    required
                                                    rows="3"
                                                />
                                            </div>
                                            <div className="form-group">
                                                <select
                                                    className="form-control"
                                                    name="role"
                                                    value={formData.role}
                                                    onChange={handleChange}
                                                    required
                                                >
                                                    <option value="CUSTOMER">Customer</option>
                                                    <option value="COURIER">Courier</option>
                                                </select>
                                            </div>
                                            <button
                                                type="submit"
                                                className="btn btn-primary btn-user btn-block"
                                                disabled={loading}
                                            >
                                                {loading ? (
                                                    <span>
                                                        <i className="fas fa-spinner fa-spin mr-2"></i>
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
        </div>
    );
};

export default Signup; 