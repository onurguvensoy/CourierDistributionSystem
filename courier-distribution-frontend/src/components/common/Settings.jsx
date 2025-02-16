import React, { useState } from 'react';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Settings = () => {
    const [loading, setLoading] = useState(false);
    const [settings, setSettings] = useState({
        emailNotifications: true,
        smsNotifications: false,
        language: 'en',
        theme: 'light'
    });

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setSettings(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const token = localStorage.getItem('token');
            await axios.put(`${API_URL}/user/settings`, settings, {
                headers: { Authorization: `Bearer ${token}` }
            });
            toast.success('Settings updated successfully');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to update settings');
            console.error('Settings update error:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container-fluid">
            <h1 className="h3 mb-4 text-gray-800">Settings</h1>
            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">User Settings</h6>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <div className="form-check">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    id="emailNotifications"
                                    name="emailNotifications"
                                    checked={settings.emailNotifications}
                                    onChange={handleChange}
                                />
                                <label className="form-check-label" htmlFor="emailNotifications">
                                    Email Notifications
                                </label>
                            </div>
                        </div>
                        <div className="mb-3">
                            <div className="form-check">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    id="smsNotifications"
                                    name="smsNotifications"
                                    checked={settings.smsNotifications}
                                    onChange={handleChange}
                                />
                                <label className="form-check-label" htmlFor="smsNotifications">
                                    SMS Notifications
                                </label>
                            </div>
                        </div>
                        <div className="mb-3">
                            <label htmlFor="language" className="form-label">Language</label>
                            <select
                                className="form-control"
                                id="language"
                                name="language"
                                value={settings.language}
                                onChange={handleChange}
                            >
                                <option value="en">English</option>
                                <option value="tr">Turkish</option>
                            </select>
                        </div>
                        <div className="mb-3">
                            <label htmlFor="theme" className="form-label">Theme</label>
                            <select
                                className="form-control"
                                id="theme"
                                name="theme"
                                value={settings.theme}
                                onChange={handleChange}
                            >
                                <option value="light">Light</option>
                                <option value="dark">Dark</option>
                            </select>
                        </div>
                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={loading}
                        >
                            {loading ? (
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

export default Settings; 