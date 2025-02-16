import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const ForgotPassword = () => {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            if (!email.trim()) {
                toast.error('Please enter your email address');
                return;
            }

            const response = await axios.post(`${API_URL}/auth/forgot-password`, { email });
            
            if (response.data.message) {
                toast.success('Password reset instructions have been sent to your email.');
                setEmail('');
            }
        } catch (error) {
            let errorMessage = 'Failed to process password reset request';
            
            if (error.response?.data) {
                errorMessage = error.response.data.message || error.response.data.error;
            }
            
            toast.error(errorMessage);
            console.error('Password reset error:', error);
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
                                    <div className="col-lg-6 d-none d-lg-block bg-password-image"
                                         style={{
                                             backgroundImage: 'url(/images/delivery-forgot.jpg)',
                                             backgroundPosition: 'center',
                                             backgroundSize: 'cover'
                                         }}></div>
                                    <div className="col-lg-6">
                                        <div className="p-5">
                                            <div className="text-center">
                                                <h1 className="h4 text-gray-900 mb-2">Forgot Your Password?</h1>
                                                <p className="mb-4">We understand, things happen. Just enter your email
                                                    address below and we'll send you a link to reset your password!</p>
                                            </div>
                                            <form className="user" onSubmit={handleSubmit}>
                                                <div className="form-group">
                                                    <input
                                                        type="email"
                                                        className="form-control form-control-user"
                                                        id="email"
                                                        placeholder="Enter Email Address..."
                                                        value={email}
                                                        onChange={(e) => setEmail(e.target.value)}
                                                        required
                                                        disabled={loading}
                                                    />
                                                </div>
                                                <button
                                                    type="submit"
                                                    className="btn btn-primary btn-user btn-block"
                                                    disabled={loading}
                                                >
                                                    {loading ? (
                                                        <span>
                                                            <i className="fas fa-spinner fa-spin me-2"></i>
                                                            Processing...
                                                        </span>
                                                    ) : (
                                                        'Reset Password'
                                                    )}
                                                </button>
                                            </form>
                                            <hr />
                                            <div className="text-center">
                                                <Link className="small" to="/register">
                                                    Create an Account!
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
        </div>
    );
};

export default ForgotPassword; 