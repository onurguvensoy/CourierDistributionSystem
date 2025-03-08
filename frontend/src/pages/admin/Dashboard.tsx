import React, { useState, useEffect, FC } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import moment from 'moment';
import DataTable from 'react-data-table-component';
import { Card, Row, Col, Badge } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { toast } from 'react-toastify';
import axios, { AxiosError } from 'axios';
import { useAuth } from '../../contexts/auth.context';
import type { AdminStats, User, DeliveryPackage } from '../../types/dashboard';
import { faDownload } from '@fortawesome/free-solid-svg-icons';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const AdminDashboard: FC = () => {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState<AdminStats>({
        totalUsers: 0,
        totalPackages: 0,
        activeDeliveries: 0,
        revenue: 0
    });
    const [users, setUsers] = useState<User[]>([]);
    const [packages, setPackages] = useState<DeliveryPackage[]>([]);
    const navigate = useNavigate();
    const { checkAuthorization, isLoading, isAuthenticated } = useAuth();

    useEffect(() => {
        const checkAuth = async () => {
            if (!isLoading) {
                if (!isAuthenticated) {
                    navigate('/login');
                    return;
                }
                const isAuthorized = await checkAuthorization('ADMIN');
                if (!isAuthorized) {
                    navigate('/login');
                }
            }
        };

        checkAuth();
    }, [isLoading, isAuthenticated, checkAuthorization, navigate]);

    useEffect(() => {
        fetchDashboardStats();
    }, []);

    const fetchDashboardStats = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/admin/dashboard/stats`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setStats(response.data);
        } catch (error) {
            toast.error(error instanceof AxiosError ? error.response?.data?.message || 'Failed to fetch dashboard statistics' : 'Failed to fetch dashboard statistics');
            console.error('Dashboard stats error:', error);
        } finally {
            setLoading(false);
        }
    };

    const userColumns = [
        {
            name: 'Username',
            selector: (row: User) => row.username,
            sortable: true
        },
        {
            name: 'Role',
            selector: (row: User) => row.role,
            sortable: true,
            cell: (row: User) => (
                <Badge bg={row.role === 'ADMIN' ? 'danger' : 
                          row.role === 'COURIER' ? 'success' : 'primary'}>
                    {row.role}
                </Badge>
            )
        },
        {
            name: 'Email',
            selector: (row: User) => row.email,
            sortable: true
        },
        {
            name: 'Status',
            selector: (row: User) => row.status,
            sortable: true,
            cell: (row: User) => (
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
            selector: (row: User) => row.createdAt,
            sortable: true,
            cell: (row: User) => moment(row.createdAt).format('YYYY-MM-DD HH:mm')
        }
    ];

    const packageColumns = [
        {
            name: 'Package ID',
            selector: (row: DeliveryPackage) => row.package_id || '',
            sortable: true
        },
        {
            name: 'Customer',
            selector: (row: DeliveryPackage) => row.customer?.username || '',
            sortable: true
        },
        {
            name: 'Courier',
            selector: (row: DeliveryPackage) => row.courier?.username || '',
            sortable: true,
            cell: (row: DeliveryPackage) => row.courier?.username || 'Not Assigned'
        },
        {
            name: 'Status',
            selector: (row: DeliveryPackage) => row.status,
            sortable: true,
            cell: (row: DeliveryPackage) => (
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
            selector: (row: DeliveryPackage) => row.pickupAddress || '',
            sortable: true
        },
        {
            name: 'Delivery Location',
            selector: (row: DeliveryPackage) => row.deliveryAddress || '',
            sortable: true
        },
        {
            name: 'Created At',
            selector: (row: DeliveryPackage) => row.createdAt,
            sortable: true,
            cell: (row: DeliveryPackage) => moment(row.createdAt).format('YYYY-MM-DD HH:mm')
        }
    ];

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
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Admin Dashboard</h1>
                <Link to="/admin/reports" className="d-none d-sm-inline-block btn btn-primary shadow-sm">
                    <FontAwesomeIcon icon={faDownload} className="fa-sm text-white-50 me-2" />
                    View Reports
                </Link>
            </div>

            <div className="row">
                <div className="col-xl-3 col-md-6 mb-4">
                    <div className="card border-left-primary shadow h-100 py-2">
                        <div className="card-body">
                            <div className="row no-gutters align-items-center">
                                <div className="col mr-2">
                                    <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                        Total Users
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.totalUsers}
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-users fa-2x text-gray-300"></i>
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
                                        Total Packages
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        {stats.totalPackages}
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
                                        Revenue
                                    </div>
                                    <div className="h5 mb-0 font-weight-bold text-gray-800">
                                        ${stats.revenue.toFixed(2)}
                                    </div>
                                </div>
                                <div className="col-auto">
                                    <i className="fas fa-dollar-sign fa-2x text-gray-300"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

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

export default AdminDashboard; 