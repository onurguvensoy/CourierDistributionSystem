import React, { useState, useEffect } from 'react';
import { Card, Button, Modal } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPrint, faEye, faTrash } from '@fortawesome/free-solid-svg-icons';
import DataTable from 'react-data-table-component';
import moment from 'moment';
import { toast } from 'react-toastify';
import authService from '../../services/authService';

const Reports = () => {
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [selectedReport, setSelectedReport] = useState(null);

    useEffect(() => {
        fetchReports();
    }, []);

    const fetchReports = async () => {
        try {
            const response = await authService.get('/api/delivery-reports');
            setReports(response.data);
        } catch (error) {
            console.error('Error fetching reports:', error);
            toast.error('Failed to load reports');
        } finally {
            setLoading(false);
        }
    };

    const viewReport = async (reportId) => {
        try {
            const response = await authService.get(`/api/delivery-reports/${reportId}`);
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
                await authService.delete(`/api/delivery-reports/${reportId}`);
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
        return <div className="text-center mt-5"><div className="spinner-border" /></div>;
    }

    return (
        <div className="container-fluid">
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Delivery Reports</h1>
                <Button variant="primary" onClick={printAllReports}>
                    <FontAwesomeIcon icon={faPrint} className="me-2" /> Print All Reports
                </Button>
            </div>

            <Card className="shadow mb-4">
                <Card.Header className="py-3">
                    <h6 className="m-0 font-weight-bold text-primary">All Delivery Reports</h6>
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