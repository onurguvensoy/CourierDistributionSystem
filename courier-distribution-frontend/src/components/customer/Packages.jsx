import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Packages = () => {
    const [loading, setLoading] = useState(true);
    const [packages, setPackages] = useState([]);
    const [filters, setFilters] = useState({
        status: '',
        startDate: '',
        endDate: ''
    });

    useEffect(() => {
        fetchPackages();
    }, [filters]);

    const fetchPackages = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/customer/packages`, {
                headers: { Authorization: `Bearer ${token}` },
                params: filters
            });
            setPackages(response.data);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to fetch packages');
            console.error('Package fetch error:', error);
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

    const cancelPackage = async (packageId) => {
        if (window.confirm('Are you sure you want to cancel this package?')) {
            try {
                const token = localStorage.getItem('token');
                await axios.post(
                    `${API_URL}/customer/packages/${packageId}/cancel`,
                    {},
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                toast.success('Package cancelled successfully');
                fetchPackages();
            } catch (error) {
                toast.error(error.response?.data?.message || 'Failed to cancel package');
                console.error('Package cancellation error:', error);
            }
        }
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
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">My Packages</h1>
                <Link to="/customer/new-package" className="d-none d-sm-inline-block btn btn-primary shadow-sm">
                    <i className="fas fa-plus fa-sm text-white-50 mr-2"></i>
                    New Package
                </Link>
            </div>

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
                                    <option value="PENDING">Pending</option>
                                    <option value="ASSIGNED">Assigned</option>
                                    <option value="PICKED_UP">Picked Up</option>
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
                    <h6 className="m-0 font-weight-bold text-primary">Package List</h6>
                </div>
                <div className="card-body">
                    {packages.length === 0 ? (
                        <div className="text-center py-4">
                            <p className="text-gray-500 mb-0">No packages found.</p>
                            <Link to="/customer/new-package" className="btn btn-primary mt-3">
                                Create Your First Package
                            </Link>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Pickup Address</th>
                                        <th>Delivery Address</th>
                                        <th>Status</th>
                                        <th>Courier</th>
                                        <th>Created At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {packages.map((pkg) => (
                                        <tr key={pkg.id}>
                                            <td>{pkg.id}</td>
                                            <td>{pkg.pickupAddress}</td>
                                            <td>{pkg.deliveryAddress}</td>
                                            <td>
                                                <span className={`badge bg-${getStatusColor(pkg.status)}`}>
                                                    {pkg.status}
                                                </span>
                                            </td>
                                            <td>{pkg.courierName || 'Not assigned'}</td>
                                            <td>{new Date(pkg.createdAt).toLocaleString()}</td>
                                            <td>
                                                {pkg.status === 'PENDING' && (
                                                    <button
                                                        className="btn btn-sm btn-danger"
                                                        onClick={() => cancelPackage(pkg.id)}
                                                    >
                                                        Cancel
                                                    </button>
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
        case 'PENDING':
            return 'secondary';
        case 'ASSIGNED':
            return 'warning';
        case 'PICKED_UP':
            return 'info';
        case 'DELIVERED':
            return 'success';
        case 'CANCELLED':
            return 'danger';
        default:
            return 'secondary';
    }
};

export default Packages; 