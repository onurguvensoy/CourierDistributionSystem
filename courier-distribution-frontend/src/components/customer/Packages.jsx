import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import apiService from '../../services/apiService';
import { toast } from 'react-toastify';

const Packages = () => {
    const [packages, setPackages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({
        status: 'ALL',
        dateRange: 'ALL'
    });

    const fetchPackages = useCallback(async () => {
        try {
            const response = await apiService.getCustomerPackages(filters);
            setPackages(response);
        } catch (error) {
            console.error('Error fetching packages:', error);
            toast.error('Failed to load packages');
        } finally {
            setLoading(false);
        }
    }, [filters]);

    useEffect(() => {
        fetchPackages();
    }, [fetchPackages]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleCancelPackage = async (packageId) => {
        try {
            await apiService.cancelPackage(packageId);
            toast.success('Package cancelled successfully');
            fetchPackages();
        } catch (error) {
            toast.error(error.message || 'Failed to cancel package');
        }
    };

    const getStatusBadgeClass = (status) => {
        const statusClasses = {
            PENDING: 'warning',
            IN_TRANSIT: 'info',
            DELIVERED: 'success',
            FAILED: 'danger',
            CANCELLED: 'secondary'
        };
        return `badge badge-${statusClasses[status] || 'primary'}`;
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
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">My Packages</h1>
                <Link to="/customer/new-package" className="d-none d-sm-inline-block btn btn-sm btn-primary shadow-sm">
                    <i className="fas fa-plus fa-sm text-white-50 mr-2"></i>
                    New Package
                </Link>
            </div>

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
                                    <option value="PENDING">Pending</option>
                                    <option value="IN_TRANSIT">In Transit</option>
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
                                    <option value="ALL">All Time</option>
                                    <option value="TODAY">Today</option>
                                    <option value="LAST_7_DAYS">Last 7 Days</option>
                                    <option value="LAST_30_DAYS">Last 30 Days</option>
                                    <option value="LAST_90_DAYS">Last 90 Days</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Packages Table */}
            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Package List</h6>
                </div>
                <div className="card-body">
                    <div className="table-responsive">
                        <table className="table table-bordered">
                            <thead>
                                <tr>
                                    <th>Package ID</th>
                                    <th>Description</th>
                                    <th>Delivery Address</th>
                                    <th>Created Date</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {packages.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="text-center">
                                            No packages found
                                        </td>
                                    </tr>
                                ) : (
                                    packages.map((pkg) => (
                                        <tr key={pkg.id}>
                                            <td>{pkg.packageId}</td>
                                            <td>{pkg.description}</td>
                                            <td>{pkg.deliveryAddress}</td>
                                            <td>{new Date(pkg.createdAt).toLocaleDateString()}</td>
                                            <td>
                                                <span className={getStatusBadgeClass(pkg.status)}>
                                                    {pkg.status}
                                                </span>
                                            </td>
                                            <td>
                                                <Link
                                                    to={`/customer/tracking/${pkg.packageId}`}
                                                    className="btn btn-info btn-sm mr-2"
                                                >
                                                    Track
                                                </Link>
                                                {pkg.status === 'PENDING' && (
                                                    <button
                                                        className="btn btn-danger btn-sm"
                                                        onClick={() => handleCancelPackage(pkg.packageId)}
                                                    >
                                                        Cancel
                                                    </button>
                                                )}
                                            </td>
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

export default Packages; 