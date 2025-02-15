import React, { useState, useEffect } from 'react';
import apiService from '../../services/apiService';
import { toast } from 'react-toastify';

const Settings = () => {
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [settings, setSettings] = useState({
        notifications: {
            email: true,
            sms: true,
            push: true
        },
        preferences: {
            language: 'en',
            timezone: 'UTC',
            theme: 'light'
        },
        security: {
            twoFactorAuth: false,
            sessionTimeout: 30
        }
    });

    useEffect(() => {
        fetchSettings();
    }, []);

    const fetchSettings = async () => {
        try {
            const response = await apiService.getSettings();
            setSettings(response);
        } catch (error) {
            toast.error('Failed to load settings');
        } finally {
            setLoading(false);
        }
    };

    const handleNotificationChange = (e) => {
        const { name, checked } = e.target;
        setSettings(prev => ({
            ...prev,
            notifications: {
                ...prev.notifications,
                [name]: checked
            }
        }));
    };

    const handlePreferenceChange = (e) => {
        const { name, value } = e.target;
        setSettings(prev => ({
            ...prev,
            preferences: {
                ...prev.preferences,
                [name]: value
            }
        }));
    };

    const handleSecurityChange = (e) => {
        const { name, value, type, checked } = e.target;
        setSettings(prev => ({
            ...prev,
            security: {
                ...prev.security,
                [name]: type === 'checkbox' ? checked : value
            }
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);

        try {
            await apiService.updateSettings(settings);
            toast.success('Settings updated successfully');
        } catch (error) {
            toast.error(error.message || 'Failed to update settings');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="container-fluid">
                <div className="text-center mt-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="sr-only">Loading...</span>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="container-fluid">
            <h1 className="h3 mb-4 text-gray-800">Settings</h1>

            <form onSubmit={handleSubmit}>
                {/* Notification Settings */}
                <div className="card shadow mb-4">
                    <div className="card-header py-3">
                        <h6 className="m-0 font-weight-bold text-primary">Notification Settings</h6>
                    </div>
                    <div className="card-body">
                        <div className="form-group">
                            <div className="custom-control custom-checkbox">
                                <input
                                    type="checkbox"
                                    className="custom-control-input"
                                    id="emailNotifications"
                                    name="email"
                                    checked={settings.notifications.email}
                                    onChange={handleNotificationChange}
                                />
                                <label className="custom-control-label" htmlFor="emailNotifications">
                                    Email Notifications
                                </label>
                            </div>
                        </div>
                        <div className="form-group">
                            <div className="custom-control custom-checkbox">
                                <input
                                    type="checkbox"
                                    className="custom-control-input"
                                    id="smsNotifications"
                                    name="sms"
                                    checked={settings.notifications.sms}
                                    onChange={handleNotificationChange}
                                />
                                <label className="custom-control-label" htmlFor="smsNotifications">
                                    SMS Notifications
                                </label>
                            </div>
                        </div>
                        <div className="form-group">
                            <div className="custom-control custom-checkbox">
                                <input
                                    type="checkbox"
                                    className="custom-control-input"
                                    id="pushNotifications"
                                    name="push"
                                    checked={settings.notifications.push}
                                    onChange={handleNotificationChange}
                                />
                                <label className="custom-control-label" htmlFor="pushNotifications">
                                    Push Notifications
                                </label>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Preferences */}
                <div className="card shadow mb-4">
                    <div className="card-header py-3">
                        <h6 className="m-0 font-weight-bold text-primary">Preferences</h6>
                    </div>
                    <div className="card-body">
                        <div className="form-group">
                            <label htmlFor="language">Language</label>
                            <select
                                className="form-control"
                                id="language"
                                name="language"
                                value={settings.preferences.language}
                                onChange={handlePreferenceChange}
                            >
                                <option value="en">English</option>
                                <option value="es">Spanish</option>
                                <option value="fr">French</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label htmlFor="timezone">Timezone</label>
                            <select
                                className="form-control"
                                id="timezone"
                                name="timezone"
                                value={settings.preferences.timezone}
                                onChange={handlePreferenceChange}
                            >
                                <option value="UTC">UTC</option>
                                <option value="EST">EST</option>
                                <option value="PST">PST</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label htmlFor="theme">Theme</label>
                            <select
                                className="form-control"
                                id="theme"
                                name="theme"
                                value={settings.preferences.theme}
                                onChange={handlePreferenceChange}
                            >
                                <option value="light">Light</option>
                                <option value="dark">Dark</option>
                            </select>
                        </div>
                    </div>
                </div>

                {/* Security Settings */}
                <div className="card shadow mb-4">
                    <div className="card-header py-3">
                        <h6 className="m-0 font-weight-bold text-primary">Security Settings</h6>
                    </div>
                    <div className="card-body">
                        <div className="form-group">
                            <div className="custom-control custom-checkbox">
                                <input
                                    type="checkbox"
                                    className="custom-control-input"
                                    id="twoFactorAuth"
                                    name="twoFactorAuth"
                                    checked={settings.security.twoFactorAuth}
                                    onChange={handleSecurityChange}
                                />
                                <label className="custom-control-label" htmlFor="twoFactorAuth">
                                    Enable Two-Factor Authentication
                                </label>
                            </div>
                        </div>
                        <div className="form-group">
                            <label htmlFor="sessionTimeout">Session Timeout (minutes)</label>
                            <input
                                type="number"
                                className="form-control"
                                id="sessionTimeout"
                                name="sessionTimeout"
                                value={settings.security.sessionTimeout}
                                onChange={handleSecurityChange}
                                min="5"
                                max="120"
                            />
                        </div>
                    </div>
                </div>

                <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? 'Saving...' : 'Save Changes'}
                </button>
            </form>
        </div>
    );
};

export default Settings; 