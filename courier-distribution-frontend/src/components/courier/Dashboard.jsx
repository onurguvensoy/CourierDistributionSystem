import React, { useState, useEffect, useCallback } from 'react';
import { Card, Row, Col, Button, Badge } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTruck, faBox } from '@fortawesome/free-solid-svg-icons';
import DataTable from 'react-data-table-component';
import { toast } from 'react-toastify';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import authService from '../../services/authService';

const Dashboard = () => {
    const [stats, setStats] = useState({
        activeDeliveries: 0,
        availablePackages: 0
    });
    const [availablePackages, setAvailablePackages] = useState([]);
    const [activeDeliveries, setActiveDeliveries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [stompClient, setStompClient] = useState(null);

    const updateAvailablePackagesTable = useCallback((packages) => {
        setAvailablePackages(packages);
        setStats(prev => ({ ...prev, availablePackages: packages.length }));
    }, []);

    const updateActiveDeliveriesTable = useCallback((deliveries) => {
        setActiveDeliveries(deliveries);
        setStats(prev => ({ ...prev, activeDeliveries: deliveries.length }));
    }, []);

    const initializeWebSocket = useCallback(() => {
        try {
            const socket = new SockJS('/websocket');
            const client = Stomp.over(socket);
            
            const username = authService.getUsername();
            if (!username) {
                console.error('Username not found');
                toast.error('Authentication error: Username not found');
                return;
            }
            
            const connectHeaders = {
                username: username
            };
            
            client.connect(connectHeaders, () => {
                console.log('Connected to WebSocket with username:', username);
                
                // Subscribe to available packages updates
                client.subscribe('/user/queue/packages/available', (message) => {
                    try {
                        const packages = JSON.parse(message.body);
                        updateAvailablePackagesTable(packages);
                    } catch (error) {
                        console.error('Error processing available packages:', error);
                        toast.error('Error processing available packages');
                    }
                });
                
                // Subscribe to active deliveries updates
                client.subscribe('/user/queue/packages/active', (message) => {
                    try {
                        const deliveries = JSON.parse(message.body);
                        updateActiveDeliveriesTable(deliveries);
                    } catch (error) {
                        console.error('Error processing active deliveries:', error);
                        toast.error('Error processing active deliveries');
                    }
                });
            }, (error) => {
                console.error('WebSocket connection error:', error);
                toast.error('Failed to connect to real-time updates');
            });
            
            setStompClient(client);
        } catch (error) {
            console.error('Error initializing WebSocket:', error);
            toast.error('Failed to initialize real-time updates');
        }
    }, [updateAvailablePackagesTable, updateActiveDeliveriesTable]);

    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                const [availableResponse, activeResponse] = await Promise.all([
                    authService.get('/api/courier/packages/available'),
                    authService.get('/api/courier/packages/active')
                ]);

                updateAvailablePackagesTable(availableResponse.data);
                updateActiveDeliveriesTable(activeResponse.data);
            } catch (error) {
                console.error('Error fetching initial data:', error);
                toast.error('Failed to load dashboard data');
            } finally {
                setLoading(false);
            }
        };

        fetchInitialData();
        initializeWebSocket();

        return () => {
            if (stompClient) {
                stompClient.disconnect();
            }
        };
    }, [initializeWebSocket, updateAvailablePackagesTable, updateActiveDeliveriesTable]);

    const takeDelivery = async (packageId) => {
        try {
            await authService.post(`/api/courier/packages/${packageId}/take`);
            toast.success('Package assigned successfully');
        } catch (error) {
            console.error('Error taking delivery:', error);
            toast.error('Failed to take delivery');
        }
    };

    const updateDeliveryStatus = async (packageId, newStatus) => {
        try {
            await authService.post(`/api/courier/packages/${packageId}/status`, { status: newStatus });
            toast.success('Delivery status updated successfully');
        } catch (error) {
            console.error('Error updating delivery status:', error);
            toast.error('Failed to update delivery status');
        }
    };

    const getStatusBadge = (status) => {
        const variants = {
            'ASSIGNED': 'info',
            'PICKED_UP': 'primary',
            'IN_TRANSIT': 'warning',
            'DELIVERED': 'success',
            'FAILED': 'danger'
        };
        return <Badge bg={variants[status] || 'secondary'}>{status}</Badge>;
    };

    const getActionButton = (packageId, status) => {
        switch (status) {
            case 'ASSIGNED':
                return (
                    <Button variant="primary" size="sm" onClick={() => updateDeliveryStatus(packageId, 'PICKED_UP')}>
                        <FontAwesomeIcon icon={faTruck} className="me-1" /> Pick Up
                    </Button>
                );
            case 'PICKED_UP':
                return (
                    <Button variant="warning" size="sm" onClick={() => updateDeliveryStatus(packageId, 'IN_TRANSIT')}>
                        <FontAwesomeIcon icon={faTruck} className="me-1" /> Start Delivery
                    </Button>
                );
            case 'IN_TRANSIT':
                return (
                    <div className="d-flex gap-2">
                        <Button variant="success" size="sm" onClick={() => updateDeliveryStatus(packageId, 'DELIVERED')}>
                            <FontAwesomeIcon icon={faTruck} className="me-1" /> Complete
                        </Button>
                        <Button variant="danger" size="sm" onClick={() => updateDeliveryStatus(packageId, 'FAILED')}>
                            <FontAwesomeIcon icon={faTruck} className="me-1" /> Failed
                        </Button>
                    </div>
                );
            default:
                return null;
        }
    };

    const availablePackagesColumns = [
        {
            name: 'Package ID',
            selector: row => row.id,
            sortable: true
        },
        {
            name: 'Customer',
            selector: row => row.customerUsername,
            sortable: true
        },
        {
            name: 'Pickup Address',
            selector: row => row.pickupAddress,
            sortable: true
        },
        {
            name: 'Delivery Address',
            selector: row => row.deliveryAddress,
            sortable: true
        },
        {
            name: 'Weight',
            selector: row => row.weight,
            sortable: true,
            cell: row => `${row.weight} kg`
        },
        {
            name: 'Actions',
            cell: row => (
                <Button variant="primary" size="sm" onClick={() => takeDelivery(row.id)}>
                    <FontAwesomeIcon icon={faTruck} className="me-1" /> Take Delivery
                </Button>
            )
        }
    ];

    const activeDeliveriesColumns = [
        {
            name: 'Package ID',
            selector: row => row.id,
            sortable: true
        },
        {
            name: 'Customer',
            selector: row => row.customerUsername,
            sortable: true
        },
        {
            name: 'Pickup Address',
            selector: row => row.pickupAddress,
            sortable: true
        },
        {
            name: 'Delivery Address',
            selector: row => row.deliveryAddress,
            sortable: true
        },
        {
            name: 'Status',
            selector: row => row.status,
            sortable: true,
            cell: row => getStatusBadge(row.status)
        },
        {
            name: 'Actions',
            cell: row => getActionButton(row.id, row.status)
        }
    ];

    if (loading) {
        return <div className="text-center mt-5"><div className="spinner-border" /></div>;
    }

    return (
        <div className="container-fluid">
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Courier Dashboard</h1>
            </div>

            <Row>
                <Col xl={6} md={6} className="mb-4">
                    <Card className="border-left-primary shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="mr-2">
                                    <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                        Active Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.activeDeliveries}
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <FontAwesomeIcon icon={faTruck} className="fa-2x text-gray-300" />
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>

                <Col xl={6} md={6} className="mb-4">
                    <Card className="border-left-success shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="mr-2">
                                    <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                        Available Packages
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.availablePackages}
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <FontAwesomeIcon icon={faBox} className="fa-2x text-gray-300" />
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <Card className="shadow mb-4">
                <Card.Header className="py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Available Packages</h6>
                </Card.Header>
                <Card.Body>
                    <DataTable
                        columns={availablePackagesColumns}
                        data={availablePackages}
                        pagination
                        responsive
                        highlightOnHover
                        striped
                    />
                </Card.Body>
            </Card>

            <Card className="shadow mb-4">
                <Card.Header className="py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Active Deliveries</h6>
                </Card.Header>
                <Card.Body>
                    <DataTable
                        columns={activeDeliveriesColumns}
                        data={activeDeliveries}
                        pagination
                        responsive
                        highlightOnHover
                        striped
                    />
                </Card.Body>
            </Card>
        </div>
    );
};

export default Dashboard; 