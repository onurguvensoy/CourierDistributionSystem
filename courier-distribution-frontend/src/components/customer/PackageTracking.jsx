import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import apiService from '../../services/apiService';
import { toast } from 'react-toastify';

const PackageTracking = () => {
    const { packageId } = useParams();
    const [loading, setLoading] = useState(true);
    const [packageData, setPackageData] = useState(null);
    const [trackingHistory, setTrackingHistory] = useState([]);

    const fetchPackageData = useCallback(async () => {
        try {
            const response = await apiService.trackPackage(packageId);
            setPackageData(response.packageData);
            setTrackingHistory(response.trackingHistory);
        } catch (error) {
            toast.error(error.message || 'Failed to load tracking information');
        } finally {
            setLoading(false);
        }
    }, [packageId]);

    useEffect(() => {
        if (packageId) {
            fetchPackageData();
        }
    }, [packageId, fetchPackageData]);

    const getStatusColor = (status) => {
        const colors = {
            PENDING: 'warning',
            IN_TRANSIT: 'info',
            DELIVERED: 'success',
            FAILED: 'danger'
        };
        return colors[status] || 'secondary';
    };

    if (loading) {
        return (
            <div className="container-fluid">
                <div className="text-center mt-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="sr-only">Loading...</span>
                    </div>
                </div>
            </div>
        );
    }

    if (!packageData) {
        return (
            <div className="container-fluid">
                <div className="text-center mt-5">
                    <h3 className="text-gray-800">Package Not Found</h3>
                    <p>The package you're looking for doesn't exist or you don't have permission to view it.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="container-fluid">
            <h1 className="h3 mb-4 text-gray-800">Package Tracking</h1>

            {/* Package Information Card */}
            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Package Information</h6>
                </div>
                <div className="card-body">
                    <div className="row">
                        <div className="col-md-6">
                            <p>
                                <strong>Package ID:</strong> {packageData.packageId}
                            </p>
                            <p>
                                <strong>Status:</strong>{' '}
                                <span className={`badge badge-${getStatusColor(packageData.status)}`}>
                                    {packageData.status}
                                </span>
                            </p>
                            <p>
                                <strong>Created Date:</strong>{' '}
                                {new Date(packageData.createdAt).toLocaleDateString()}
                            </p>
                        </div>
                        <div className="col-md-6">
                            <p>
                                <strong>Delivery Address:</strong> {packageData.deliveryAddress}
                            </p>
                            <p>
                                <strong>Recipient:</strong> {packageData.recipientName}
                            </p>
                            <p>
                                <strong>Estimated Delivery:</strong>{' '}
                                {packageData.estimatedDelivery
                                    ? new Date(packageData.estimatedDelivery).toLocaleDateString()
                                    : 'Not available'}
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Tracking Timeline */}
            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Tracking History</h6>
                </div>
                <div className="card-body">
                    <div className="timeline">
                        {trackingHistory.map((event, index) => (
                            <div key={index} className="timeline-item">
                                <div className="timeline-item-content">
                                    <span className="tag" style={{ background: getStatusColor(event.status) }}>
                                        {event.status}
                                    </span>
                                    <time>{new Date(event.timestamp).toLocaleString()}</time>
                                    <p>{event.description}</p>
                                    {event.location && (
                                        <p>
                                            <strong>Location:</strong> {event.location}
                                        </p>
                                    )}
                                    <span className="circle" />
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PackageTracking; 