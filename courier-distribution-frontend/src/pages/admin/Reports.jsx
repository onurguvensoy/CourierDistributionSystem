import React, { useState, useEffect } from 'react';
import { Card, Button, Modal } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPrint, faEye, faTrash } from '@fortawesome/free-solid-svg-icons';
import DataTable from 'react-data-table-component';
import moment from 'moment';
import { toast } from 'react-toastify';
import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const Reports = () => {
    const [loading, setLoading] = useState(true);
    const [reports, setReports] = useState({
        deliveryStats: {
            total: 0,
            completed: 0,
            inProgress: 0,
            cancelled: 0
        },
        revenueStats: {
            total: 0,
            monthly: 0,
            weekly: 0,
            daily: 0
        },
        courierStats: {
            total: 0,
            active: 0,
            topPerformers: []
        }
    });
    const [showModal, setShowModal] = useState(false);
    const [selectedReport, setSelectedReport] = useState(null);

    useEffect(() => {
        fetchReports();
    }, []);

    const fetchReports = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/admin/reports`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setReports(response.data);
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to fetch reports');
            console.error('Reports fetch error:', error);
        } finally {
            setLoading(false);
        }
    };

    const viewReport = async (reportId) => {
        try {
            const response = await axios.get(`${API_URL}/delivery-reports/${reportId}`);
            if (response.data) {
                setSelectedReport(response.data);
                setShowModal(true);
            }
        } catch (error) {
            console.error('Error loading report details:', error);
            toast.error('Failed to load report details');
        }
    };

    const deleteReport = async (reportId) => {
        if (window.confirm('Are you sure you want to delete this report?')) {
            try {
                await axios.delete(`${API_URL}/delivery-reports/${reportId}`);
                toast.success('Report deleted successfully');
                fetchReports();
            } catch (error) {
                console.error('Error deleting report:', error);
                toast.error('Failed to delete report');
            }
        }
    };

    const printReport = (report) => {
        const printWindow = window.open('', '', 'height=600,width=800');
        printWindow.document.write('<html><head><title>Delivery Report</title>');
        printWindow.document.write('<style>body { font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; }</style>');
        printWindow.document.write('</head><body>');
        printWindow.document.write('<pre>' + report.content + '</pre>');
        printWindow.document.write('</body></html>');
        printWindow.document.close();
        printWindow.print();
    };

    const printAllReports = () => {
        const printWindow = window.open('', '', 'height=600,width=800');
        printWindow.document.write('<html><head><title>All Delivery Reports</title>');
        printWindow.document.write('<style>');
        printWindow.document.write('body { font-family: Arial, sans-serif; }');
        printWindow.document.write('table { border-collapse: collapse; width: 100%; }');
        printWindow.document.write('th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }');
        printWindow.document.write('th { background-color: #f2f2f2; }');
        printWindow.document.write('</style></head><body>');
        printWindow.document.write('<h2>Delivery Reports Summary</h2>');
        
        // Create table HTML
        let tableHtml = '<table><thead><tr>';
        columns.forEach(col => {
            if (col.name !== 'Actions') {
                tableHtml += `<th>${col.name}</th>`;
            }
        });
        tableHtml += '</tr></thead><tbody>';
        
        reports.forEach(report => {
            tableHtml += '<tr>';
            tableHtml += `<td>${report.id}</td>`;
            tableHtml += `<td>${report.deliveryPackage.id}</td>`;
            tableHtml += `<td>${report.courier.username}</td>`;
            tableHtml += `<td>${report.deliveryPackage.customer.username}</td>`;
            tableHtml += `<td>${moment(report.deliveryTime).format('YYYY-MM-DD HH:mm:ss')}</td>`;
            tableHtml += `<td>${report.distanceTraveled} km</td>`;
            tableHtml += `<td>${report.status}</td>`;
            tableHtml += '</tr>';
        });
        
        tableHtml += '</tbody></table>';
        printWindow.document.write(tableHtml);
        printWindow.document.write('</body></html>');
        printWindow.document.close();
        printWindow.print();
    };

    const columns = [
        {
            name: 'Report ID',
            selector: row => row.id,
            sortable: true
        },
        {
            name: 'Package ID',
            selector: row => row.deliveryPackage.id,
            sortable: true
        },
        {
            name: 'Courier',
            selector: row => row.courier.username,
            sortable: true
        },
        {
            name: 'Customer',
            selector: row => row.deliveryPackage.customer.username,
            sortable: true
        },
        {
            name: 'Delivery Time',
            selector: row => row.deliveryTime,
            sortable: true,
            cell: row => moment(row.deliveryTime).format('YYYY-MM-DD HH:mm:ss')
        },
        {
            name: 'Distance',
            selector: row => row.distanceTraveled,
            sortable: true,
            cell: row => `${row.distanceTraveled} km`
        },
        {
            name: 'Status',
            selector: row => row.status,
            sortable: true,
            cell: row => (
                <span className="badge bg-success">{row.status}</span>
            )
        },
        {
            name: 'Actions',
            cell: row => (
                <div className="d-flex gap-2">
                    <Button variant="info" size="sm" onClick={() => viewReport(row.id)}>
                        <FontAwesomeIcon icon={faEye} /> View
                    </Button>
                    <Button variant="primary" size="sm" onClick={() => printReport(row)}>
                        <FontAwesomeIcon icon={faPrint} /> Print
                    </Button>
                    <Button variant="danger" size="sm" onClick={() => deleteReport(row.id)}>
                        <FontAwesomeIcon icon={faTrash} /> Delete
                    </Button>
                </div>
            )
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
            <h1 className="h3 mb-4 text-gray-800">Reports</h1>

            <div className="row">
                <div className="col-lg-4">
                    <div className="card shadow mb-4">
                        <div className="card-header py-3">
                            <h6 className="m-0 font-weight-bold text-primary">Delivery Statistics</h6>
                        </div>
                        <div className="card-body">
                            <div className="mb-3">
                                <div className="small text-gray-500">Total Deliveries</div>
                                <div className="h5">{reports.deliveryStats.total}</div>
                            </div>
                            <div className="mb-3">
                                <div className="small text-gray-500">Completed Deliveries</div>
                                <div className="h5">{reports.deliveryStats.completed}</div>
                            </div>
                            <div className="mb-3">
                                <div className="small text-gray-500">In Progress</div>
                                <div className="h5">{reports.deliveryStats.inProgress}</div>
                            </div>
                            <div className="mb-3">
                                <div className="small text-gray-500">Cancelled</div>
                                <div className="h5">{reports.deliveryStats.cancelled}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="col-lg-4">
                    <div className="card shadow mb-4">
                        <div className="card-header py-3">
                            <h6 className="m-0 font-weight-bold text-primary">Revenue Statistics</h6>
                        </div>
                        <div className="card-body">
                            <div className="mb-3">
                                <div className="small text-gray-500">Total Revenue</div>
                                <div className="h5">${reports.revenueStats.total.toFixed(2)}</div>
                            </div>
                            <div className="mb-3">
                                <div className="small text-gray-500">Monthly Revenue</div>
                                <div className="h5">${reports.revenueStats.monthly.toFixed(2)}</div>
                            </div>
                            <div className="mb-3">
                                <div className="small text-gray-500">Weekly Revenue</div>
                                <div className="h5">${reports.revenueStats.weekly.toFixed(2)}</div>
                            </div>
                            <div className="mb-3">
                                <div className="small text-gray-500">Daily Revenue</div>
                                <div className="h5">${reports.revenueStats.daily.toFixed(2)}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="col-lg-4">
                    <div className="card shadow mb-4">
                        <div className="card-header py-3">
                            <h6 className="m-0 font-weight-bold text-primary">Courier Statistics</h6>
                        </div>
                        <div className="card-body">
                            <div className="mb-3">
                                <div className="small text-gray-500">Total Couriers</div>
                                <div className="h5">{reports.courierStats.total}</div>
                            </div>
                            <div className="mb-3">
                                <div className="small text-gray-500">Active Couriers</div>
                                <div className="h5">{reports.courierStats.active}</div>
                            </div>
                            <div className="mt-4">
                                <h6 className="font-weight-bold">Top Performing Couriers</h6>
                                <div className="list-group">
                                    {reports.courierStats.topPerformers.map((courier, index) => (
                                        <div key={index} className="list-group-item">
                                            <div className="d-flex w-100 justify-content-between">
                                                <h6 className="mb-1">{courier.name}</h6>
                                                <small>{courier.deliveries} deliveries</small>
                                            </div>
                                            <small className="text-muted">Rating: {courier.rating}/5</small>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <Card className="shadow mb-4">
                <Card.Header className="py-3 d-flex justify-content-between align-items-center">
                    <h6 className="m-0 font-weight-bold text-primary">All Delivery Reports</h6>
                    <Button variant="primary" size="sm" onClick={printAllReports}>
                        <FontAwesomeIcon icon={faPrint} className="mr-2" /> Print All Reports
                    </Button>
                </Card.Header>
                <Card.Body>
                    <DataTable
                        columns={columns}
                        data={reports}
                        pagination
                        responsive
                        highlightOnHover
                        striped
                        defaultSortFieldId={1}
                        defaultSortAsc={false}
                    />
                </Card.Body>
            </Card>

            <Modal show={showModal} onHide={() => setShowModal(false)} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>Report Details</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <pre className="bg-light p-3" style={{ whiteSpace: 'pre-wrap' }}>
                        {selectedReport?.content}
                    </pre>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowModal(false)}>
                        Close
                    </Button>
                    <Button variant="primary" onClick={() => printReport(selectedReport)}>
                        Print Report
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default Reports; 