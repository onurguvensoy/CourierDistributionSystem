// Customer-specific WebSocket functionality
const isCustomer = true;
const isCourier = false;

// Extend base WebSocket functionality
function refreshCustomerPackages() {
    if ($.fn.DataTable.isDataTable('#customerPackagesTable')) {
        $('#customerPackagesTable').DataTable().ajax.reload(null, false);
    }
}

function loadPackageDetails(packageId) {
    $.get(`/api/packages/${packageId}`, function(data) {
        $('#packageDetailsId').text(data.id);
        $('#packageDetailsStatus').text(data.status);
        $('#packageDetailsOrigin').text(data.pickupLocation);
        $('#packageDetailsDestination').text(data.deliveryLocation);
        
        if (data.courier) {
            $('#courierDetailsSection').show();
            $('#packageDetailsCourier').text(data.courier.username);
            $('#packageDetailsCourierPhone').text(data.courier.phoneNumber);
        } else {
            $('#courierDetailsSection').hide();
        }
        
        if (data.currentLocation) {
            updateMapMarker(data.currentLocation.latitude, data.currentLocation.longitude);
        }
    });
}

function updateMapMarker(lat, lng) {
    if (currentMarker) {
        currentMarker.remove();
    }
    
    const latLng = [lat, lng];
    currentMarker = L.marker(latLng).addTo(map);
    map.setView(latLng, 15);
}

function submitRating() {
    const packageId = $('#ratingPackageId').val();
    const rating = $('input[name="rating"]:checked').val();
    const comment = $('#ratingComment').val();
    
    $.ajax({
        url: '/api/ratings',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            packageId: packageId,
            rating: rating,
            comment: comment
        }),
        success: function(response) {
            $('#ratingModal').modal('hide');
            showNotificationToast('Thank you for your rating!');
            refreshCustomerPackages();
        },
        error: function(xhr) {
            showErrorToast('Failed to submit rating. Please try again.');
        }
    });
}

// Custom notification handlers for customer-specific updates
function onCustomerPackageUpdate(payload) {
    const update = JSON.parse(payload.body);
    console.log('Customer package update received:', update);
    
    // Update package status in UI
    const statusCell = document.querySelector(`#package-${update.packageId} .status`);
    if (statusCell) {
        statusCell.textContent = update.status;
        statusCell.className = `status badge bg-${getStatusColor(update.status)}`;
    }
    
    // Show notification with more detailed message for customers
    let message = `Your package #${update.packageId} `;
    switch(update.type) {
        case 'STATUS_UPDATE':
            message += `status has been updated to: ${update.status}`;
            break;
        case 'COURIER_ASSIGNED':
            message += `has been assigned to courier: ${update.courierName}`;
            break;
        case 'LOCATION_UPDATE':
            message += `is now at: ${update.location}`;
            break;
        default:
            message += `has been updated`;
    }
    
    showNotificationToast(message);
    refreshCustomerPackages();
    
    // If tracking modal is open, update package details
    if ($('#trackingModal').is(':visible')) {
        loadPackageDetails(update.packageId);
    }
}

// Initialize map when tracking modal is shown
$('#trackingModal').on('shown.bs.modal', function() {
    if (!map) {
        initializeMap();
    }
    map.invalidateSize();
}); 