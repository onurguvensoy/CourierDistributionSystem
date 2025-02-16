import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
const WS_URL = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';

const CourierDashboard = () => {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalDeliveries: 0,
        completedDeliveries: 0,
        activeDeliveries: 0,
        rating: 0
    });
    const [activeDeliveries, setActiveDeliveries] = useState([]);
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
                client.subscribe(`/topic/courier/${user.userId}`, (message) => {
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
        setActiveDeliveries(prev => {
            const index = prev.findIndex(d => d.id === delivery.id);
            if (index === -1) {
                return [...prev, delivery];
            }
            const updated = [...prev];
            updated[index] = delivery;
            return updated;
        });
    };

    const fetchDashboardData = async () => {
        try {
            const token = localStorage.getItem('token');
            const [statsResponse, deliveriesResponse] = await Promise.all([
                axios.get(`${API_URL}/courier/stats`, {
                    headers: { Authorization: `Bearer ${token}` }
                }),
                axios.get(`${API_URL}/courier/active-deliveries`, {
                    headers: { Authorization: `Bearer ${token}` }
                })
            ]);

            setStats(statsResponse.data);
            setActiveDeliveries(deliveriesResponse.data);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to fetch dashboard data');
            console.error('Dashboard data error:', error);
        } finally {
            setLoading(false);
        }
    };

    const updateDeliveryStatus = async (deliveryId, status) => {
        try {
            const token = localStorage.getItem('token');
            await axios.put(
                `${API_URL}/courier/deliveries/${deliveryId}/status`,
                { status },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            toast.success('Delivery status updated successfully');
            fetchDashboardData();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to update delivery status');
            console.error('Status update error:', error);
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
            <h1 className="h3 mb-4 text-gray-800">Courier Dashboard</h1>

            <div className="row">
                <div className="col-xl-3 col-md-6 mb-4">
                    <div className="card border-left-primary shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                        Total Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.totalDeliveries}
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-box fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="col-xl-3 col-md-6 mb-4">
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

                <div className="col-xl-3 col-md-6 mb-4">
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

                <div className="col-xl-3 col-md-6 mb-4">
                    <div className="card border-left-warning shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                        Rating
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.rating.toFixed(1)}/5.0
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-star fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Active Deliveries</h6>
                </div>
                <div className="card-body">
                    {activeDeliveries.length === 0 ? (
                        <div className="text-center py-4">
                            <p className="text-gray-500 mb-0">No active deliveries at the moment.</p>
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
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {activeDeliveries.map((delivery) => (
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
                                            <td>
                                                {delivery.status === 'ASSIGNED' && (
                                                    <button
                                                        className="btn btn-sm btn-primary mr-2"
                                                        onClick={() => updateDeliveryStatus(delivery.id, 'PICKED_UP')}
                                                    >
                                                        Mark as Picked Up
                                                    </button>
                                                )}
                                                {delivery.status === 'PICKED_UP' && (
                                                    <button
                                                        className="btn btn-sm btn-success"
                                                        onClick={() => updateDeliveryStatus(delivery.id, 'DELIVERED')}
                                                    >
                                                        Mark as Delivered
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

export default CourierDashboard; 