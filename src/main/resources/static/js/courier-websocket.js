// Courier-specific WebSocket functionality
const isCustomer = false;
const isCourier = true;

// Extend base WebSocket functionality
function refreshCourierPackages() {
    if ($.fn.DataTable.isDataTable('#courierPackagesTable')) {
        $('#courierPackagesTable').DataTable().ajax.reload(null, false);
    }
}

function refreshAvailablePackages() {
    if ($.fn.DataTable.isDataTable('#availablePackagesTable')) {
        $('#availablePackagesTable').DataTable().ajax.reload(null, false);
    }
}

function updatePackageLocation(packageId, latitude, longitude, location) {
    $.ajax({
        url: `/api/packages/${packageId}/location`,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            latitude: latitude,
            longitude: longitude,
            location: location
        }),
        success: function(response) {
            $('#locationModal').modal('hide');
            showNotificationToast('Location updated successfully');
            refreshCourierPackages();
        },
        error: function(xhr) {
            showErrorToast('Failed to update location. Please try again.');
        }
    });
}

function updatePackageStatus(packageId, status) {
    $.ajax({
        url: `/api/packages/${packageId}/status`,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            status: status
        }),
        success: function(response) {
            $('#statusModal').modal('hide');
            showNotificationToast('Status updated successfully');
            refreshCourierPackages();
        },
        error: function(xhr) {
            showErrorToast('Failed to update status. Please try again.');
        }
    });
}

function acceptPackage(packageId) {
    $.ajax({
        url: `/api/courier/packages/${packageId}/accept`,
        type: 'POST',
        success: function(response) {
            showNotificationToast('Package accepted successfully');
            refreshAvailablePackages();
            refreshCourierPackages();
        },
        error: function(xhr) {
            showErrorToast('Failed to accept package. Please try again.');
        }
    });
}

function dropPackage(packageId) {
    if (confirm('Are you sure you want to drop this package?')) {
        $.ajax({
            url: `/api/courier/packages/${packageId}/drop`,
            type: 'POST',
            success: function(response) {
                showNotificationToast('Package dropped successfully');
                refreshCourierPackages();
            },
            error: function(xhr) {
                showErrorToast('Failed to drop package. Please try again.');
            }
        });
    }
}

// Custom notification handlers for courier-specific updates
function onCourierPackageUpdate(payload) {
    const update = JSON.parse(payload.body);
    console.log('Courier package update received:', update);
    
    // Update package status in UI
    const statusCell = document.querySelector(`#package-${update.packageId} .status`);
    if (statusCell) {
        statusCell.textContent = update.status;
        statusCell.className = `status badge bg-${getStatusColor(update.status)}`;
    }
    
    // Show notification with more detailed message for couriers
    let message = `Package #${update.packageId} `;
    switch(update.type) {
        case 'NEW_ASSIGNMENT':
            message += 'has been assigned to you';
            break;
        case 'STATUS_UPDATE':
            message += `status has been updated to: ${update.status}`;
            break;
        case 'CUSTOMER_RATING':
            message += `has received a ${update.rating}-star rating`;
            break;
        default:
            message += 'has been updated';
    }
    
    showNotificationToast(message);
    refreshCourierPackages();
}

// Location tracking
let watchId = null;

function startLocationTracking(packageId) {
    if ("geolocation" in navigator) {
        const options = {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
        };
        
        watchId = navigator.geolocation.watchPosition(
            position => updateDeliveryLocation(packageId, position),
            error => console.error("Error getting location:", error),
            options
        );
    } else {
        showErrorToast('Geolocation is not supported by your browser');
    }
}

function stopLocationTracking() {
    if (watchId !== null) {
        navigator.geolocation.clearWatch(watchId);
        watchId = null;
    }
}

function updateDeliveryLocation(packageId, position) {
    const { latitude, longitude } = position.coords;
    
    // Get address from coordinates using reverse geocoding
    $.get(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}`, function(data) {
        const location = data.display_name;
        updatePackageLocation(packageId, latitude, longitude, location);
    });
}

// Initialize location tracking for active deliveries
document.addEventListener('DOMContentLoaded', function() {
    const activeDeliveries = document.querySelectorAll('.package-row[data-status="IN_TRANSIT"]');
    activeDeliveries.forEach(delivery => {
        const packageId = delivery.dataset.packageId;
        startLocationTracking(packageId);
    });
}); 