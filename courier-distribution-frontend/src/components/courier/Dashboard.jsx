import React, { useState, useEffect } from 'react';
import apiService from '../../services/apiService';

const Dashboard = () => {
    const [stats, setStats] = useState({
        activeDeliveries: 0,
        completedDeliveries: 0,
        pendingDeliveries: 0,
        todaysDeliveries: []
    });

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const response = await apiService.getCourierDeliveries();
                setStats(response);
            } catch (error) {
                console.error('Error fetching courier dashboard data:', error);
            }
        };

        fetchDashboardData();
    }, []);

    const handleStatusUpdate = async (packageId, status) => {
        try {
            await apiService.updateDeliveryStatus(packageId, status);
            // Refresh dashboard data
            const response = await apiService.getCourierDeliveries();
            setStats(response);
        } catch (error) {
            console.error('Error updating delivery status:', error);
        }
    };

    return (
        <div className="container-fluid">
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Courier Dashboard</h1>
            </div>

            {/* Statistics Cards */}
            <div className="row">
                <div className="col-xl-4 col-md-6 mb-4">
                    <div className="card border-left-primary shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
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
                                        Completed Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.completedDeliveries}
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-check-circle fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="col-xl-4 col-md-6 mb-4">
                    <div className="card border-left-warning shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                        Pending Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.pendingDeliveries}
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-clock fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Today's Deliveries */}
            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Today's Deliveries</h6>
                </div>
                <div className="card-body">
                    <div className="table-responsive">
                        <table className="table table-bordered">
                            <thead>
                                <tr>
                                    <th>Package ID</th>
                                    <th>Delivery Address</th>
                                    <th>Customer Name</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {stats.todaysDeliveries.map((delivery) => (
                                    <tr key={delivery.id}>
                                        <td>{delivery.packageId}</td>
                                        <td>{delivery.deliveryAddress}</td>
                                        <td>{delivery.customerName}</td>
                                        <td>
                                            <span className={`badge badge-${delivery.status.toLowerCase()}`}>
                                                {delivery.status}
                                            </span>
                                        </td>
                                        <td>
                                            <div className="btn-group">
                                                <button
                                                    className="btn btn-success btn-sm"
                                                    onClick={() => handleStatusUpdate(delivery.packageId, 'DELIVERED')}
                                                >
                                                    Mark Delivered
                                                </button>
                                                <button
                                                    className="btn btn-danger btn-sm"
                                                    onClick={() => handleStatusUpdate(delivery.packageId, 'FAILED')}
                                                >
                                                    Mark Failed
                                                </button>
                                            </div>
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