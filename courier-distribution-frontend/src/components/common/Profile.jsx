import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Profile = () => {
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [profile, setProfile] = useState({
        username: '',
        email: '',
        phoneNumber: '',
        address: '',
        role: '',
        vehicleType: ''
    });

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/user/profile`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setProfile(response.data);
        } catch (error) {
            toast.error('Failed to fetch profile data');
            console.error('Profile fetch error:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);

        try {
            const token = localStorage.getItem('token');
            await axios.put(`${API_URL}/user/profile`, profile, {
                headers: { Authorization: `Bearer ${token}` }
            });
            toast.success('Profile updated successfully');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to update profile');
            console.error('Profile update error:', error);
        } finally {
            setSaving(false);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProfile(prev => ({
            ...prev,
            [name]: value
        }));
    };

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ height: '400px' }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    return (
        <div className="container-fluid">
            <h1 className="h3 mb-4 text-gray-800">Profile</h1>
            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">User Profile</h6>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label htmlFor="username" className="form-label">Username</label>
                            <input
                                type="text"
                                className="form-control"
                                id="username"
                                name="username"
                                value={profile.username}
                                onChange={handleChange}
                                disabled
                            />
                        </div>
                        <div className="mb-3">
                            <label htmlFor="email" className="form-label">Email</label>
                            <input
                                type="email"
                                className="form-control"
                                id="email"
                                name="email"
                                value={profile.email}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="mb-3">
                            <label htmlFor="phoneNumber" className="form-label">Phone Number</label>
                            <input
                                type="tel"
                                className="form-control"
                                id="phoneNumber"
                                name="phoneNumber"
                                value={profile.phoneNumber}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="mb-3">
                            <label htmlFor="address" className="form-label">Address</label>
                            <textarea
                                className="form-control"
                                id="address"
                                name="address"
                                value={profile.address}
                                onChange={handleChange}
                                rows="3"
                            />
                        </div>
                        {profile.role === 'COURIER' && (
                            <div className="mb-3">
                                <label htmlFor="vehicleType" className="form-label">Vehicle Type</label>
                                <select
                                    className="form-control"
                                    id="vehicleType"
                                    name="vehicleType"
                                    value={profile.vehicleType}
                                    onChange={handleChange}
                                    required
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
                            className="btn btn-primary"
                            disabled={saving}
                        >
                            {saving ? (
                                <span>
                                    <i className="fas fa-spinner fa-spin mr-2"></i>
                                    Saving...
                                </span>
                            ) : (
                                'Save Changes'
                            )}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Profile; 