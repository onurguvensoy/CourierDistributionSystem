import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Button, Row, Col } from 'react-bootstrap';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const NewPackage = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [validated, setValidated] = useState(false);
    const [formData, setFormData] = useState({
        pickupAddress: '',
        deliveryAddress: '',
        weight: '',
        dimensions: {
            length: '',
            width: '',
            height: ''
        },
        description: '',
        fragile: false,
        express: false
    });

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        if (name.includes('.')) {
            const [parent, child] = name.split('.');
            setFormData(prev => ({
                ...prev,
                [parent]: {
                    ...prev[parent],
                    [child]: value
                }
            }));
        } else {
            setFormData(prev => ({
                ...prev,
                [name]: type === 'checkbox' ? checked : value
            }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const form = e.currentTarget;
        
        if (!form.checkValidity()) {
            e.stopPropagation();
            setValidated(true);
            return;
        }

        setLoading(true);
        try {
            const token = localStorage.getItem('token');
            const response = await axios.post(
                `${API_URL}/customer/packages`,
                formData,
                {
                    headers: { Authorization: `Bearer ${token}` }
                }
            );
            toast.success('Package created successfully');
            navigate(`/customer/tracking/${response.data.id}`);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to create package');
            console.error('Package creation error:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">New Package</h1>
            </div>

            <Card className="shadow mb-4">
                <Card.Header className="py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Package Details</h6>
                </Card.Header>
                <Card.Body>
                    <Form noValidate validated={validated} onSubmit={handleSubmit}>
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Pickup Address</Form.Label>
                                    <Form.Control
                                        required
                                        type="text"
                                        name="pickupAddress"
                                        value={formData.pickupAddress}
                                        onChange={handleChange}
                                        placeholder="Enter pickup address"
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        Please provide a pickup address.
                                    </Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Delivery Address</Form.Label>
                                    <Form.Control
                                        required
                                        type="text"
                                        name="deliveryAddress"
                                        value={formData.deliveryAddress}
                                        onChange={handleChange}
                                        placeholder="Enter delivery address"
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        Please provide a delivery address.
                                    </Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                        </Row>

                        <Row>
                            <Col md={3}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Weight (kg)</Form.Label>
                                    <Form.Control
                                        required
                                        type="number"
                                        min="0.1"
                                        step="0.1"
                                        name="weight"
                                        value={formData.weight}
                                        onChange={handleChange}
                                        placeholder="Enter weight"
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        Please provide a valid weight.
                                    </Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                            <Col md={3}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Length (cm)</Form.Label>
                                    <Form.Control
                                        required
                                        type="number"
                                        min="1"
                                        name="dimensions.length"
                                        value={formData.dimensions.length}
                                        onChange={handleChange}
                                        placeholder="Enter length"
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={3}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Width (cm)</Form.Label>
                                    <Form.Control
                                        required
                                        type="number"
                                        min="1"
                                        name="dimensions.width"
                                        value={formData.dimensions.width}
                                        onChange={handleChange}
                                        placeholder="Enter width"
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={3}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Height (cm)</Form.Label>
                                    <Form.Control
                                        required
                                        type="number"
                                        min="1"
                                        name="dimensions.height"
                                        value={formData.dimensions.height}
                                        onChange={handleChange}
                                        placeholder="Enter height"
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Form.Group className="mb-3">
                            <Form.Label>Description</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={3}
                                name="description"
                                value={formData.description}
                                onChange={handleChange}
                                placeholder="Enter package description (optional)"
                            />
                        </Form.Group>

                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Check
                                        type="checkbox"
                                        label="Fragile Package"
                                        name="fragile"
                                        checked={formData.fragile}
                                        onChange={handleChange}
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Check
                                        type="checkbox"
                                        label="Express Delivery"
                                        name="express"
                                        checked={formData.express}
                                        onChange={handleChange}
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <div className="d-grid gap-2">
                            <Button
                                variant="primary"
                                type="submit"
                                disabled={loading}
                            >
                                {loading ? 'Creating...' : 'Create Package'}
                            </Button>
                        </div>
                    </Form>
                </Card.Body>
            </Card>
        </>
    );
};

export default NewPackage; 