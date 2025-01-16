<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Courier Dashboard - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://maps.googleapis.com/maps/api/js?key=${googleMapsApiKey}&libraries=places"></script>
    <style>
        body {
            background-color: #f8f9fa;
            padding-top: 20px;
        }
        .dashboard-container {
            background-color: white;
            border-radius: 5px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            padding: 20px;
            margin-bottom: 20px;
        }
        .nav-pills .nav-link {
            color: #495057;
        }
        .nav-pills .nav-link.active {
            background-color: #0d6efd;
            color: white;
        }
        #map {
            height: 400px;
            width: 100%;
            border-radius: 5px;
            margin-top: 20px;
        }
        .map-container {
            position: relative;
        }
        .package-marker {
            cursor: pointer;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="dashboard-container">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Courier Dashboard</h2>
                <div>
                    <span class="me-3">Welcome, ${user.username}</span>
                    <a href="/auth/logout" class="btn btn-outline-danger">Logout</a>
                </div>
            </div>
        </div>

        <!-- Main Content -->
        <div class="row">
            <!-- Sidebar -->
            <div class="col-md-3">
                <div class="dashboard-container">
                    <div class="nav flex-column nav-pills">
                        <a class="nav-link active" href="#overview" data-bs-toggle="pill">Overview</a>
                        <a class="nav-link" href="#available-packages" data-bs-toggle="pill">Available Packages</a>
                        <a class="nav-link" href="#active-deliveries" data-bs-toggle="pill">Active Deliveries</a>
                        <a class="nav-link" href="#delivery-history" data-bs-toggle="pill">Delivery History</a>
                        <a class="nav-link" href="#availability" data-bs-toggle="pill">Set Availability</a>
                        <a class="nav-link" href="#ratings" data-bs-toggle="pill">My Ratings</a>
                        <a class="nav-link" href="#profile" data-bs-toggle="pill">Profile</a>
                    </div>
                </div>
            </div>

            <!-- Content Area -->
            <div class="col-md-9">
                <div class="dashboard-container">
                    <div class="tab-content">
                        <!-- Overview Tab -->
                        <div class="tab-pane fade show active" id="overview">
                            <h3>Overview</h3>
                            <div class="row mt-4">
                                <div class="col-md-4">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Active Deliveries</h5>
                                            <p class="card-text display-4">0</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Today's Earnings</h5>
                                            <p class="card-text display-4">$0</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Rating</h5>
                                            <p class="card-text display-4">${user.averageRating}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Available Packages Tab -->
                        <div class="tab-pane fade" id="available-packages">
                            <h3>Available Packages</h3>
                            
                            <!-- Map View -->
                            <div class="map-container">
                                <div id="map"></div>
                            </div>

                            <!-- Table View -->
                            <div class="table-responsive mt-3">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Package ID</th>
                                            <th>Customer</th>
                                            <th>Pickup Location</th>
                                            <th>Delivery Location</th>
                                            <th>Weight</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${availablePackages}" var="deliveryPackage">
                                            <tr data-package-id="${deliveryPackage.id}">
                                                <td>${deliveryPackage.id}</td>
                                                <td>${deliveryPackage.customer.username}</td>
                                                <td>${deliveryPackage.pickupAddress}</td>
                                                <td>${deliveryPackage.deliveryAddress}</td>
                                                <td>${deliveryPackage.weight} kg</td>
                                                <td class="package-actions">
                                                    <form action="/courier/take-package" method="POST" style="display: inline;">
                                                        <input type="hidden" name="packageId" value="${deliveryPackage.id}">
                                                        <button type="submit" class="btn btn-primary btn-sm">Take Package</button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- Active Deliveries Tab -->
                        <div class="tab-pane fade" id="active-deliveries">
                            <h3>Active Deliveries</h3>
                            <div class="table-responsive mt-3">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>Package ID</th>
                                            <th>Customer</th>
                                            <th>Pickup Location</th>
                                            <th>Delivery Location</th>
                                            <th>Status</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${activeDeliveries}" var="deliveryPackage">
                                            <tr data-package-id="${deliveryPackage.id}">
                                                <td>${deliveryPackage.id}</td>
                                                <td>${deliveryPackage.customer.username}</td>
                                                <td>${deliveryPackage.pickupAddress}</td>
                                                <td>${deliveryPackage.deliveryAddress}</td>
                                                <td class="package-status">${deliveryPackage.status}</td>
                                                <td class="package-actions">
                                                    <form action="/courier/update-delivery-status" method="POST" style="display: inline;">
                                                        <input type="hidden" name="packageId" value="${deliveryPackage.id}">
                                                        <c:choose>
                                                            <c:when test="${deliveryPackage.status == 'ASSIGNED'}">
                                                                <button type="submit" name="status" value="PICKED_UP" class="btn btn-info btn-sm">Mark as Picked Up</button>
                                                            </c:when>
                                                            <c:when test="${deliveryPackage.status == 'PICKED_UP'}">
                                                                <button type="submit" name="status" value="DELIVERED" class="btn btn-success btn-sm">Mark as Delivered</button>
                                                            </c:when>
                                                        </c:choose>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/webjars/sockjs-client/sockjs.min.js"></script>
    <script src="/webjars/stomp-websocket/stomp.min.js"></script>
    <script>
        var stompClient = null;
        var userId = "${user.id}";
        var map;
        var markers = new Map();
        var geocoder;
        var directionsService;
        var directionsRenderer;
        var isConnecting = false;
        var reconnectTimeout = null;

        function connect() {
            if (isConnecting) return;
            isConnecting = true;
            
            if (stompClient !== null) {
                stompClient.disconnect();
            }

            console.log('Attempting to connect to WebSocket...');
            var socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            
            // Disable debug logging
            stompClient.debug = null;

            stompClient.connect({}, 
                function(frame) {
                    console.log('Connected to WebSocket');
                    isConnecting = false;
                    if (reconnectTimeout) {
                        clearTimeout(reconnectTimeout);
                        reconnectTimeout = null;
                    }
                    
                    // Subscribe to personal package updates
                    stompClient.subscribe('/topic/courier/' + userId + '/package-updates', function(message) {
                        try {
                            var deliveryPackage = JSON.parse(message.body);
                            updatePackageInTable(deliveryPackage);
                            // Update map markers if map is initialized
                            if (map && geocoder) {
                                addPackageToMap(deliveryPackage);
                            }
                        } catch (error) {
                            console.error('Error processing message:', error);
                        }
                    });

                    // Subscribe to new available packages
                    stompClient.subscribe('/topic/couriers/available-packages', function(message) {
                        try {
                            var deliveryPackage = JSON.parse(message.body);
                            addNewAvailablePackage(deliveryPackage);
                        } catch (error) {
                            console.error('Error processing message:', error);
                        }
                    });
                },
                function(error) {
                    console.error('STOMP error:', error);
                    isConnecting = false;
                    reconnect();
                }
            );

            socket.onclose = function() {
                console.log('WebSocket connection closed');
                isConnecting = false;
                reconnect();
            };
        }

        function reconnect() {
            if (!reconnectTimeout) {
                console.log('Scheduling reconnection...');
                reconnectTimeout = setTimeout(function() {
                    console.log('Attempting to reconnect...');
                    connect();
                }, 5000); // Try to reconnect after 5 seconds
            }
        }

        function updatePackageInTable(deliveryPackage) {
            if (!deliveryPackage) return;

            // Find and update package in active deliveries table
            var row = document.querySelector('tr[data-package-id="' + deliveryPackage.id + '"]');
            if (row) {
                // Update status
                row.querySelector('.package-status').textContent = deliveryPackage.status;
                
                // Update action buttons based on status
                var actionCell = row.querySelector('.package-actions');
                if (deliveryPackage.status === 'ASSIGNED') {
                    actionCell.innerHTML = `
                        <form action="/courier/update-delivery-status" method="POST" style="display: inline;">
                            <input type="hidden" name="packageId" value="${deliveryPackage.id}">
                            <button type="submit" name="status" value="PICKED_UP" class="btn btn-info btn-sm">Mark as Picked Up</button>
                        </form>`;
                } else if (deliveryPackage.status === 'PICKED_UP') {
                    actionCell.innerHTML = `
                        <form action="/courier/update-delivery-status" method="POST" style="display: inline;">
                            <input type="hidden" name="packageId" value="${deliveryPackage.id}">
                            <button type="submit" name="status" value="DELIVERED" class="btn btn-success btn-sm">Mark as Delivered</button>
                        </form>`;
                } else if (deliveryPackage.status === 'DELIVERED') {
                    actionCell.innerHTML = '<span class="text-success">Delivered</span>';
                }
            }
            
            // Update or add markers for the package
            if (map && geocoder) {
                addPackageToMap(deliveryPackage);
            }
        }

        function addNewAvailablePackage(deliveryPackage) {
            // Only add if package is in PENDING status
            if (deliveryPackage.status === 'PENDING') {
                // Check if package already exists
                const existingRow = document.querySelector(`tr[data-package-id="${deliveryPackage.id}"]`);
                if (existingRow) return; // Skip if already exists

                var tbody = document.querySelector('#available-packages table tbody');
                var row = document.createElement('tr');
                row.setAttribute('data-package-id', deliveryPackage.id);
                row.innerHTML = `
                    <td>${deliveryPackage.id}</td>
                    <td>${deliveryPackage.customer.username}</td>
                    <td>${deliveryPackage.pickupAddress}</td>
                    <td>${deliveryPackage.deliveryAddress}</td>
                    <td>${deliveryPackage.weight} kg</td>
                    <td class="package-actions">
                        <form action="/courier/take-package" method="POST" style="display: inline;">
                            <input type="hidden" name="packageId" value="${deliveryPackage.id}">
                            <button type="submit" class="btn btn-primary btn-sm">Take Package</button>
                        </form>
                    </td>
                `;
                tbody.appendChild(row);

                // Add package to map
                addPackageToMap({
                    id: deliveryPackage.id,
                    pickupAddress: deliveryPackage.pickupAddress,
                    deliveryAddress: deliveryPackage.deliveryAddress,
                    status: 'PENDING'
                });

                // Show notification
                if ('Notification' in window && Notification.permission === 'granted') {
                    new Notification('New Package Available', {
                        body: `New package from ${deliveryPackage.customer.username} is available for pickup at ${deliveryPackage.pickupAddress}`,
                        icon: '/favicon.ico'
                    });
                }
            }
        }

        function initMap() {
            geocoder = new google.maps.Geocoder();
            directionsService = new google.maps.DirectionsService();
            directionsRenderer = new google.maps.DirectionsRenderer({
                suppressMarkers: true
            });

            // Default location (Istanbul, Fatih)
            const defaultLocation = { lat: 41.0082, lng: 28.9784 };

            // Create map with custom options
            map = new google.maps.Map(document.getElementById('map'), {
                zoom: 13,
                center: defaultLocation,
                // Disable unnecessary controls
                streetViewControl: false,
                mapTypeControl: false,
                fullscreenControl: false,
                // Map style with labels
                styles: [
                    {
                        featureType: "poi",
                        stylers: [{ visibility: "on" }]
                    },
                    {
                        featureType: "transit",
                        stylers: [{ visibility: "on" }]
                    },
                    {
                        featureType: "road",
                        elementType: "labels",
                        stylers: [{ visibility: "on" }]
                    },
                    {
                        featureType: "road",
                        elementType: "geometry",
                        stylers: [{ visibility: "on" }]
                    },
                    {
                        featureType: "landscape",
                        stylers: [{ visibility: "on" }]
                    },
                    {
                        featureType: "water",
                        stylers: [{ visibility: "on" }]
                    },
                    {
                        featureType: "administrative",
                        stylers: [{ visibility: "on" }]
                    }
                ]
            });

            directionsRenderer.setMap(map);

            // Try to get user's current location
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    (position) => {
                        const pos = {
                            lat: position.coords.latitude,
                            lng: position.coords.longitude
                        };
                        map.setCenter(pos);
                        
                        // Add a marker for courier's current location
                        const courierMarker = new google.maps.Marker({
                            position: pos,
                            map: map,
                            icon: {
                                url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
                                scaledSize: new google.maps.Size(32, 32)
                            },
                            title: 'Your Location'
                        });

                        // Update courier location every 30 seconds
                        setInterval(() => {
                            if (navigator.geolocation) {
                                navigator.geolocation.getCurrentPosition(
                                    (newPosition) => {
                                        const newPos = {
                                            lat: newPosition.coords.latitude,
                                            lng: newPosition.coords.longitude
                                        };
                                        courierMarker.setPosition(newPos);
                                    },
                                    () => {}, // Silently fail if location update fails
                                    { maximumAge: 10000, timeout: 5000 }
                                );
                            }
                        }, 30000);

                        // Initialize package markers
                        initializePackageMarkers();
                    },
                    () => {
                        // If geolocation fails, use default location
                        map.setCenter(defaultLocation);
                        initializePackageMarkers();
                    },
                    { maximumAge: 10000, timeout: 5000 }
                );
            } else {
                // Browser doesn't support Geolocation, use default location
                map.setCenter(defaultLocation);
                initializePackageMarkers();
            }
        }

        function initializePackageMarkers() {
            // Initialize markers for available packages
            const availablePackages = Array.from(document.querySelectorAll('#available-packages tr[data-package-id]')).map(row => ({
                id: row.getAttribute('data-package-id'),
                pickupAddress: row.cells[2].textContent,
                deliveryAddress: row.cells[3].textContent,
                status: 'PENDING'
            }));

            // Initialize markers for active deliveries
            const activeDeliveries = Array.from(document.querySelectorAll('#active-deliveries tr[data-package-id]')).map(row => ({
                id: row.getAttribute('data-package-id'),
                pickupAddress: row.cells[2].textContent,
                deliveryAddress: row.cells[3].textContent,
                status: row.cells[4].textContent
            }));

            const allPackages = [...availablePackages, ...activeDeliveries];
            allPackages.forEach(addPackageToMap);
        }

        function addPackageToMap(deliveryPackage) {
            // Add pickup marker
            geocoder.geocode({ address: deliveryPackage.pickupAddress }, function(results, status) {
                if (status === 'OK') {
                    const marker = new google.maps.Marker({
                        map: map,
                        position: results[0].geometry.location,
                        icon: {
                            url: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
                            scaledSize: new google.maps.Size(32, 32)
                        },
                        title: 'Pickup: ' + deliveryPackage.pickupAddress
                    });

                    const infoWindow = new google.maps.InfoWindow({
                        content: `
                            <div>
                                <h6>Package #${deliveryPackage.id}</h6>
                                <p><strong>Pickup Location</strong><br>${deliveryPackage.pickupAddress}</p>
                                <p><strong>Status:</strong> ${deliveryPackage.status}</p>
                            </div>
                        `
                    });

                    marker.addListener('click', () => {
                        infoWindow.open(map, marker);
                    });

                    markers.set('pickup-' + deliveryPackage.id, marker);

                    // If this is an active delivery, calculate and display route
                    if (deliveryPackage.status === 'ASSIGNED' || deliveryPackage.status === 'PICKED_UP') {
                        calculateAndDisplayRoute(deliveryPackage);
                    }
                }
            });

            // Add delivery marker
            geocoder.geocode({ address: deliveryPackage.deliveryAddress }, function(results, status) {
                if (status === 'OK') {
                    const marker = new google.maps.Marker({
                        map: map,
                        position: results[0].geometry.location,
                        icon: {
                            url: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png',
                            scaledSize: new google.maps.Size(32, 32)
                        },
                        title: 'Delivery: ' + deliveryPackage.deliveryAddress
                    });

                    const infoWindow = new google.maps.InfoWindow({
                        content: `
                            <div>
                                <h6>Package #${deliveryPackage.id}</h6>
                                <p><strong>Delivery Location</strong><br>${deliveryPackage.deliveryAddress}</p>
                                <p><strong>Status:</strong> ${deliveryPackage.status}</p>
                            </div>
                        `
                    });

                    marker.addListener('click', () => {
                        infoWindow.open(map, marker);
                    });

                    markers.set('delivery-' + deliveryPackage.id, marker);
                }
            });
        }

        function calculateAndDisplayRoute(deliveryPackage) {
            const request = {
                origin: deliveryPackage.pickupAddress,
                destination: deliveryPackage.deliveryAddress,
                travelMode: 'DRIVING'
            };

            directionsService.route(request, function(result, status) {
                if (status === 'OK') {
                    directionsRenderer.setDirections(result);
                }
            });
        }

        // Initialize map when the available packages or active deliveries tab is shown
        document.querySelectorAll('a[href="#available-packages"], a[href="#active-deliveries"]').forEach(tab => {
            tab.addEventListener('click', function() {
                setTimeout(function() {
                    if (!map) {
                        initMap();
                    }
                }, 100);
            });
        });

        // Request notification permission on page load
        document.addEventListener('DOMContentLoaded', function() {
            if ('Notification' in window) {
                Notification.requestPermission();
            }
            connect();
        });
    </script>
</body>
</html> 