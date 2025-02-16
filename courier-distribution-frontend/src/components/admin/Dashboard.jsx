import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import moment from 'moment';
import DataTable from 'react-data-table-component';
import { Card, Row, Col, Badge } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faDownload, faUsers, faTruck, faClock, faCheckCircle } from '@fortawesome/free-solid-svg-icons';
import authService from '../../services/authService';

const Dashboard = () => {
    const [stats, setStats] = useState({
        totalUsers: 0,
        activeCouriers: 0,
        pendingPackages: 0,
        totalDeliveries: 0
    });

    const [users, setUsers] = useState([]);
    const [packages, setPackages] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const [statsResponse, usersResponse, packagesResponse] = await Promise.all([
                    authService.get('/api/admin/stats'),
                    authService.get('/api/admin/users'),
                    authService.get('/api/admin/packages')
                ]);

                setStats(statsResponse.data);
                setUsers(usersResponse.data);
                setPackages(packagesResponse.data);
            } catch (error) {
                console.error('Error fetching dashboard data:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    const userColumns = [
        {
            name: 'Username',
            selector: row => row.username,
            sortable: true
        },
        {
            name: 'Role',
            selector: row => row.role,
            sortable: true,
            cell: row => (
                <Badge bg={row.role === 'ADMIN' ? 'danger' : 
                          row.role === 'COURIER' ? 'success' : 'primary'}>
                    {row.role}
                </Badge>
            )
        },
        {
            name: 'Email',
            selector: row => row.email,
            sortable: true
        },
        {
            name: 'Status',
            selector: row => row.status,
            sortable: true,
            cell: row => (
                row.role === 'COURIER' ? (
                    <Badge bg={row.available ? 'success' : 'secondary'}>
                        {row.available ? 'Available' : 'Busy'}
                    </Badge>
                ) : (
                    <Badge bg="success">Active</Badge>
                )
            )
        },
        {
            name: 'Created At',
            selector: row => row.createdAt,
            sortable: true,
            cell: row => moment(row.createdAt).format('YYYY-MM-DD HH:mm')
        }
    ];

    const packageColumns = [
        {
            name: 'Package ID',
            selector: row => row.package_id,
            sortable: true
        },
        {
            name: 'Customer',
            selector: row => row.customer?.username,
            sortable: true
        },
        {
            name: 'Courier',
            selector: row => row.courier?.username,
            sortable: true,
            cell: row => row.courier?.username || 'Not Assigned'
        },
        {
            name: 'Status',
            selector: row => row.status,
            sortable: true,
            cell: row => (
                <Badge bg={
                    row.status === 'PENDING' ? 'warning' :
                    row.status === 'ASSIGNED' ? 'info' :
                    row.status === 'PICKED_UP' ? 'primary' :
                    row.status === 'IN_TRANSIT' ? 'info' :
                    row.status === 'DELIVERED' ? 'success' : 'danger'
                }>
                    {row.status}
                </Badge>
            )
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
            name: 'Created At',
            selector: row => row.createdAt,
            sortable: true,
            cell: row => moment(row.createdAt).format('YYYY-MM-DD HH:mm')
        }
    ];

    if (loading) {
        return <div className="text-center mt-5"><div className="spinner-border" /></div>;
    }

    return (
        <div className="container-fluid">
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Admin Dashboard</h1>
                <Link to="/admin/reports" className="d-none d-sm-inline-block btn btn-primary shadow-sm">
                    <FontAwesomeIcon icon={faDownload} className="fa-sm text-white-50 me-2" />
                    View Reports
                </Link>
            </div>

            <Row>
                <Col xl={3} md={6} className="mb-4">
                    <Card className="border-left-primary shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="mr-2">
                                    <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                        Total Users
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.totalUsers}
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <FontAwesomeIcon icon={faUsers} className="fa-2x text-gray-300" />
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>

                <Col xl={3} md={6} className="mb-4">
                    <Card className="border-left-success shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="mr-2">
                                    <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                        Active Couriers
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.activeCouriers}
                                    </div>
                                </Col>
                                <Col xs="auto">
                                    <FontAwesomeIcon icon={faTruck} className="fa-2x text-gray-300" />
                                </Col>
                            </Row>
                        </Card.Body>
                    </Card>
                </Col>

                <Col xl={3} md={6} className="mb-4">
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

                <Col xl={3} md={6} className="mb-4">
                    <Card className="border-left-info shadow h-100 py-2">
                        <Card.Body>
                            <Row className="no-gutters align-items-center">
                                <Col className="mr-2">
                                    <div className="text-xs font-weight-bold text-info text-uppercase mb-1">
                                        Total Deliveries
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.totalDeliveries}
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

            <Row>
                <Col xs={12}>
                    <Card className="shadow mb-4">
                        <Card.Header className="py-3">
                            <h6 className="m-0 font-weight-bold text-primary">System Users</h6>
                        </Card.Header>
                        <Card.Body>
                            <DataTable
                                columns={userColumns}
                                data={users}
                                pagination
                                responsive
                                highlightOnHover
                                striped
                            />
                        </Card.Body>
                    </Card>
                </Col>

                <Col xs={12}>
                    <Card className="shadow mb-4">
                        <Card.Header className="py-3">
                            <h6 className="m-0 font-weight-bold text-primary">All Packages</h6>
                        </Card.Header>
                        <Card.Body>
                            <DataTable
                                columns={packageColumns}
                                data={packages}
                                pagination
                                responsive
                                highlightOnHover
                                striped
                            />
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default Dashboard; 