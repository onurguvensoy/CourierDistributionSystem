import React, { useState, useEffect, useCallback } from 'react';
import { Card, Row, Col, Badge, Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faClock, faTruck, faCheckCircle, faPlus, faHistory } from '@fortawesome/free-solid-svg-icons';
import DataTable from 'react-data-table-component';
import moment from 'moment';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import authService from '../../services/authService';

const Dashboard = () => {
    const [stats, setStats] = useState({
        pendingPackages: 0,
        inTransitPackages: 0,
        deliveredPackages: 0
    });
    const [activePackages, setActivePackages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [stompClient, setStompClient] = useState(null);

    const updateStats = useCallback((packages) => {
        const pendingCount = packages.filter(pkg => pkg.status === 'PENDING').length;
        const inTransitCount = packages.filter(pkg => 
            ['IN_TRANSIT', 'PICKED_UP', 'ASSIGNED'].includes(pkg.status)
        ).length;
        const deliveredCount = packages.filter(pkg => pkg.status === 'DELIVERED').length;

        setStats({
            pendingPackages: pendingCount,
            inTransitPackages: inTransitCount,
            deliveredPackages: deliveredCount
        });
    }, []);

    const updatePackages = useCallback((packages) => {
        setActivePackages(packages);
        updateStats(packages);
    }, [updateStats]);

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
                
                // Subscribe to package updates
                client.subscribe('/user/queue/packages', (message) => {
                    try {
                        const packages = JSON.parse(message.body);
                        updatePackages(packages);
                    } catch (error) {
                        console.error('Error processing package updates:', error);
                        toast.error('Error processing package updates');
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
    }, [updatePackages]);

    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                const response = await authService.get('/api/customer/packages/active');
                updatePackages(response.data);
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
    }, [initializeWebSocket, updatePackages]);

    const getStatusBadge = (status) => {
        const variants = {
            'PENDING': 'warning',
            'ASSIGNED': 'info',
            'PICKED_UP': 'primary',
            'IN_TRANSIT': 'info',
            'DELIVERED': 'success'
        };
        return <Badge bg={variants[status] || 'secondary'}>{status}</Badge>;
    };

    const columns = [
        {
            name: 'Package ID',
            selector: row => row.package_id,
            sortable: true
        },
        {
            name: 'Pickup Location',
            selector: row => row.pickupAddress,
            sortable: true
        },
        {
            name: 'Delivery Location',
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
            name: 'Courier',
            selector: row => row.courier?.username,
            sortable: true,
            cell: row => row.courier?.username || 'Not Assigned'
        },
        {
            name: 'Created At',
            selector: row => row.createdAt,
            sortable: true,
            cell: row => moment(row.createdAt).format('YYYY-MM-DD HH:mm:ss')
        }
    ];

    if (loading) {
        return <div className="text-center mt-5"><div className="spinner-border" /></div>;
    }

    return (
        <div className="container-fluid">
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">My Packages</h1>
                <div>
                    <Link to="/customer/new-package" className="btn btn-primary me-2">
                        <FontAwesomeIcon icon={faPlus} className="me-2" /> Create New Package
                    </Link>
                </div>
            </div>

            <Row>
                <Col xl={4} md={6} className="mb-4">
                    <Card className="border-left-warning shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="mr-2">
                                    <div className="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                        Pending Packages
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.pendingPackages}
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <FontAwesomeIcon icon={faClock} className="fa-2x text-gray-300" />
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>

                <Col xl={4} md={6} className="mb-4">
                    <Card className="border-left-info shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="mr-2">
                                    <div className="text-xs font-weight-bold text-info text-uppercase mb-1">
                                        In Transit
                                    </div>
                                    <div className="row no-gutters align-items-center">
                                        <div className="col-auto">
                                            <div className="h5 mb-0 mr-3 font-weight-bold text-gray-800">
                                                {stats.inTransitPackages}
                                            </div>
                                        </div>
                                        <div className="col">
                                            <div className="progress progress-sm mr-2">
                                                <div 
                                                    className="progress-bar bg-info" 
                                                    role="progressbar" 
                                                    style={{ width: stats.inTransitPackages > 0 ? '100%' : '0%' }}
                                                    aria-valuenow={stats.inTransitPackages}
                                                    aria-valuemin="0"
                                                    aria-valuemax="100"
                                                />
                                            </div>
                                        </div>
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <FontAwesomeIcon icon={faTruck} className="fa-2x text-gray-300" />
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>

                <Col xl={4} md={6} className="mb-4">
                    <Card className="border-left-success shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="mr-2">
                                    <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                        Delivered
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.deliveredPackages}
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <FontAwesomeIcon icon={faCheckCircle} className="fa-2x text-gray-300" />
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <Card className="shadow mb-4">
                <Card.Header className="py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 className="m-0 font-weight-bold text-primary">Active Packages</h6>
                    <Link to="/customer/delivery-history" className="btn btn-info btn-sm">
                        <FontAwesomeIcon icon={faHistory} className="me-1" /> View Delivery History
                    </Link>
                </Card.Header>
                <Card.Body>
                    <DataTable
                        columns={columns}
                        data={activePackages}
                        pagination
                        responsive
                        highlightOnHover
                        striped
                        defaultSortFieldId={1}
                        defaultSortAsc={false}
                    />
                </Card.Body>
            </Card>
        </div>
    );
};

export default Dashboard; 