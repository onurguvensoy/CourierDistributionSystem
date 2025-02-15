import React, { useState, useEffect } from 'react';
import {
    Container,
    Grid,
    Paper,
    Typography,
    Button,
    Box,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    CircularProgress,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Tabs,
    Tab,
} from '@mui/material';
import { LocalShipping, Done, Error } from '@mui/icons-material';
import { toast } from 'react-toastify';
import apiService from '../../services/apiService';
import websocketService from '../../services/websocketService';

const CourierDashboard = () => {
    const [activeDeliveries, setActiveDeliveries] = useState([]);
    const [availablePackages, setAvailablePackages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedPackage, setSelectedPackage] = useState(null);
    const [statusDialog, setStatusDialog] = useState(false);
    const [newStatus, setNewStatus] = useState('');
    const [activeTab, setActiveTab] = useState(0);

    useEffect(() => {
        loadDeliveries();
        setupWebSocket();

        return () => {
            websocketService.unsubscribe('/user/queue/deliveries');
            websocketService.unsubscribe('/user/queue/available-packages');
        };
    }, []);

    const setupWebSocket = () => {
        websocketService.subscribe('/user/queue/deliveries', (data) => {
            if (data.type === 'DELIVERY_UPDATE') {
                setActiveDeliveries(prevDeliveries =>
                    prevDeliveries.map(delivery =>
                        delivery.id === data.deliveryId
                            ? { ...delivery, status: data.status }
                            : delivery
                    )
                );
            }
        });

        websocketService.subscribe('/user/queue/available-packages', (data) => {
            if (data.type === 'NEW_PACKAGE') {
                setAvailablePackages(prev => [...prev, data.package]);
            } else if (data.type === 'PACKAGE_TAKEN') {
                setAvailablePackages(prev =>
                    prev.filter(pkg => pkg.id !== data.packageId)
                );
            }
        });
    };

    const loadDeliveries = async () => {
        try {
            const [activeDeliveriesData, availablePackagesData] = await Promise.all([
                apiService.getCourierDeliveries(),
                apiService.getPackages()
            ]);
            setActiveDeliveries(activeDeliveriesData);
            setAvailablePackages(availablePackagesData.filter(pkg => pkg.status === 'PENDING'));
        } catch (error) {
            toast.error('Failed to load deliveries');
        } finally {
            setLoading(false);
        }
    };

    const handleAcceptDelivery = async (packageId) => {
        try {
            await apiService.acceptDelivery(packageId);
            const updatedPackage = await apiService.getPackageById(packageId);
            setActiveDeliveries(prev => [...prev, updatedPackage]);
            setAvailablePackages(prev => prev.filter(pkg => pkg.id !== packageId));
            toast.success('Delivery accepted successfully');
        } catch (error) {
            toast.error(error.message || 'Failed to accept delivery');
        }
    };

    const handleStatusUpdate = async () => {
        if (!selectedPackage || !newStatus) return;

        try {
            await apiService.updateDeliveryStatus(selectedPackage.id, newStatus);
            setActiveDeliveries(prev =>
                prev.map(delivery =>
                    delivery.id === selectedPackage.id
                        ? { ...delivery, status: newStatus }
                        : delivery
                )
            );
            toast.success('Status updated successfully');
            handleCloseStatusDialog();
        } catch (error) {
            toast.error(error.message || 'Failed to update status');
        }
    };

    const handleOpenStatusDialog = (pkg) => {
        setSelectedPackage(pkg);
        setStatusDialog(true);
    };

    const handleCloseStatusDialog = () => {
        setStatusDialog(false);
        setSelectedPackage(null);
        setNewStatus('');
    };

    const handleTabChange = (event, newValue) => {
        setActiveTab(newValue);
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'PENDING':
                return '#ffa726';
            case 'PICKED_UP':
                return '#42a5f5';
            case 'IN_TRANSIT':
                return '#7e57c2';
            case 'DELIVERED':
                return '#66bb6a';
            case 'FAILED':
                return '#ef5350';
            default:
                return '#757575';
        }
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Grid container spacing={3}>
                <Grid item xs={12}>
                    <Paper sx={{ p: 2 }}>
                        <Tabs value={activeTab} onChange={handleTabChange} centered>
                            <Tab label="Active Deliveries" />
                            <Tab label="Available Packages" />
                        </Tabs>
                    </Paper>
                </Grid>

                <Grid item xs={12}>
                    {activeTab === 0 ? (
                        <TableContainer component={Paper}>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Tracking Number</TableCell>
                                        <TableCell>Description</TableCell>
                                        <TableCell>Delivery Address</TableCell>
                                        <TableCell>Status</TableCell>
                                        <TableCell>Actions</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {activeDeliveries.map((delivery) => (
                                        <TableRow key={delivery.id}>
                                            <TableCell>{delivery.trackingNumber}</TableCell>
                                            <TableCell>{delivery.description}</TableCell>
                                            <TableCell>{delivery.deliveryAddress}</TableCell>
                                            <TableCell>
                                                <Box
                                                    sx={{
                                                        backgroundColor: getStatusColor(delivery.status),
                                                        color: 'white',
                                                        padding: '4px 8px',
                                                        borderRadius: '4px',
                                                        display: 'inline-block',
                                                    }}
                                                >
                                                    {delivery.status}
                                                </Box>
                                            </TableCell>
                                            <TableCell>
                                                <Button
                                                    variant="contained"
                                                    color="primary"
                                                    onClick={() => handleOpenStatusDialog(delivery)}
                                                    startIcon={<LocalShipping />}
                                                >
                                                    Update Status
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    ) : (
                        <TableContainer component={Paper}>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Tracking Number</TableCell>
                                        <TableCell>Description</TableCell>
                                        <TableCell>Delivery Address</TableCell>
                                        <TableCell>Actions</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {availablePackages.map((pkg) => (
                                        <TableRow key={pkg.id}>
                                            <TableCell>{pkg.trackingNumber}</TableCell>
                                            <TableCell>{pkg.description}</TableCell>
                                            <TableCell>{pkg.deliveryAddress}</TableCell>
                                            <TableCell>
                                                <Button
                                                    variant="contained"
                                                    color="primary"
                                                    onClick={() => handleAcceptDelivery(pkg.id)}
                                                >
                                                    Accept Delivery
                                                </Button>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    )}
                </Grid>
            </Grid>

            <Dialog open={statusDialog} onClose={handleCloseStatusDialog}>
                <DialogTitle>Update Delivery Status</DialogTitle>
                <DialogContent>
                    <FormControl fullWidth sx={{ mt: 2 }}>
                        <InputLabel>Status</InputLabel>
                        <Select
                            value={newStatus}
                            label="Status"
                            onChange={(e) => setNewStatus(e.target.value)}
                        >
                            <MenuItem value="PICKED_UP">Picked Up</MenuItem>
                            <MenuItem value="IN_TRANSIT">In Transit</MenuItem>
                            <MenuItem value="DELIVERED">Delivered</MenuItem>
                            <MenuItem value="FAILED">Failed</MenuItem>
                        </Select>
                    </FormControl>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseStatusDialog}>Cancel</Button>
                    <Button
                        onClick={handleStatusUpdate}
                        variant="contained"
                        color="primary"
                        disabled={!newStatus}
                    >
                        Update
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
};

export default CourierDashboard; 