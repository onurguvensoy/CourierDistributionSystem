import React, { useState, useEffect, useCallback } from 'react';
import { Card, Row, Col, Badge, Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowLeft } from '@fortawesome/free-solid-svg-icons';
import { Link, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import moment from 'moment';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { GoogleMap, LoadScript, Marker, DirectionsRenderer } from '@react-google-maps/api';
import authService from '../../services/authService';

const PackageTracking = () => {
    const { packageId } = useParams();
    const [trackingInfo, setTrackingInfo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [stompClient, setStompClient] = useState(null);
    const [courierLocation, setCourierLocation] = useState(null);
    const [directions, setDirections] = useState(null);

    const updateCourierLocation = useCallback((location) => {
        setCourierLocation(location);
        if (trackingInfo) {

            const directionsService = new window.google.maps.DirectionsService();
            directionsService.route(
                {
                    origin: location,
                    destination: { 
                        lat: parseFloat(trackingInfo.deliveryLatitude), 
                        lng: parseFloat(trackingInfo.deliveryLongitude) 
                    },
                    travelMode: window.google.maps.TravelMode.DRIVING,
                },
                (result, status) => {
                    if (status === window.google.maps.DirectionsStatus.OK) {
                        setDirections(result);
                    }
                }
            );
        }
    }, [trackingInfo]);

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
                console.log('Connected to WebSocket');
                
                // Subscribe to package status updates
                client.subscribe(`/topic/package/${packageId}/status`, (message) => {
                    try {
                        const update = JSON.parse(message.body);
                        setTrackingInfo(prev => ({
                            ...prev,
                            status: update.status,
                            lastUpdated: update.timestamp
                        }));
                        toast.info(`Package status updated to: ${update.status}`);
                    } catch (error) {
                        console.error('Error processing status update:', error);
                    }
                });

                // Subscribe to courier location updates
                client.subscribe(`/topic/package/${packageId}/location`, (message) => {
                    try {
                        const location = JSON.parse(message.body);
                        updateCourierLocation({
                            lat: parseFloat(location.latitude),
                            lng: parseFloat(location.longitude)
                        });
                    } catch (error) {
                        console.error('Error processing location update:', error);
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
    }, [packageId, updateCourierLocation]);

    useEffect(() => {
        const fetchTrackingInfo = async () => {
            try {
                const response = await authService.get(`/api/packages/${packageId}/tracking`);
                setTrackingInfo(response.data);
                
                // Initialize map with package locations
                if (response.data.status === 'IN_TRANSIT' && response.data.courierLatitude) {
                    updateCourierLocation({
                        lat: parseFloat(response.data.courierLatitude),
                        lng: parseFloat(response.data.courierLongitude)
                    });
                }
            } catch (error) {
                console.error('Error fetching tracking info:', error);
                setError('Failed to load package tracking information');
                toast.error('Failed to load tracking information');
            } finally {
                setLoading(false);
            }
        };

        fetchTrackingInfo();
        initializeWebSocket();

        return () => {
            if (stompClient) {
                stompClient.disconnect();
            }
        };
    }, [packageId, initializeWebSocket, updateCourierLocation]);

    const getStatusBadge = (status) => {
        const variants = {
            'PENDING': 'warning',
            'ASSIGNED': 'info',
            'PICKED_UP': 'primary',
            'IN_TRANSIT': 'info',
            'DELIVERED': 'success',
            'FAILED': 'danger'
        };
        return <Badge bg={variants[status] || 'secondary'}>{status}</Badge>;
    };

    if (loading) {
        return <div className="text-center mt-5"><div className="spinner-border" /></div>;
    }

    if (error) {
        return (
            <div className="container mt-4">
                <div className="alert alert-danger">{error}</div>
                <Link to="/customer/dashboard" className="btn btn-secondary">
                    <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
                    Back to Dashboard
                </Link>
            </div>
        );
    }

    const mapContainerStyle = {
        width: '100%',
        height: '400px'
    };

    const center = courierLocation || {
        lat: parseFloat(trackingInfo.pickupLatitude),
        lng: parseFloat(trackingInfo.pickupLongitude)
    };

    return (
        <div className="container-fluid">
            <div className="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 className="h3 mb-0 text-gray-800">Package Tracking</h1>
                <Link to="/customer/dashboard" className="btn btn-secondary">
                    <FontAwesomeIcon icon={faArrowLeft} className="me-2" />
                    Back to Dashboard
                </Link>
            </div>

            <Card className="shadow mb-4">
                <Card.Body>
                    <h5 className="card-title">Package Details</h5>
                    <Row>
                        <Col md={6}>
                            <p><strong>Package ID:</strong> {trackingInfo.id}</p>
                            <p>
                                <strong>Status:</strong>{' '}
                                {getStatusBadge(trackingInfo.status)}
                            </p>
                            <p>
                                <strong>Created:</strong>{' '}
                                {moment(trackingInfo.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                            </p>
                            <p><strong>Description:</strong> {trackingInfo.description}</p>
                        </Col>
                        <Col md={6}>
                            <p><strong>Pickup Address:</strong> {trackingInfo.pickupAddress}</p>
                            <p><strong>Delivery Address:</strong> {trackingInfo.deliveryAddress}</p>
                            {trackingInfo.courierName && (
                                <>
                                    <p><strong>Courier:</strong> {trackingInfo.courierName}</p>
                                    <p><strong>Courier Phone:</strong> {trackingInfo.courierPhone}</p>
                                </>
                            )}
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            <Card className="shadow">
                <Card.Body>
                    <h5 className="card-title">Live Tracking</h5>
                    <LoadScript googleMapsApiKey={process.env.REACT_APP_GOOGLE_MAPS_API_KEY}>
                        <GoogleMap
                            mapContainerStyle={mapContainerStyle}
                            center={center}
                            zoom={13}
                        >
                            {/* Pickup location marker */}
                            <Marker
                                position={{
                                    lat: parseFloat(trackingInfo.pickupLatitude),
                                    lng: parseFloat(trackingInfo.pickupLongitude)
                                }}
                                icon={{
                                    url: '/images/pickup-marker.png',
                                    scaledSize: new window.google.maps.Size(32, 32)
                                }}
                            />

                            {/* Delivery location marker */}
                            <Marker
                                position={{
                                    lat: parseFloat(trackingInfo.deliveryLatitude),
                                    lng: parseFloat(trackingInfo.deliveryLongitude)
                                }}
                                icon={{
                                    url: '/images/delivery-marker.png',
                                    scaledSize: new window.google.maps.Size(32, 32)
                                }}
                            />

                            {/* Courier location marker */}
                            {courierLocation && (
                                <Marker
                                    position={courierLocation}
                                    icon={{
                                        url: '/images/courier-marker.png',
                                        scaledSize: new window.google.maps.Size(32, 32)
                                    }}
                                />
                            )}

                            {/* Route display */}
                            {directions && (
                                <DirectionsRenderer
                                    directions={directions}
                                    options={{
                                        suppressMarkers: true,
                                        polylineOptions: {
                                            strokeColor: '#4e73df',
                                            strokeWeight: 4
                                        }
                                    }}
                                />
                            )}
                        </GoogleMap>
                    </LoadScript>
                </Card.Body>
            </Card>
        </div>
    );
};

export default PackageTracking; 