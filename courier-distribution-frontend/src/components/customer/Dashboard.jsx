import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiService from '../../services/apiService';

const Dashboard = () => {
    const [stats, setStats] = useState({
        totalPackages: 0,
        activeDeliveries: 0,
        deliveredPackages: 0,
        recentPackages: []
    });

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const response = await apiService.getCustomerDashboardStats();
                setStats(response);
            } catch (error) {
                console.error('Error fetching customer dashboard data:', error);
            }
        };

        fetchDashboardData();
    }, []);

    return (
        <div className="container-fluid">
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Customer Dashboard</h1>
                <Link to="/customer/new-package" className="d-none d-sm-inline-block btn btn-sm btn-primary shadow-sm">
                    <i className="fas fa-plus fa-sm text-white-50 mr-2"></i>
                    New Package
                </Link>
            </div>

            {/* Statistics Cards */}
            <div className="row">
                <div className="col-xl-4 col-md-6 mb-4">
                    <div className="card border-left-primary shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                        Total Packages
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.totalPackages}
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-box fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="col-xl-4 col-md-6 mb-4">
                    <div className="card border-left-info shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-info text-uppercase mb-1">
                                        Active Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.activeDeliveries}
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-truck fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="col-xl-4 col-md-6 mb-4">
                    <div className="card border-left-success shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                        Delivered Packages
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.deliveredPackages}
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-check-circle fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Recent Packages */}
            <div className="card shadow mb-4">
                <div className="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 className="m-0 font-weight-bold text-primary">Recent Packages</h6>
                    <Link to="/customer/packages" className="btn btn-sm btn-primary">
                        View All
                    </Link>
                </div>
                <div className="card-body">
                    <div className="table-responsive">
                        <table className="table table-bordered">
                            <thead>
                                <tr>
                                    <th>Package ID</th>
                                    <th>Delivery Address</th>
                                    <th>Status</th>
                                    <th>Created Date</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {stats.recentPackages.map((pkg) => (
                                    <tr key={pkg.id}>
                                        <td>{pkg.packageId}</td>
                                        <td>{pkg.deliveryAddress}</td>
                                        <td>
                                            <span className={`badge badge-${pkg.status.toLowerCase()}`}>
                                                {pkg.status}
                                            </span>
                                        </td>
                                        <td>{new Date(pkg.createdAt).toLocaleDateString()}</td>
                                        <td>
                                            <Link
                                                to={`/customer/tracking/${pkg.packageId}`}
                                                className="btn btn-info btn-sm"
                                            >
                                                Track
                                            </Link>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard; 