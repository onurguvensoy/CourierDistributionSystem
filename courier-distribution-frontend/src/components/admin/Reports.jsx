import React, { useState, useEffect } from 'react';
import apiService from '../../services/apiService';

const Reports = () => {
    const [reports, setReports] = useState({
        deliveryStats: {
            totalDeliveries: 0,
            completedDeliveries: 0,
            pendingDeliveries: 0,
            failedDeliveries: 0
        },
        courierPerformance: [],
        revenueData: {
            daily: [],
            monthly: [],
            yearly: []
        }
    });

    useEffect(() => {
        const fetchReports = async () => {
            try {
                const response = await apiService.getAdminReports();
                setReports(response);
            } catch (error) {
                console.error('Error fetching reports:', error);
            }
        };

        fetchReports();
    }, []);

    return (
        <div className="container-fluid">
            <h1 className="h3 mb-4 text-gray-800">Reports</h1>

            {/* Delivery Statistics */}
            <div className="row">
                <div className="col-xl-12 col-lg-12">
                    <div className="card shadow mb-4">
                        <div className="card-header py-3">
                            <h6 className="m-0 font-weight-bold text-primary">Delivery Statistics</h6>
                        </div>
                        <div className="card-body">
                            <div className="row">
                                <div className="col-md-3 mb-4">
                                    <div className="card border-left-primary shadow h-100 py-2">
                                        <div className="card-body">
                                            <div className="row no-gutters align-items-center">
                                                <div className="col mr-2">
                                                    <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                                        Total Deliveries
                                                    </div>
                                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                                        {reports.deliveryStats.totalDeliveries}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div className="col-md-3 mb-4">
                                    <div className="card border-left-success shadow h-100 py-2">
                                        <div className="card-body">
                                            <div className="row no-gutters align-items-center">
                                                <div className="col mr-2">
                                                    <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                                        Completed
                                                    </div>
                                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                                        {reports.deliveryStats.completedDeliveries}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div className="col-md-3 mb-4">
                                    <div className="card border-left-info shadow h-100 py-2">
                                        <div className="card-body">
                                            <div className="row no-gutters align-items-center">
                                                <div className="col mr-2">
                                                    <div className="text-xs font-weight-bold text-info text-uppercase mb-1">
                                                        Pending
                                                    </div>
                                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                                        {reports.deliveryStats.pendingDeliveries}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div className="col-md-3 mb-4">
                                    <div className="card border-left-danger shadow h-100 py-2">
                                        <div className="card-body">
                                            <div className="row no-gutters align-items-center">
                                                <div className="col mr-2">
                                                    <div className="text-xs font-weight-bold text-danger text-uppercase mb-1">
                                                        Failed
                                                    </div>
                                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                                        {reports.deliveryStats.failedDeliveries}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Courier Performance */}
            <div className="row">
                <div className="col-xl-12 col-lg-12">
                    <div className="card shadow mb-4">
                        <div className="card-header py-3">
                            <h6 className="m-0 font-weight-bold text-primary">Courier Performance</h6>
                        </div>
                        <div className="card-body">
                            <div className="table-responsive">
                                <table className="table table-bordered">
                                    <thead>
                                        <tr>
                                            <th>Courier Name</th>
                                            <th>Total Deliveries</th>
                                            <th>Completed</th>
                                            <th>Failed</th>
                                            <th>Success Rate</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {reports.courierPerformance.map((courier, index) => (
                                            <tr key={index}>
                                                <td>{courier.name}</td>
                                                <td>{courier.totalDeliveries}</td>
                                                <td>{courier.completedDeliveries}</td>
                                                <td>{courier.failedDeliveries}</td>
                                                <td>{courier.successRate}%</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Reports; 