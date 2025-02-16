import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
const WS_URL = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';

const CustomerDashboard = () => {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalPackages: 0,
        activeDeliveries: 0,
        completedDeliveries: 0,
        totalSpent: 0
    });
    const [recentDeliveries, setRecentDeliveries] = useState([]);
    const [stompClient, setStompClient] = useState(null);

    useEffect(() => {
        fetchDashboardData();
        setupWebSocket();

        return () => {
            if (stompClient) {
                stompClient.disconnect();
            }
        };
    }, []);

    const setupWebSocket = () => {
        const socket = new SockJS(WS_URL);
        const client = Stomp.over(socket);
        const token = localStorage.getItem('token');
        const user = JSON.parse(localStorage.getItem('user'));

        client.connect(
            { Authorization: `Bearer ${token}` },
            () => {
                client.subscribe(`/topic/customer/${user.userId}`, (message) => {
                    const delivery = JSON.parse(message.body);
                    handleDeliveryUpdate(delivery);
                });
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                toast.error('Failed to connect to real-time updates');
            }
        );

        setStompClient(client);
    };

    const handleDeliveryUpdate = (delivery) => {
        setRecentDeliveries(prev => {
            const index = prev.findIndex(d => d.id === delivery.id);
            if (index === -1) {
                return [delivery, ...prev].slice(0, 5);
            }
            const updated = [...prev];
            updated[index] = delivery;
            return updated;
        });
        fetchDashboardData();
    };

    const fetchDashboardData = async () => {
        try {
            const token = localStorage.getItem('token');
            const [statsResponse, deliveriesResponse] = await Promise.all([
                axios.get(`${API_URL}/customer/stats`, {
                    headers: { Authorization: `Bearer ${token}` }
                }),
                axios.get(`${API_URL}/customer/recent-deliveries`, {
                    headers: { Authorization: `Bearer ${token}` }
                })
            ]);

            setStats(statsResponse.data);
            setRecentDeliveries(deliveriesResponse.data);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to fetch dashboard data');
            console.error('Dashboard data error:', error);
        } finally {
            setLoading(false);
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
        <>
            {/* Page Heading */}
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Dashboard</h1>
                <Link to="/customer/new-package" className="d-none d-sm-inline-block btn btn-primary shadow-sm">
                    <i className="fas fa-plus fa-sm text-white-50"></i> New Package
                </Link>
            </div>

            {/* Content Row */}
            <div className="row">
                {/* Total Packages Card */}
                <div className="col-xl-3 col-md-6 mb-4">
                    <div className="card border-left-primary shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                        Total Packages
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">{stats.totalPackages}</div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-box fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Active Deliveries Card */}
                <div className="col-xl-3 col-md-6 mb-4">
                    <div className="card border-left-success shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                        Active Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">{stats.activeDeliveries}</div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-truck fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Completed Deliveries Card */}
                <div className="col-xl-3 col-md-6 mb-4">
                    <div className="card border-left-info shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-info text-uppercase mb-1">
                                        Completed Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">{stats.completedDeliveries}</div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-check-circle fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Total Spent Card */}
                <div className="col-xl-3 col-md-6 mb-4">
                    <div className="card border-left-warning shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                        Total Spent
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        ${stats.totalSpent.toFixed(2)}
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-dollar-sign fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Recent Deliveries */}
            <div className="row">
                <div className="col-xl-12">
                    <div className="card shadow mb-4">
                        <div className="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                            <h6 className="m-0 font-weight-bold text-primary">Recent Deliveries</h6>
                        </div>
                        <div className="card-body">
                            <div className="table-responsive">
                                <table className="table table-bordered" width="100%" cellSpacing="0">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Status</th>
                                            <th>From</th>
                                            <th>To</th>
                                            <th>Created At</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {recentDeliveries.map((delivery) => (
                                            <tr key={delivery.id}>
                                                <td>{delivery.id}</td>
                                                <td>
                                                    <span className={`badge badge-${getStatusBadgeColor(delivery.status)}`}>
                                                        {delivery.status}
                                                    </span>
                                                </td>
                                                <td>{delivery.fromAddress}</td>
                                                <td>{delivery.toAddress}</td>
                                                <td>{new Date(delivery.createdAt).toLocaleString()}</td>
                                                <td>
                                                    <Link to={`/customer/delivery/${delivery.id}`} 
                                                          className="btn btn-primary btn-sm">
                                                        View Details
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
            </div>
        </>
    );
};

const getStatusBadgeColor = (status) => {
    switch (status) {
        case 'PENDING':
            return 'warning';
        case 'IN_TRANSIT':
            return 'info';
        case 'DELIVERED':
            return 'success';
        case 'CANCELLED':
            return 'danger';
        default:
            return 'secondary';
    }
};

export default CustomerDashboard; 