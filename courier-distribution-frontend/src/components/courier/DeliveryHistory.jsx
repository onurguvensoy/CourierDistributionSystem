import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const DeliveryHistory = () => {
    const [loading, setLoading] = useState(true);
    const [deliveries, setDeliveries] = useState([]);
    const [filters, setFilters] = useState({
        status: '',
        startDate: '',
        endDate: ''
    });

    useEffect(() => {
        fetchDeliveryHistory();
    }, [filters]);

    const fetchDeliveryHistory = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/courier/delivery-history`, {
                headers: { Authorization: `Bearer ${token}` },
                params: filters
            });
            setDeliveries(response.data);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to fetch delivery history');
            console.error('Delivery history error:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({
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
            <h1 className="h3 mb-4 text-gray-800">Delivery History</h1>

            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Filters</h6>
                </div>
                <div className="card-body">
                    <div className="row">
                        <div className="col-md-4">
                            <div className="form-group">
                                <label htmlFor="status">Status</label>
                                <select
                                    className="form-control"
                                    id="status"
                                    name="status"
                                    value={filters.status}
                                    onChange={handleFilterChange}
                                >
                                    <option value="">All</option>
                                    <option value="DELIVERED">Delivered</option>
                                    <option value="CANCELLED">Cancelled</option>
                                </select>
                            </div>
                        </div>
                        <div className="col-md-4">
                            <div className="form-group">
                                <label htmlFor="startDate">Start Date</label>
                                <input
                                    type="date"
                                    className="form-control"
                                    id="startDate"
                                    name="startDate"
                                    value={filters.startDate}
                                    onChange={handleFilterChange}
                                />
                            </div>
                        </div>
                        <div className="col-md-4">
                            <div className="form-group">
                                <label htmlFor="endDate">End Date</label>
                                <input
                                    type="date"
                                    className="form-control"
                                    id="endDate"
                                    name="endDate"
                                    value={filters.endDate}
                                    onChange={handleFilterChange}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Delivery History</h6>
                </div>
                <div className="card-body">
                    {deliveries.length === 0 ? (
                        <div className="text-center py-4">
                            <p className="text-gray-500 mb-0">No delivery history found.</p>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Customer</th>
                                        <th>Pickup Address</th>
                                        <th>Delivery Address</th>
                                        <th>Status</th>
                                        <th>Completed At</th>
                                        <th>Rating</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {deliveries.map((delivery) => (
                                        <tr key={delivery.id}>
                                            <td>{delivery.id}</td>
                                            <td>{delivery.customerName}</td>
                                            <td>{delivery.pickupAddress}</td>
                                            <td>{delivery.deliveryAddress}</td>
                                            <td>
                                                <span className={`badge bg-${getStatusColor(delivery.status)}`}>
                                                    {delivery.status}
                                                </span>
                                            </td>
                                            <td>{new Date(delivery.completedAt).toLocaleString()}</td>
                                            <td>
                                                {delivery.rating ? (
                                                    <div className="text-warning">
                                                        {Array.from({ length: delivery.rating }, (_, i) => (
                                                            <i key={i} className="fas fa-star"></i>
                                                        ))}
                                                        {Array.from({ length: 5 - delivery.rating }, (_, i) => (
                                                            <i key={i} className="far fa-star"></i>
                                                        ))}
                                                    </div>
                                                ) : (
                                                    <span className="text-muted">Not rated</span>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

const getStatusColor = (status) => {
    switch (status) {
        case 'DELIVERED':
            return 'success';
        case 'CANCELLED':
            return 'danger';
        default:
            return 'secondary';
    }
};

export default DeliveryHistory; 