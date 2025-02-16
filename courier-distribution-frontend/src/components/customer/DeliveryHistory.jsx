import React, { useState, useEffect } from 'react';
import { Card, Dropdown, Button, Modal } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { 
    faArrowLeft, faEllipsisV, faFileCsv, faFilePdf, 
    faInfoCircle, faUserCircle 
} from '@fortawesome/free-solid-svg-icons';
import DataTable from 'react-data-table-component';
import moment from 'moment';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import authService from '../../services/authService';

const DeliveryHistory = () => {
    const [completedPackages, setCompletedPackages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedPackage, setSelectedPackage] = useState(null);
    const [showDetailsModal, setShowDetailsModal] = useState(false);
    const userRole = authService.getRole();

    useEffect(() => {
        const fetchDeliveryHistory = async () => {
            try {
                const response = await authService.get(
                    userRole === 'COURIER' 
                        ? '/api/courier/deliveries/completed'
                        : '/api/customer/packages/completed'
                );
                setCompletedPackages(response.data);
            } catch (error) {
                console.error('Error fetching delivery history:', error);
                toast.error('Failed to load delivery history');
            } finally {
                setLoading(false);
            }
        };

        fetchDeliveryHistory();
    }, [userRole]);

    const viewDetails = async (packageId) => {
        try {
            const response = await authService.get(`/api/packages/${packageId}/details`);
            setSelectedPackage(response.data);
            setShowDetailsModal(true);
        } catch (error) {
            console.error('Error fetching package details:', error);
            toast.error('Failed to load package details');
        }
    };

    const exportData = async (format) => {
        try {
            const response = await authService.get(
                `/api/packages/export/${format}`,
                { responseType: 'blob' }
            );
            
            const blob = new Blob([response.data], {
                type: format === 'csv' 
                    ? 'text/csv' 
                    : 'application/pdf'
            });
            
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `delivery-history.${format}`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            
            toast.success(`Successfully exported delivery history as ${format.toUpperCase()}`);
        } catch (error) {
            console.error(`Error exporting ${format}:`, error);
            toast.error(`Failed to export as ${format.toUpperCase()}`);
        }
    };

    const columns = [
        {
            name: 'Package ID',
            selector: row => row.package_id,
            sortable: true
        },
        {
            name: 'Tracking Number',
            selector: row => row.trackingNumber,
            sortable: true,
            cell: row => (
                <span className="badge bg-secondary">{row.trackingNumber}</span>
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
            name: 'Courier',
            selector: row => row.courierUsername,
            sortable: true,
            cell: row => (
                <div className="d-flex align-items-center">
                    <FontAwesomeIcon icon={faUserCircle} className="text-gray-400 me-2" />
                    {row.courierUsername}
                </div>
            )
        },
        {
            name: 'Delivered At',
            selector: row => row.deliveryDate,
            sortable: true,
            cell: row => row.deliveryDate 
                ? moment(row.deliveryDate).format('YYYY-MM-DD HH:mm:ss')
                : 'N/A'
        },
        {
            name: 'Actions',
            cell: row => (
                <Button 
                    variant="info" 
                    size="sm"
                    onClick={() => viewDetails(row.package_id)}
                >
                    <FontAwesomeIcon icon={faInfoCircle} className="me-1" /> Details
                </Button>
            )
        }
    ];

    if (loading) {
        return <div className="text-center mt-5"><div className="spinner-border" /></div>;
    }

    return (
        <div className="container-fluid">
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Delivery History</h1>
                <Link 
                    to={userRole === 'COURIER' ? '/courier/dashboard' : '/customer/dashboard'} 
                    className="btn btn-secondary"
                >
                    <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
                    Back to {userRole === 'COURIER' ? 'Courier' : 'Customer'} Dashboard
                </Link>
            </div>

            <Card className="shadow mb-4">
                <Card.Header className="py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 className="m-0 font-weight-bold text-primary">
                        {userRole === 'COURIER' ? 'Your Completed Deliveries' : 'Your Package History'}
                    </h6>
                    <Dropdown>
                        <Dropdown.Toggle variant="link" className="no-arrow">
                            <FontAwesomeIcon icon={faEllipsisV} className="text-gray-400" />
                        </Dropdown.Toggle>
                        <Dropdown.Menu className="shadow dropdown-menu-right">
                            <Dropdown.Header>Export Options:</Dropdown.Header>
                            <Dropdown.Item onClick={() => exportData('csv')}>
                                <FontAwesomeIcon icon={faFileCsv} className="me-2 text-gray-400" />
                                Export CSV
                            </Dropdown.Item>
                            <Dropdown.Item onClick={() => exportData('pdf')}>
                                <FontAwesomeIcon icon={faFilePdf} className="me-2 text-gray-400" />
                                Export PDF
                            </Dropdown.Item>
                        </Dropdown.Menu>
                    </Dropdown>
                </Card.Header>
                <Card.Body>
                    <DataTable
                        columns={columns}
                        data={completedPackages}
                        pagination
                        responsive
                        highlightOnHover
                        striped
                        defaultSortFieldId={1}
                        defaultSortAsc={false}
                    />
                </Card.Body>
            </Card>

            <Modal show={showDetailsModal} onHide={() => setShowDetailsModal(false)} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>Package Details</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedPackage && (
                        <div>
                            <h6>Package Information</h6>
                            <p><strong>Package ID:</strong> {selectedPackage.package_id}</p>
                            <p><strong>Tracking Number:</strong> {selectedPackage.trackingNumber}</p>
                            <p><strong>Status:</strong> {selectedPackage.status}</p>
                            <p><strong>Weight:</strong> {selectedPackage.weight} kg</p>
                            
                            <h6 className="mt-4">Delivery Information</h6>
                            <p><strong>Pickup Address:</strong> {selectedPackage.pickupAddress}</p>
                            <p><strong>Delivery Address:</strong> {selectedPackage.deliveryAddress}</p>
                            <p><strong>Courier:</strong> {selectedPackage.courierUsername}</p>
                            <p><strong>Created At:</strong> {moment(selectedPackage.createdAt).format('YYYY-MM-DD HH:mm:ss')}</p>
                            <p><strong>Delivered At:</strong> {
                                selectedPackage.deliveryDate 
                                    ? moment(selectedPackage.deliveryDate).format('YYYY-MM-DD HH:mm:ss')
                                    : 'N/A'
                            }</p>
                            
                            {selectedPackage.specialInstructions && (
                                <>
                                    <h6 className="mt-4">Special Instructions</h6>
                                    <p>{selectedPackage.specialInstructions}</p>
                                </>
                            )}
                        </div>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDetailsModal(false)}>
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default DeliveryHistory; 