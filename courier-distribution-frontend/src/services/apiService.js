import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

class ApiService {
    // Package related endpoints
    getPackages = async () => {
        const response = await axios.get(`${API_URL}/packages`);
        return response.data;
    };

    getPackageById = async (id) => {
        const response = await axios.get(`${API_URL}/packages/${id}`);
        return response.data;
    };

    createPackage = async (packageData) => {
        const response = await axios.post(`${API_URL}/packages`, packageData);
        return response.data;
    };

    updatePackage = async (id, packageData) => {
        const response = await axios.put(`${API_URL}/packages/${id}`, packageData);
        return response.data;
    };

    deletePackage = async (id) => {
        const response = await axios.delete(`${API_URL}/packages/${id}`);
        return response.data;
    };

    // Courier related endpoints
    getCourierDeliveries = async () => {
        const response = await axios.get(`${API_URL}/courier/deliveries`);
        return response.data;
    };

    acceptDelivery = async (packageId) => {
        const response = await axios.post(`${API_URL}/courier/deliveries/${packageId}/accept`);
        return response.data;
    };

    updateDeliveryStatus = async (packageId, status) => {
        const response = await axios.put(`${API_URL}/courier/deliveries/${packageId}/status`, { status });
        return response.data;
    };

    // Customer related endpoints
    getCustomerPackages = async () => {
        const response = await axios.get(`${API_URL}/customer/packages`);
        return response.data;
    };

    trackPackage = async (trackingNumber) => {
        const response = await axios.get(`${API_URL}/track/${trackingNumber}`);
        return response.data;
    };

    // Profile related endpoints
    updateProfile = async (profileData) => {
        const response = await axios.put(`${API_URL}/profile`, profileData);
        return response.data;
    };

    getProfile = async () => {
        const response = await axios.get(`${API_URL}/profile`);
        return response.data;
    };

    // Error handling wrapper
    handleError = (error) => {
        if (error.response) {
            // Server responded with error
            throw new Error(error.response.data);
        } else if (error.request) {
            // Request made but no response
            throw new Error('No response from server');
        } else {
            // Error in request setup
            throw new Error(error.message);
        }
    };
}

const apiService = new ApiService();
export default apiService; 