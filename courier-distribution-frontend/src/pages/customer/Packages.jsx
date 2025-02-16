import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Card, Table, Badge, Button, Form, InputGroup, Row, Col } from 'react-bootstrap';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Packages = () => {
    const [loading, setLoading] = useState(true);
    const [packages, setPackages] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filter, setFilter] = useState('all');
    const [dateRange, setDateRange] = useState({
        startDate: '',
        endDate: ''
    });

    useEffect(() => {
        fetchPackages();
    }, []);

    const fetchPackages = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/customer/packages`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setPackages(response.data);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to fetch packages');
            console.error('Packages fetch error:', error);
        } finally {
            setLoading(false);
        }
    };

    const cancelPackage = async (packageId) => {
        try {
            const token = localStorage.getItem('token');
            await axios.post(
                `${API_URL}/customer/cancel-package/${packageId}`,
                {},
                {
                    headers: { Authorization: `Bearer ${token}` }
                }
            );
            toast.success('Package cancelled successfully');
            fetchPackages();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to cancel package');
            console.error('Cancel package error:', error);
        }
    };

    const filteredPackages = packages
        .filter(pkg => {
            if (filter === 'all') return true;
            return pkg.status === filter;
        })
        .filter(pkg => {
            if (!searchTerm) return true;
            return (
                pkg.courierName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                pkg.pickupAddress.toLowerCase().includes(searchTerm.toLowerCase()) ||
                pkg.deliveryAddress.toLowerCase().includes(searchTerm.toLowerCase())
            );
        })
        .filter(pkg => {
            const packageDate = new Date(pkg.createdAt);
            const start = dateRange.startDate ? new Date(dateRange.startDate) : null;
            const end = dateRange.endDate ? new Date(dateRange.endDate) : null;

            if (start && end) {
                return packageDate >= start && packageDate <= end;
            } else if (start) {
                return packageDate >= start;
            } else if (end) {
                return packageDate <= end;
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
                <h1 className="h3 mb-0 text-gray-800">My Packages</h1>
                <Link to="/customer/new-package" className="d-none d-sm-inline-block btn btn-primary shadow-sm">
                    <i className="fas fa-plus fa-sm text-white-50 me-2"></i>
                    New Package
                </Link>
            </div>

            <Card className="shadow mb-4">
                <Card.Header className="py-3">
                    <h6 className="m-0 font-weight-bold text-primary mb-3">Package List</h6>
                    <Row className="align-items-center">
                        <Col md={4}>
                            <InputGroup>
                                <InputGroup.Text>
                                    <i className="fas fa-search"></i>
                                </InputGroup.Text>
                                <Form.Control
                                    type="text"
                                    placeholder="Search packages..."
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
                                <option value="PENDING">Pending</option>
                                <option value="PICKED_UP">Picked Up</option>
                                <option value="IN_TRANSIT">In Transit</option>
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
                    {filteredPackages.length === 0 ? (
                        <div className="text-center py-4">
                            <p className="text-gray-500 mb-0">No packages found</p>
                            <Link to="/customer/new-package" className="btn btn-primary mt-3">
                                Create New Package
                            </Link>
                        </div>
                    ) : (
                        <div className="table-responsive">
                            <Table className="table-bordered" width="100%" cellSpacing="0">
                                <thead>
                                    <tr>
                                        <th>Package ID</th>
                                        <th>Courier</th>
                                        <th>From</th>
                                        <th>To</th>
                                        <th>Status</th>
                                        <th>Created At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredPackages.map((pkg) => (
                                        <tr key={pkg.id}>
                                            <td>{pkg.id}</td>
                                            <td>{pkg.courierName || '-'}</td>
                                            <td>{pkg.pickupAddress}</td>
                                            <td>{pkg.deliveryAddress}</td>
                                            <td>
                                                <Badge bg={getStatusBadgeColor(pkg.status)}>
                                                    {pkg.status}
                                                </Badge>
                                            </td>
                                            <td>{new Date(pkg.createdAt).toLocaleString()}</td>
                                            <td>
                                                <div className="d-flex gap-2">
                                                    <Button
                                                        variant="primary"
                                                        size="sm"
                                                        as={Link}
                                                        to={`/customer/tracking/${pkg.id}`}
                                                    >
                                                        Track
                                                    </Button>
                                                    {pkg.status === 'PENDING' && (
                                                        <Button
                                                            variant="danger"
                                                            size="sm"
                                                            onClick={() => cancelPackage(pkg.id)}
                                                        >
                                                            Cancel
                                                        </Button>
                                                    )}
                                                </div>
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

export default Packages; 