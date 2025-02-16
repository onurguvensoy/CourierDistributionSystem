import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const NewPackage = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        pickupAddress: '',
        deliveryAddress: '',
        weight: '',
        description: '',
        fragile: false,
        priority: 'NORMAL'
    });

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const token = localStorage.getItem('token');
            await axios.post(`${API_URL}/customer/packages`, formData, {
                headers: { Authorization: `Bearer ${token}` }
            });
            toast.success('Package created successfully');
            navigate('/customer/packages');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to create package');
            console.error('Package creation error:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container-fluid">
            <h1 className="h3 mb-4 text-gray-800">Create New Package</h1>

            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Package Details</h6>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit}>
                        <div className="row">
                            <div className="col-md-6">
                                <div className="form-group mb-3">
                                    <label htmlFor="pickupAddress">Pickup Address</label>
                                    <textarea
                                        className="form-control"
                                        id="pickupAddress"
                                        name="pickupAddress"
                                        value={formData.pickupAddress}
                                        onChange={handleChange}
                                        rows="3"
                                        required
                                    />
                                </div>
                            </div>
                            <div className="col-md-6">
                                <div className="form-group mb-3">
                                    <label htmlFor="deliveryAddress">Delivery Address</label>
                                    <textarea
                                        className="form-control"
                                        id="deliveryAddress"
                                        name="deliveryAddress"
                                        value={formData.deliveryAddress}
                                        onChange={handleChange}
                                        rows="3"
                                        required
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="row">
                            <div className="col-md-6">
                                <div className="form-group mb-3">
                                    <label htmlFor="weight">Weight (kg)</label>
                                    <input
                                        type="number"
                                        className="form-control"
                                        id="weight"
                                        name="weight"
                                        value={formData.weight}
                                        onChange={handleChange}
                                        min="0.1"
                                        step="0.1"
                                        required
                                    />
                                </div>
                            </div>
                            <div className="col-md-6">
                                <div className="form-group mb-3">
                                    <label htmlFor="priority">Priority Level</label>
                                    <select
                                        className="form-control"
                                        id="priority"
                                        name="priority"
                                        value={formData.priority}
                                        onChange={handleChange}
                                        required
                                    >
                                        <option value="NORMAL">Normal</option>
                                        <option value="EXPRESS">Express</option>
                                        <option value="URGENT">Urgent</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div className="form-group mb-3">
                            <label htmlFor="description">Package Description</label>
                            <textarea
                                className="form-control"
                                id="description"
                                name="description"
                                value={formData.description}
                                onChange={handleChange}
                                rows="3"
                            />
                        </div>

                        <div className="form-group mb-3">
                            <div className="form-check">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    id="fragile"
                                    name="fragile"
                                    checked={formData.fragile}
                                    onChange={handleChange}
                                />
                                <label className="form-check-label" htmlFor="fragile">
                                    Fragile Package
                                </label>
                            </div>
                        </div>

                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={loading}
                        >
                            {loading ? (
                                <span>
                                    <i className="fas fa-spinner fa-spin mr-2"></i>
                                    Creating...
                                </span>
                            ) : (
                                'Create Package'
                            )}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default NewPackage; 