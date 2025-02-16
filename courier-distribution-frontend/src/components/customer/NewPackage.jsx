import React, { useState, useEffect } from 'react';
import { Card, Form, Button, Alert } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBox, faTimes, faInfoCircle } from '@fortawesome/free-solid-svg-icons';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import authService from '../../services/authService';

const NewPackage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        pickupAddress: '',
        deliveryAddress: '',
        weight: '',
        description: '',
        specialInstructions: ''
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [validated, setValidated] = useState(false);
    const [formChanged, setFormChanged] = useState(false);

    useEffect(() => {
        // Handle unsaved changes warning
        const handleBeforeUnload = (e) => {
            if (formChanged) {
                e.preventDefault();
                e.returnValue = 'You have unsaved changes. Are you sure you want to leave?';
            }
        };

        window.addEventListener('beforeunload', handleBeforeUnload);

        return () => {
            window.removeEventListener('beforeunload', handleBeforeUnload);
        };
    }, [formChanged]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        setFormChanged(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const form = e.currentTarget;

        if (form.checkValidity() === false) {
            e.stopPropagation();
            setValidated(true);
            return;
        }

        setIsSubmitting(true);

        try {
            const response = await authService.post('/api/packages/create', {
                ...formData,
                weight: parseFloat(formData.weight)
            });

            if (response.data.status === 'success' || response.data.package_id) {
                toast.success('Package created successfully!');
                setTimeout(() => navigate('/customer/dashboard'), 1500);
            } else {
                toast.error(response.data.message || 'Failed to create package');
            }
        } catch (error) {
            console.error('Error creating package:', error);
            toast.error(error.response?.data?.message || 'Failed to create package. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="container-fluid">
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Create New Package</h1>
            </div>

            <div className="row">
                <div className="col-12">
                    <Card className="shadow mb-4">
                        <Card.Header className="py-3">
                            <h6 className="m-0 font-weight-bold text-primary">Package Details</h6>
                        </Card.Header>
                        <Card.Body>
                            <Form noValidate validated={validated} onSubmit={handleSubmit}>
                                <Form.Group className="mb-3">
                                    <Form.Label>
                                        Pickup Address <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="pickupAddress"
                                        value={formData.pickupAddress}
                                        onChange={handleInputChange}
                                        required
                                        minLength={5}
                                        maxLength={200}
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        Please provide a pickup address (5-200 characters).
                                    </Form.Control.Feedback>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label>
                                        Delivery Address <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="deliveryAddress"
                                        value={formData.deliveryAddress}
                                        onChange={handleInputChange}
                                        required
                                        minLength={5}
                                        maxLength={200}
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        Please provide a delivery address (5-200 characters).
                                    </Form.Control.Feedback>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label>
                                        Weight (kg) <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="weight"
                                        value={formData.weight}
                                        onChange={handleInputChange}
                                        required
                                        step="0.1"
                                        min="0.1"
                                        max="1000"
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        Please provide a valid weight between 0.1 and 1000 kg.
                                    </Form.Control.Feedback>
                                    <Form.Text className="text-muted">
                                        Enter the package weight in kilograms (0.1 - 1000 kg).
                                    </Form.Text>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label>
                                        Package Description <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={3}
                                        name="description"
                                        value={formData.description}
                                        onChange={handleInputChange}
                                        required
                                        minLength={10}
                                        maxLength={500}
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        Please provide a package description (10-500 characters).
                                    </Form.Control.Feedback>
                                    <Form.Text className="text-muted d-flex justify-content-between">
                                        <span>Describe the package contents and any relevant details (10-500 characters).</span>
                                        <span>{formData.description.length}/500</span>
                                    </Form.Text>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label>Special Instructions</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={2}
                                        name="specialInstructions"
                                        value={formData.specialInstructions}
                                        onChange={handleInputChange}
                                        maxLength={200}
                                    />
                                    <Form.Text className="text-muted d-flex justify-content-between">
                                        <span>Optional: Add any special handling instructions or notes for the courier.</span>
                                        <span>{formData.specialInstructions.length}/200</span>
                                    </Form.Text>
                                </Form.Group>

                                <Alert variant="info">
                                    <FontAwesomeIcon icon={faInfoCircle} className="me-2" />
                                    Please review all details carefully before submitting. 
                                    Once created, the package will be available for courier pickup.
                                </Alert>

                                <div className="mt-3">
                                    <Button 
                                        type="submit" 
                                        variant="primary" 
                                        className="me-2"
                                        disabled={isSubmitting}
                                    >
                                        {isSubmitting ? (
                                            <>
                                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                                                Creating...
                                            </>
                                        ) : (
                                            <>
                                                <FontAwesomeIcon icon={faBox} className="me-2" />
                                                Create Package
                                            </>
                                        )}
                                    </Button>
                                    <Link 
                                        to="/customer/dashboard" 
                                        className="btn btn-secondary"
                                        onClick={(e) => {
                                            if (formChanged && !window.confirm('You have unsaved changes. Are you sure you want to leave?')) {
                                                e.preventDefault();
                                            }
                                        }}
                                    >
                                        <FontAwesomeIcon icon={faTimes} className="me-2" />
                                        Cancel
                                    </Link>
                                </div>
                            </Form>
                        </Card.Body>
                    </Card>
                </div>
            </div>
        </div>
    );
};

export default NewPackage; 