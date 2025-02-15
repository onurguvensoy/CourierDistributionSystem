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
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    CircularProgress,
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import apiService from '../../services/apiService';
import websocketService from '../../services/websocketService';

const CustomerDashboard = () => {
    const [packages, setPackages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [openDialog, setOpenDialog] = useState(false);
    const [newPackage, setNewPackage] = useState({
        description: '',
        deliveryAddress: '',
        recipientName: '',
        recipientPhone: '',
    });

    useEffect(() => {
        loadPackages();
        setupWebSocket();

        return () => {
            websocketService.unsubscribe('/user/queue/packages');
        };
    }, []);

    const setupWebSocket = () => {
        websocketService.subscribe('/user/queue/packages', (data) => {
            if (data.type === 'PACKAGE_UPDATE') {
                setPackages(prevPackages =>
                    prevPackages.map(pkg =>
                        pkg.id === data.packageId
                            ? { ...pkg, status: data.status }
                            : pkg
                    )
                );
            }
        });
    };

    const loadPackages = async () => {
        try {
            const data = await apiService.getCustomerPackages();
            setPackages(data);
        } catch (error) {
            toast.error('Failed to load packages');
        } finally {
            setLoading(false);
        }
    };

    const handleCreatePackage = async () => {
        try {
            setLoading(true);
            const response = await apiService.createPackage(newPackage);
            setPackages([...packages, response]);
            toast.success('Package created successfully');
            handleCloseDialog();
        } catch (error) {
            toast.error(error.message || 'Failed to create package');
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewPackage(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleOpenDialog = () => {
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
        setNewPackage({
            description: '',
            deliveryAddress: '',
            recipientName: '',
            recipientPhone: '',
        });
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
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                        <Typography variant="h4">My Packages</Typography>
                        <Button
                            variant="contained"
                            color="primary"
                            startIcon={<AddIcon />}
                            onClick={handleOpenDialog}
                        >
                            New Package
                        </Button>
                    </Box>
                </Grid>
                <Grid item xs={12}>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Tracking Number</TableCell>
                                    <TableCell>Description</TableCell>
                                    <TableCell>Recipient</TableCell>
                                    <TableCell>Delivery Address</TableCell>
                                    <TableCell>Status</TableCell>
                                    <TableCell>Created At</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {packages.map((pkg) => (
                                    <TableRow key={pkg.id}>
                                        <TableCell>{pkg.trackingNumber}</TableCell>
                                        <TableCell>{pkg.description}</TableCell>
                                        <TableCell>
                                            {pkg.recipientName}
                                            <br />
                                            {pkg.recipientPhone}
                                        </TableCell>
                                        <TableCell>{pkg.deliveryAddress}</TableCell>
                                        <TableCell>
                                            <Box
                                                sx={{
                                                    backgroundColor: getStatusColor(pkg.status),
                                                    color: 'white',
                                                    padding: '4px 8px',
                                                    borderRadius: '4px',
                                                    display: 'inline-block',
                                                }}
                                            >
                                                {pkg.status}
                                            </Box>
                                        </TableCell>
                                        <TableCell>
                                            {new Date(pkg.createdAt).toLocaleString()}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Grid>
            </Grid>

            <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                <DialogTitle>Create New Package</DialogTitle>
                <DialogContent>
                    <Box sx={{ mt: 2 }}>
                        <TextField
                            fullWidth
                            label="Description"
                            name="description"
                            value={newPackage.description}
                            onChange={handleInputChange}
                            margin="normal"
                            required
                        />
                        <TextField
                            fullWidth
                            label="Delivery Address"
                            name="deliveryAddress"
                            value={newPackage.deliveryAddress}
                            onChange={handleInputChange}
                            margin="normal"
                            required
                        />
                        <TextField
                            fullWidth
                            label="Recipient Name"
                            name="recipientName"
                            value={newPackage.recipientName}
                            onChange={handleInputChange}
                            margin="normal"
                            required
                        />
                        <TextField
                            fullWidth
                            label="Recipient Phone"
                            name="recipientPhone"
                            value={newPackage.recipientPhone}
                            onChange={handleInputChange}
                            margin="normal"
                            required
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDialog}>Cancel</Button>
                    <Button
                        onClick={handleCreatePackage}
                        variant="contained"
                        color="primary"
                        disabled={loading}
                    >
                        Create
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
};

export default CustomerDashboard; 