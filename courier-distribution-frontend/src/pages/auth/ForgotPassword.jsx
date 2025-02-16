import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import authService from '../../services/authService';

const ForgotPassword = () => {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [sent, setSent] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            await authService.forgotPassword(email);
            setSent(true);
            toast.success('Password reset instructions have been sent to your email');
        } catch (error) {
            toast.error(error.message || 'Failed to process password reset request');
        } finally {
            setLoading(false);
        }
    };

    if (sent) {
        return (
            <div className="container">
                <div className="row justify-content-center">
                    <div className="col-xl-10 col-lg-12 col-md-9">
                        <div className="card o-hidden border-0 shadow-lg my-5">
                            <div className="card-body p-0">
                                <div className="row">
                                    <div className="col-lg-6 d-none d-lg-block bg-password-image"></div>
                                    <div className="col-lg-6">
                                        <div className="p-5">
                                            <div className="text-center">
                                                <h1 className="h4 text-gray-900 mb-2">Check Your Email</h1>
                                                <p className="mb-4">
                                                    We've sent password reset instructions to your email address.
                                                    Please check your inbox and follow the instructions to reset your password.
                                                </p>
                                            </div>
                                            <div className="text-center">
                                                <Link className="small" to="/login">
                                                    Back to Login
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
    }

    return (
        <div className="container">
            <div className="row justify-content-center">
                <div className="col-xl-10 col-lg-12 col-md-9">
                    <div className="card o-hidden border-0 shadow-lg my-5">
                        <div className="card-body p-0">
                            <div className="row">
                                <div className="col-lg-6 d-none d-lg-block bg-password-image"></div>
                                <div className="col-lg-6">
                                    <div className="p-5">
                                        <div className="text-center">
                                            <h1 className="h4 text-gray-900 mb-2">Forgot Your Password?</h1>
                                            <p className="mb-4">
                                                Enter your email address below and we'll send you instructions
                                                to reset your password.
                                            </p>
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
                                                />
                                            </div>
                                            <button
                                                type="submit"
                                                className="btn btn-primary btn-user btn-block"
                                                disabled={loading}
                                            >
                                                {loading ? (
                                                    <>
                                                        <span className="spinner-border spinner-border-sm me-2" />
                                                        Processing...
                                                    </>
                                                ) : (
                                                    'Reset Password'
                                                )}
                                            </button>
                                        </form>
                                        <hr />
                                        <div className="text-center">
                                            <Link className="small" to="/signup">
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
    );
};

export default ForgotPassword; 