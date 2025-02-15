import React, { useState, useEffect, useCallback } from 'react';
import apiService from '../../services/apiService';
import { toast } from 'react-toastify';

const DeliveryHistory = () => {
    const [deliveries, setDeliveries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({
        status: 'ALL',
        dateRange: 'LAST_7_DAYS'
    });

    const fetchDeliveryHistory = useCallback(async () => {
        try {
            const response = await apiService.getCourierDeliveryHistory(filters);
            setDeliveries(response);
        } catch (error) {
            console.error('Error fetching delivery history:', error);
            toast.error('Failed to load delivery history');
        } finally {
            setLoading(false);
        }
    }, [filters]);

    useEffect(() => {
        fetchDeliveryHistory();
    }, [fetchDeliveryHistory]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const getStatusBadgeClass = (status) => {
        const statusClasses = {
            DELIVERED: 'success',
            FAILED: 'danger',
            CANCELLED: 'secondary'
        };
        return `badge badge-${statusClasses[status] || 'info'}`;
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
            <h1 className="h3 mb-4 text-gray-800">Delivery History</h1>

            {/* Filters */}
            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Filters</h6>
                </div>
                <div className="card-body">
                    <div className="row">
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="status">Status</label>
                                <select
                                    className="form-control"
                                    id="status"
                                    name="status"
                                    value={filters.status}
                                    onChange={handleFilterChange}
                                >
                                    <option value="ALL">All</option>
                                    <option value="DELIVERED">Delivered</option>
                                    <option value="FAILED">Failed</option>
                                    <option value="CANCELLED">Cancelled</option>
                                </select>
                            </div>
                        </div>
                        <div className="col-md-6">
                            <div className="form-group">
                                <label htmlFor="dateRange">Date Range</label>
                                <select
                                    className="form-control"
                                    id="dateRange"
                                    name="dateRange"
                                    value={filters.dateRange}
                                    onChange={handleFilterChange}
                                >
                                    <option value="TODAY">Today</option>
                                    <option value="LAST_7_DAYS">Last 7 Days</option>
                                    <option value="LAST_30_DAYS">Last 30 Days</option>
                                    <option value="ALL_TIME">All Time</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Delivery History Table */}
            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Delivery Records</h6>
                </div>
                <div className="card-body">
                    <div className="table-responsive">
                        <table className="table table-bordered">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Package ID</th>
                                    <th>Customer</th>
                                    <th>Delivery Address</th>
                                    <th>Status</th>
                                    <th>Notes</th>
                                </tr>
                            </thead>
                            <tbody>
                                {deliveries.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="text-center">
                                            No delivery history found
                                        </td>
                                    </tr>
                                ) : (
                                    deliveries.map((delivery) => (
                                        <tr key={delivery.id}>
                                            <td>{new Date(delivery.completedAt).toLocaleDateString()}</td>
                                            <td>{delivery.packageId}</td>
                                            <td>{delivery.customerName}</td>
                                            <td>{delivery.deliveryAddress}</td>
                                            <td>
                                                <span className={getStatusBadgeClass(delivery.status)}>
                                                    {delivery.status}
                                                </span>
                                            </td>
                                            <td>{delivery.notes || '-'}</td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DeliveryHistory; 