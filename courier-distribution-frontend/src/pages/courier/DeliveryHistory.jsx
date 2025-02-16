import React, { useState, useEffect } from 'react';
import { Card, Table, Badge, Button, Form, InputGroup, Row, Col } from 'react-bootstrap';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const DeliveryHistory = () => {
    const [loading, setLoading] = useState(true);
    const [deliveries, setDeliveries] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filter, setFilter] = useState('all');
    const [dateRange, setDateRange] = useState({
        startDate: '',
        endDate: ''
    });

    useEffect(() => {
        fetchDeliveryHistory();
    }, []);

    const fetchDeliveryHistory = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/courier/delivery-history`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setDeliveries(response.data);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to fetch delivery history');
            console.error('Delivery history fetch error:', error);
        } finally {
            setLoading(false);
        }
    };

    const filteredDeliveries = deliveries
        .filter(delivery => {
            if (filter === 'all') return true;
            return delivery.status === filter;
        })
        .filter(delivery => {
            if (!searchTerm) return true;
            return (
                delivery.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                delivery.pickupAddress.toLowerCase().includes(searchTerm.toLowerCase()) ||
                delivery.deliveryAddress.toLowerCase().includes(searchTerm.toLowerCase())
            );
        })
        .filter(delivery => {
            const deliveryDate = new Date(delivery.completedAt || delivery.createdAt);
            const start = dateRange.startDate ? new Date(dateRange.startDate) : null;
            const end = dateRange.endDate ? new Date(dateRange.endDate) : null;

            if (start && end) {
                return deliveryDate >= start && deliveryDate <= end;
            } else if (start) {
                return deliveryDate >= start;
            } else if (end) {
                return deliveryDate <= end;
            }
            return true;
        });

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
                <h1 className="h3 mb-0 text-gray-800">Delivery History</h1>
            </div>

            <Card className="shadow mb-4">
                <Card.Header className="py-3">
                    <h6 className="m-0 font-weight-bold text-primary mb-3">Delivery History List</h6>
                    <Row className="align-items-center">
                        <Col md={4}>
                            <InputGroup>
                                <InputGroup.Text>
                                    <i className="fas fa-search"></i>
                                </InputGroup.Text>
                                <Form.Control
                                    type="text"
                                    placeholder="Search deliveries..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                            </InputGroup>
                        </Col>
                        <Col md={2}>
                            <Form.Select
                                value={filter}
                                onChange={(e) => setFilter(e.target.value)}
                            >
                                <option value="all">All Status</option>
                                <option value="DELIVERED">Delivered</option>
                                <option value="FAILED">Failed</option>
                                <option value="CANCELLED">Cancelled</option>
                            </Form.Select>
                        </Col>
                        <Col md={3}>
                            <Form.Control
                                type="date"
                                placeholder="Start Date"
                                value={dateRange.startDate}
                                onChange={(e) =>
                                    setDateRange((prev) => ({ ...prev, startDate: e.target.value }))
                                }
                            />
                        </Col>
                        <Col md={3}>
                            <Form.Control
                                type="date"
                                placeholder="End Date"
                                value={dateRange.endDate}
                                onChange={(e) =>
                                    setDateRange((prev) => ({ ...prev, endDate: e.target.value }))
                                }
                            />
                        </Col>
                    </Row>
                </Card.Header>
                <Card.Body>
                    {filteredDeliveries.length === 0 ? (
                        <div className="text-center py-4">
                            <p className="text-gray-500 mb-0">No delivery history found</p>
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
                                        <th>Completed At</th>
                                        <th>Rating</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredDeliveries.map((delivery) => (
                                        <tr key={delivery.id}>
                                            <td>{delivery.id}</td>
                                            <td>{delivery.customerName}</td>
                                            <td>{delivery.pickupAddress}</td>
                                            <td>{delivery.deliveryAddress}</td>
                                            <td>
                                                <Badge bg={getStatusBadgeColor(delivery.status)}>
                                                    {delivery.status}
                                                </Badge>
                                            </td>
                                            <td>
                                                {delivery.completedAt
                                                    ? new Date(delivery.completedAt).toLocaleDateString()
                                                    : '-'}
                                            </td>
                                            <td>
                                                {delivery.rating ? (
                                                    <span className="text-warning">
                                                        <i className="fas fa-star me-1"></i>
                                                        {delivery.rating.toFixed(1)}
                                                    </span>
                                                ) : (
                                                    '-'
                                                )}
                                            </td>
                                            <td>
                                                <Button
                                                    variant="primary"
                                                    size="sm"
                                                    href={`/courier/delivery/${delivery.id}`}
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
        case 'DELIVERED':
            return 'success';
        case 'FAILED':
            return 'danger';
        case 'CANCELLED':
            return 'secondary';
        default:
            return 'secondary';
    }
};

export default DeliveryHistory; 