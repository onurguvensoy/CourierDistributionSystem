import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Card, Row, Col, Badge, Button, Form } from 'react-bootstrap';
import { toast } from 'react-toastify';
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
const WS_URL = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';

const PackageTracking = () => {
    const { packageId } = useParams();
    const [loading, setLoading] = useState(true);
    const [package_, setPackage] = useState(null);
    const [rating, setRating] = useState(0);
    const [stompClient, setStompClient] = useState(null);

    useEffect(() => {
        fetchPackageDetails();
        setupWebSocket();

        return () => {
            if (stompClient) {
                stompClient.disconnect();
            }
        };
    }, [packageId]);

    const setupWebSocket = () => {
        const socket = new SockJS(WS_URL);
        const client = Stomp.over(socket);
        const token = localStorage.getItem('token');

        client.connect(
            { Authorization: `Bearer ${token}` },
            () => {
                client.subscribe(`/user/queue/package/${packageId}/status`, (message) => {
                    const updatedPackage = JSON.parse(message.body);
                    setPackage(updatedPackage);
                });
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                toast.error('Failed to connect to real-time updates');
            }
        );

        setStompClient(client);
    };

    const fetchPackageDetails = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/customer/packages/${packageId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setPackage(response.data);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to fetch package details');
            console.error('Package details error:', error);
        } finally {
            setLoading(false);
        }
    };

    const submitRating = async () => {
        try {
            const token = localStorage.getItem('token');
            await axios.post(
                `${API_URL}/customer/packages/${packageId}/rate`,
                { rating },
                {
                    headers: { Authorization: `Bearer ${token}` }
                }
            );
            toast.success('Rating submitted successfully');
            fetchPackageDetails();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to submit rating');
            console.error('Rating submission error:', error);
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

    if (!package_) {
        return (
            <div className="text-center py-5">
                <h3>Package not found</h3>
                <p>The package you're looking for doesn't exist or you don't have permission to view it.</p>
            </div>
        );
    }

    return (
        <>
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Package Tracking</h1>
            </div>

            <Row>
                <Col lg={8}>
                    <Card className="shadow mb-4">
                        <Card.Header className="py-3">
                            <h6 className="m-0 font-weight-bold text-primary">Package Details</h6>
                        </Card.Header>
                        <Card.Body>
                            <Row className="mb-3">
                                <Col md={6}>
                                    <p className="mb-0 text-sm text-gray-600">Package ID</p>
                                    <p className="mb-0 font-weight-bold">{package_.id}</p>
                                </Col>
                                <Col md={6}>
                                    <p className="mb-0 text-sm text-gray-600">Status</p>
                                    <Badge bg={getStatusBadgeColor(package_.status)}>
                                        {package_.status}
                                    </Badge>
                                </Col>
                            </Row>

                            <Row className="mb-3">
                                <Col md={6}>
                                    <p className="mb-0 text-sm text-gray-600">Pickup Address</p>
                                    <p className="mb-0 font-weight-bold">{package_.pickupAddress}</p>
                                </Col>
                                <Col md={6}>
                                    <p className="mb-0 text-sm text-gray-600">Delivery Address</p>
                                    <p className="mb-0 font-weight-bold">{package_.deliveryAddress}</p>
                                </Col>
                            </Row>

                            <Row className="mb-3">
                                <Col md={4}>
                                    <p className="mb-0 text-sm text-gray-600">Weight</p>
                                    <p className="mb-0 font-weight-bold">{package_.weight} kg</p>
                                </Col>
                                <Col md={8}>
                                    <p className="mb-0 text-sm text-gray-600">Dimensions</p>
                                    <p className="mb-0 font-weight-bold">
                                        {package_.dimensions.length} x {package_.dimensions.width} x{' '}
                                        {package_.dimensions.height} cm
                                    </p>
                                </Col>
                            </Row>

                            <Row className="mb-3">
                                <Col md={6}>
                                    <p className="mb-0 text-sm text-gray-600">Created At</p>
                                    <p className="mb-0 font-weight-bold">
                                        {new Date(package_.createdAt).toLocaleString()}
                                    </p>
                                </Col>
                                <Col md={6}>
                                    <p className="mb-0 text-sm text-gray-600">Last Updated</p>
                                    <p className="mb-0 font-weight-bold">
                                        {new Date(package_.updatedAt).toLocaleString()}
                                    </p>
                                </Col>
                            </Row>

                            {package_.description && (
                                <div className="mb-3">
                                    <p className="mb-0 text-sm text-gray-600">Description</p>
                                    <p className="mb-0">{package_.description}</p>
                                </div>
                            )}

                            <div>
                                <p className="mb-0 text-sm text-gray-600">Package Features</p>
                                <div className="d-flex gap-2">
                                    {package_.fragile && (
                                        <Badge bg="danger">Fragile</Badge>
                                    )}
                                    {package_.express && (
                                        <Badge bg="warning">Express Delivery</Badge>
                                    )}
                                </div>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>

                <Col lg={4}>
                    <Card className="shadow mb-4">
                        <Card.Header className="py-3">
                            <h6 className="m-0 font-weight-bold text-primary">Courier Information</h6>
                        </Card.Header>
                        <Card.Body>
                            {package_.courierName ? (
                                <>
                                    <p className="mb-0 text-sm text-gray-600">Courier Name</p>
                                    <p className="mb-3 font-weight-bold">{package_.courierName}</p>

                                    {package_.status === 'DELIVERED' && !package_.rating && (
                                        <div>
                                            <p className="mb-2">Rate the Delivery Service</p>
                                            <div className="d-flex align-items-center gap-3 mb-3">
                                                <Form.Select
                                                    value={rating}
                                                    onChange={(e) => setRating(Number(e.target.value))}
                                                    style={{ width: '100px' }}
                                                >
                                                    <option value="0">Select</option>
                                                    <option value="1">1 Star</option>
                                                    <option value="2">2 Stars</option>
                                                    <option value="3">3 Stars</option>
                                                    <option value="4">4 Stars</option>
                                                    <option value="5">5 Stars</option>
                                                </Form.Select>
                                                <Button
                                                    variant="primary"
                                                    onClick={submitRating}
                                                    disabled={rating === 0}
                                                >
                                                    Submit Rating
                                                </Button>
                                            </div>
                                        </div>
                                    )}

                                    {package_.rating && (
                                        <div>
                                            <p className="mb-0 text-sm text-gray-600">Your Rating</p>
                                            <p className="mb-0 text-warning">
                                                <i className="fas fa-star me-1"></i>
                                                {package_.rating.toFixed(1)}
                                            </p>
                                        </div>
                                    )}
                                </>
                            ) : (
                                <p className="mb-0 text-gray-500">No courier assigned yet</p>
                            )}
                        </Card.Body>
                    </Card>

                    <Card className="shadow mb-4">
                        <Card.Header className="py-3">
                            <h6 className="m-0 font-weight-bold text-primary">Tracking Timeline</h6>
                        </Card.Header>
                        <Card.Body>
                            <div className="timeline">
                                {package_.trackingHistory?.map((event, index) => (
                                    <div key={index} className="timeline-item">
                                        <div className="timeline-badge bg-primary">
                                            <i className="fas fa-circle"></i>
                                        </div>
                                        <div className="timeline-content">
                                            <h6 className="mb-0">{event.status}</h6>
                                            <p className="text-sm text-gray-600 mb-0">
                                                {new Date(event.timestamp).toLocaleString()}
                                            </p>
                                            {event.note && (
                                                <p className="mb-0 mt-1">{event.note}</p>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </>
    );
};

const getStatusBadgeColor = (status) => {
    switch (status) {
        case 'PENDING':
            return 'warning';
        case 'PICKED_UP':
            return 'info';
        case 'IN_TRANSIT':
            return 'primary';
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

export default PackageTracking; 