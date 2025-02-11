let stompClient = null;
let username = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 5000;

function connect() {
    username = document.getElementById('username').value;
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    const headers = {
        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
    };

    stompClient.connect(headers, onConnected, onError);
}

function onConnected() {
    console.log('WebSocket Connected!');
    reconnectAttempts = 0;

    // Subscribe to personal notifications
    stompClient.subscribe('/user/' + username + '/notification', onNotificationReceived);
    
    // Subscribe to package updates
    stompClient.subscribe('/user/' + username + '/queue/package-updates', onPackageUpdate);
    
    // Subscribe to location updates
    stompClient.subscribe('/user/' + username + '/queue/location-updates', onLocationUpdate);
    
    // Subscribe to rating prompts
    stompClient.subscribe('/user/' + username + '/queue/rating-prompts', onRatingPrompt);
    
    // Subscribe to general topics
    stompClient.subscribe('/topic/packages', onPackageBroadcast);
    stompClient.subscribe('/topic/status', onStatusUpdate);

    // Role-specific subscriptions
    if (isCustomer) {
        stompClient.subscribe('/topic/customer/' + username + '/package-updates', onCustomerPackageUpdate);
    } else if (isCourier) {
        stompClient.subscribe('/topic/courier/' + username + '/packages', onCourierPackageUpdate);
    }
}

function onError(error) {
    console.error('WebSocket Error:', error);
    
    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        console.log(`Reconnecting... Attempt ${reconnectAttempts + 1} of ${MAX_RECONNECT_ATTEMPTS}`);
        setTimeout(() => {
            reconnectAttempts++;
            connect();
        }, RECONNECT_DELAY);
    } else {
        console.error('Max reconnection attempts reached');
        showErrorToast('Connection lost. Please refresh the page.');
    }
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
        console.log("Disconnected");
    }
}

// Notification handlers
function onNotificationReceived(payload) {
    const notification = JSON.parse(payload.body);
    console.log('Notification received:', notification);
    
    showNotificationToast(notification.message);
    updateNotificationBadge();
    
    // Update notifications list if visible
    if (document.getElementById('notificationsContainer')) {
        loadNotifications();
    }
}

function onPackageUpdate(payload) {
    const update = JSON.parse(payload.body);
    console.log('Package update received:', update);
    
    // Update package status in UI
    const statusCell = document.querySelector(`#package-${update.packageId} .status`);
    if (statusCell) {
        statusCell.textContent = update.status;
        statusCell.className = `status badge bg-${getStatusColor(update.status)}`;
    }
    
    // Show notification
    showNotificationToast(`Package #${update.packageId} status: ${update.status}`);
    
    // Refresh data tables if they exist
    if (typeof refreshPackagesTable === 'function') {
        refreshPackagesTable();
    }
}

function onLocationUpdate(payload) {
    const update = JSON.parse(payload.body);
    console.log('Location update received:', update);
    
    // Update location on map if visible
    if (window.map && window.currentMarker) {
        const latLng = [update.latitude, update.longitude];
        window.currentMarker.setLatLng(latLng);
        window.map.setView(latLng, 15);
    }
    
    // Update location text if visible
    const locationCell = document.querySelector(`#package-${update.packageId} .location`);
    if (locationCell) {
        locationCell.textContent = update.location;
    }
}

function onRatingPrompt(payload) {
    const prompt = JSON.parse(payload.body);
    console.log('Rating prompt received:', prompt);
    
    // Show rating modal
    if (typeof showRatingModal === 'function') {
        showRatingModal(prompt.packageId);
    }
}

function onPackageBroadcast(payload) {
    const message = JSON.parse(payload.body);
    console.log('Package broadcast received:', message);
    
    if (message.type === 'NEW_PACKAGE' && isCourier) {
        showNotificationToast('New delivery package available!');
        if (typeof refreshAvailablePackages === 'function') {
            refreshAvailablePackages();
        }
    }
}

function onStatusUpdate(payload) {
    const update = JSON.parse(payload.body);
    console.log('Status update received:', update);
    
    // Update dashboard statistics if they exist
    if (typeof refreshDashboardStats === 'function') {
        refreshDashboardStats();
    }
}

function onCustomerPackageUpdate(payload) {
    const update = JSON.parse(payload.body);
    console.log('Customer package update received:', update);
    
    if (typeof refreshCustomerPackages === 'function') {
        refreshCustomerPackages();
    }
}

function onCourierPackageUpdate(payload) {
    const update = JSON.parse(payload.body);
    console.log('Courier package update received:', update);
    
    if (typeof refreshCourierPackages === 'function') {
        refreshCourierPackages();
    }
}

// Helper functions
function showNotificationToast(message) {
    toastr.info(message, null, {
        closeButton: true,
        progressBar: true,
        positionClass: "toast-top-right",
        timeOut: 5000
    });
}

function showErrorToast(message) {
    toastr.error(message, 'Error', {
        closeButton: true,
        progressBar: true,
        positionClass: "toast-top-right",
        timeOut: 0,
        extendedTimeOut: 0
    });
}

function getStatusColor(status) {
    switch(status) {
        case 'PENDING': return 'warning';
        case 'PICKED_UP': return 'info';
        case 'IN_TRANSIT': return 'primary';
        case 'DELIVERED': return 'success';
        case 'CANCELLED': return 'danger';
        default: return 'secondary';
    }
}

function updateNotificationBadge() {
    const badge = document.getElementById('notificationBadge');
    if (badge) {
        const currentCount = parseInt(badge.textContent || '0');
        badge.textContent = currentCount + 1;
        badge.style.display = 'inline';
    }
}

// Connect when the page loads
document.addEventListener('DOMContentLoaded', function() {
    connect();
});

// Cleanup on page unload
window.addEventListener('beforeunload', function() {
    disconnect();
}); 