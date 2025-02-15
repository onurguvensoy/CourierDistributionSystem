import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../../services/apiService';
import { toast } from 'react-toastify';

const NewPackage = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        description: '',
        weight: '',
        dimensions: {
            length: '',
            width: '',
            height: ''
        },
        deliveryAddress: '',
        recipientName: '',
        recipientPhone: '',
        specialInstructions: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        if (name.includes('.')) {
            const [parent, child] = name.split('.');
            setFormData((prev) => ({
                ...prev,
                [parent]: {
                    ...prev[parent],
                    [child]: value
                }
            }));
        } else {
            setFormData((prev) => ({
                ...prev,
                [name]: value
            }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await apiService.createPackage(formData);
            toast.success('Package created successfully!');
            navigate(`/customer/tracking/${response.packageId}`);
        } catch (error) {
            toast.error(error.message || 'Failed to create package');
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
                                <div className="form-group">
                                    <label htmlFor="description">Package Description</label>
                                    <textarea
                                        className="form-control"
                                        id="description"
                                        name="description"
                                        rows="3"
                                        value={formData.description}
                                        onChange={handleChange}
                                        required
                                    ></textarea>
                                </div>
                            </div>
                            <div className="col-md-6">
                                <div className="form-group">
                                    <label htmlFor="weight">Weight (kg)</label>
                                    <input
                                        type="number"
                                        className="form-control"
                                        id="weight"
                                        name="weight"
                                        value={formData.weight}
                                        onChange={handleChange}
                                        required
                                        step="0.1"
                                        min="0"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="row">
                            <div className="col-md-4">
                                <div className="form-group">
                                    <label htmlFor="dimensions.length">Length (cm)</label>
                                    <input
                                        type="number"
                                        className="form-control"
                                        id="dimensions.length"
                                        name="dimensions.length"
                                        value={formData.dimensions.length}
                                        onChange={handleChange}
                                        required
                                        min="0"
                                    />
                                </div>
                            </div>
                            <div className="col-md-4">
                                <div className="form-group">
                                    <label htmlFor="dimensions.width">Width (cm)</label>
                                    <input
                                        type="number"
                                        className="form-control"
                                        id="dimensions.width"
                                        name="dimensions.width"
                                        value={formData.dimensions.width}
                                        onChange={handleChange}
                                        required
                                        min="0"
                                    />
                                </div>
                            </div>
                            <div className="col-md-4">
                                <div className="form-group">
                                    <label htmlFor="dimensions.height">Height (cm)</label>
                                    <input
                                        type="number"
                                        className="form-control"
                                        id="dimensions.height"
                                        name="dimensions.height"
                                        value={formData.dimensions.height}
                                        onChange={handleChange}
                                        required
                                        min="0"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="row">
                            <div className="col-md-12">
                                <div className="form-group">
                                    <label htmlFor="deliveryAddress">Delivery Address</label>
                                    <textarea
                                        className="form-control"
                                        id="deliveryAddress"
                                        name="deliveryAddress"
                                        rows="3"
                                        value={formData.deliveryAddress}
                                        onChange={handleChange}
                                        required
                                    ></textarea>
                                </div>
                            </div>
                        </div>

                        <div className="row">
                            <div className="col-md-6">
                                <div className="form-group">
                                    <label htmlFor="recipientName">Recipient Name</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        id="recipientName"
                                        name="recipientName"
                                        value={formData.recipientName}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>
                            <div className="col-md-6">
                                <div className="form-group">
                                    <label htmlFor="recipientPhone">Recipient Phone</label>
                                    <input
                                        type="tel"
                                        className="form-control"
                                        id="recipientPhone"
                                        name="recipientPhone"
                                        value={formData.recipientPhone}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="form-group">
                            <label htmlFor="specialInstructions">Special Instructions</label>
                            <textarea
                                className="form-control"
                                id="specialInstructions"
                                name="specialInstructions"
                                rows="3"
                                value={formData.specialInstructions}
                                onChange={handleChange}
                            ></textarea>
                        </div>

                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Creating...' : 'Create Package'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default NewPackage; 