import React, { useState, useEffect } from 'react';
import { Card, Table, Badge, Button, Form, InputGroup } from 'react-bootstrap';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Deliveries = () => {
    const [loading, setLoading] = useState(true);
    const [deliveries, setDeliveries] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filter, setFilter] = useState('all');

    useEffect(() => {
        fetchDeliveries();
    }, []);

    const fetchDeliveries = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/courier/available-deliveries`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setDeliveries(response.data);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to fetch deliveries');
            console.error('Deliveries fetch error:', error);
        } finally {
            setLoading(false);
        }
    };

    const acceptDelivery = async (deliveryId) => {
        try {
            const token = localStorage.getItem('token');
            await axios.post(
                `${API_URL}/courier/accept-delivery/${deliveryId}`,
                {},
                {
                    headers: { Authorization: `Bearer ${token}` }
                }
            );
            toast.success('Delivery accepted successfully');
            fetchDeliveries();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to accept delivery');
            console.error('Accept delivery error:', error);
        }
    };

    const filteredDeliveries = deliveries
        .filter(delivery => {
            if (filter === 'all') return true;
            return delivery.status === filter;
        })
        .filter(delivery =>
            searchTerm
                ? delivery.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                  delivery.pickupAddress.toLowerCase().includes(searchTerm.toLowerCase()) ||
                  delivery.deliveryAddress.toLowerCase().includes(searchTerm.toLowerCase())
                : true
        );

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
                <h1 className="h3 mb-0 text-gray-800">Available Deliveries</h1>
            </div>

            <Card className="shadow mb-4">
                <Card.Header className="py-3 d-flex flex-wrap justify-content-between align-items-center">
                    <h6 className="m-0 font-weight-bold text-primary">Deliveries List</h6>
                    <div className="d-flex gap-3">
                        <InputGroup style={{ width: '300px' }}>
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
                        <Form.Select
                            value={filter}
                            onChange={(e) => setFilter(e.target.value)}
                            style={{ width: '150px' }}
                        >
                            <option value="all">All Status</option>
                            <option value="PENDING">Pending</option>
                            <option value="ACCEPTED">Accepted</option>
                            <option value="IN_TRANSIT">In Transit</option>
                        </Form.Select>
                    </div>
                </Card.Header>
                <Card.Body>
                    {filteredDeliveries.length === 0 ? (
                        <div className="text-center py-4">
                            <p className="text-gray-500 mb-0">No deliveries found</p>
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
                                                {delivery.status === 'PENDING' && (
                                                    <Button
                                                        variant="success"
                                                        size="sm"
                                                        onClick={() => acceptDelivery(delivery.id)}
                                                    >
                                                        Accept Delivery
                                                    </Button>
                                                )}
                                                {delivery.status !== 'PENDING' && (
                                                    <Button
                                                        variant="primary"
                                                        size="sm"
                                                        href={`/courier/delivery/${delivery.id}`}
                                                    >
                                                        View Details
                                                    </Button>
                                                )}
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
        case 'PENDING':
            return 'warning';
        case 'ACCEPTED':
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

export default Deliveries; 