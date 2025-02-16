import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import authService from '../../services/authService';

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

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await authService.login(formData.username, formData.password);
            const user = authService.getCurrentUser();
            
            if (!user || !user.role) {
                throw new Error('Invalid user data received');
            }

            const role = user.role.toLowerCase();
            let redirectPath = '/';

            // Determine redirect path based on role
            switch (role) {
                case 'admin':
                    redirectPath = '/admin/dashboard';
                    break;
                case 'courier':
                    redirectPath = '/courier/dashboard';
                    break;
                case 'customer':
                    redirectPath = '/customer/dashboard';
                    break;
                default:
                    redirectPath = '/';
            }

            toast.success('Login successful!');
            navigate(redirectPath);
        } catch (error) {
            const errorMessage = error.message || 'Failed to login';
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
                                                            <i className="fas fa-spinner fa-spin mr-2"></i>
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