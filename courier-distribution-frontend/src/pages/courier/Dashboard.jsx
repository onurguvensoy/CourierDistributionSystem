import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Table, Badge, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const CourierDashboard = () => {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalDeliveries: 0,
        activeDeliveries: 0,
        completedDeliveries: 0,
        rating: 0
    });
    const [activePackages, setActivePackages] = useState([]);

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            const token = localStorage.getItem('token');
            const [statsResponse, packagesResponse] = await Promise.all([
                axios.get(`${API_URL}/courier/stats`, {
                    headers: { Authorization: `Bearer ${token}` }
                }),
                axios.get(`${API_URL}/courier/active-packages`, {
                    headers: { Authorization: `Bearer ${token}` }
                })
            ]);

            setStats(statsResponse.data);
            setActivePackages(packagesResponse.data);
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
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Courier Dashboard</h1>
                <Link to="/courier/deliveries" className="d-none d-sm-inline-block btn btn-primary shadow-sm">
                    <i className="fas fa-truck fa-sm text-white-50 me-2"></i>
                    View Available Deliveries
                </Link>
            </div>

            <Row>
                <Col xl={3} md={6} className="mb-4">
                    <Card className="border-left-primary shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="me-2">
                                    <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                        Total Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.totalDeliveries}
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <i className="fas fa-truck fa-2x text-gray-300"></i>
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>

                <Col xl={3} md={6} className="mb-4">
                    <Card className="border-left-success shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="me-2">
                                    <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                        Active Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.activeDeliveries}
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <i className="fas fa-shipping-fast fa-2x text-gray-300"></i>
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>

                <Col xl={3} md={6} className="mb-4">
                    <Card className="border-left-info shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="me-2">
                                    <div className="text-xs font-weight-bold text-info text-uppercase mb-1">
                                        Completed Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.completedDeliveries}
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <i className="fas fa-check-circle fa-2x text-gray-300"></i>
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>

                <Col xl={3} md={6} className="mb-4">
                    <Card className="border-left-warning shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="me-2">
                                    <div className="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                        Average Rating
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.rating.toFixed(1)} / 5.0
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <i className="fas fa-star fa-2x text-gray-300"></i>
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <Card className="shadow mb-4">
                <Card.Header className="py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Active Deliveries</h6>
                </Card.Header>
                <Card.Body>
                    {activePackages.length === 0 ? (
                        <div className="text-center py-4">
                            <p className="text-gray-500 mb-0">No active deliveries</p>
                            <Link to="/courier/deliveries" className="btn btn-primary mt-3">
                                Find Deliveries
                            </Link>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <Table className="table-bordered" width="100%" cellSpacing="0">
                                <thead>
                                    <tr>
                                        <th>Package ID</th>
                                        <th>Customer</th>
                                        <th>Pickup Address</th>
                                        <th>Delivery Address</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {activePackages.map((pkg) => (
                                        <tr key={pkg.id}>
                                            <td>{pkg.id}</td>
                                            <td>{pkg.customerName}</td>
                                            <td>{pkg.pickupAddress}</td>
                                            <td>{pkg.deliveryAddress}</td>
                                            <td>
                                                <Badge bg={getStatusBadgeColor(pkg.status)}>
                                                    {pkg.status}
                                                </Badge>
                                            </td>
                                            <td>
                                                <Button
                                                    variant="primary"
                                                    size="sm"
                                                    as={Link}
                                                    to={`/courier/delivery/${pkg.id}`}
                                                >
                                                    View Details
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </Card.Body>
            </Card>
        </>
    );
};

const getStatusBadgeColor = (status) => {
    switch (status) {
        case 'PICKED_UP':
            return 'info';
        case 'IN_TRANSIT':
            return 'primary';
        case 'DELIVERED':
            return 'success';
        case 'FAILED':
            return 'danger';
        default:
            return 'secondary';
    }
};

export default CourierDashboard; 