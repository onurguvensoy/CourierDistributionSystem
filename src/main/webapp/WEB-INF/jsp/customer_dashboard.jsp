<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Customer Dashboard - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
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
        #map, #sendPackageMap {
            height: 400px;
            width: 100%;
            border-radius: 8px;
            margin-top: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .map-container {
            position: relative;
            margin-bottom: 20px;
        }
        .package-marker {
            cursor: pointer;
        }
        .card {
            background-color: rgba(255, 255, 255, 0.9);
            border-radius: 8px;
            transition: transform 0.2s;
        }
        .card:hover {
            transform: translateY(-2px);
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="dashboard-container">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Customer Dashboard</h2>
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
                        <a class="nav-link" href="#send-package" data-bs-toggle="pill">Send Package</a>
                        <a class="nav-link" href="#track-packages" data-bs-toggle="pill">Track Packages</a>
                        <a class="nav-link" href="#shipping-history" data-bs-toggle="pill">Shipping History</a>
                        <a class="nav-link" href="#give-rating" data-bs-toggle="pill">Rate Delivery</a>
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
                                            <h5 class="card-title">Active Shipments</h5>
                                            <p class="card-text display-4">0</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Total Packages</h5>
                                            <p class="card-text display-4">0</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Delivered</h5>
                                            <p class="card-text display-4">0</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Send Package Tab -->
                        <div class="tab-pane fade" id="send-package">
                            <h3>Send a Package</h3>
                            <form class="mt-4" action="/api/deliveries" method="POST" id="sendPackageForm">
                                <input type="hidden" name="username" value="${user.username}">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                
                                <!-- Pickup Address Section -->
                                <div class="mb-3">
                                    <label class="form-label">Pickup Address</label>
                                    <div class="input-group">
                                        <input type="text" class="form-control" name="pickupAddress" id="pickupAddress" required>
                                        <button type="button" class="btn btn-outline-primary" id="useCurrentLocation">
                                            Use Current Location
                                        </button>
                                    </div>
                                </div>

                                <!-- Delivery Address Section -->
                                <div class="mb-3">
                                    <label class="form-label">Delivery Address</label>
                                    <input type="text" class="form-control" name="deliveryAddress" id="deliveryAddress" required readonly>
                                    <small class="text-muted">Click on the map to select delivery location</small>
                                </div>

                                <!-- Map for selecting locations -->
                                <div class="mb-3">
                                    <div id="sendPackageMap" style="height: 400px; width: 100%; border-radius: 5px;"></div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Package Weight (kg)</label>
                                    <input type="number" step="0.1" class="form-control" name="weight" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Package Description</label>
                                    <textarea class="form-control" name="description" rows="3" required></textarea>
                                </div>
                                <button type="submit" class="btn btn-primary">Request Pickup</button>
                            </form>
                        </div>

                        <script>
                            var sendPackageMap;
                            var sendPackageGeocoder;
                            var pickupMarker;
                            var deliveryMarker;
                            var searchBox;

                            // Initialize the map for sending packages
                            function initSendPackageMap() {
                                sendPackageGeocoder = new google.maps.Geocoder();
                                
                                // Default location (Istanbul, Fatih)
                                const defaultLocation = { lat: 41.0082, lng: 28.9784 };
                                
                                // Create the map with custom options
                                sendPackageMap = new google.maps.Map(document.getElementById('sendPackageMap'), {
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

                                // Create search box for pickup address
                                const pickupInput = document.getElementById('pickupAddress');
                                const pickupSearchBox = new google.maps.places.SearchBox(pickupInput);
                                
                                // Bias the SearchBox results towards current map's viewport
                                sendPackageMap.addListener('bounds_changed', function() {
                                    pickupSearchBox.setBounds(sendPackageMap.getBounds());
                                });

                                // Try to get user's current location
                                if (navigator.geolocation) {
                                    navigator.geolocation.getCurrentPosition(
                                        (position) => {
                                            const pos = {
                                                lat: position.coords.latitude,
                                                lng: position.coords.longitude
                                            };
                                            sendPackageMap.setCenter(pos);
                                            
                                            // Set current location as default pickup location
                                            sendPackageGeocoder.geocode({ location: pos }, function(results, status) {
                                                if (status === 'OK') {
                                                    if (results[0]) {
                                                        document.getElementById('pickupAddress').value = results[0].formatted_address;
                                                        if (pickupMarker) pickupMarker.setMap(null);
                                                        pickupMarker = new google.maps.Marker({
                                                            position: pos,
                                                            map: sendPackageMap,
                                                            icon: {
                                                                url: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
                                                                scaledSize: new google.maps.Size(32, 32)
                                                            },
                                                            title: 'Pickup Location'
                                                        });
                                                    }
                                                }
                                            });
                                        },
                                        () => {
                                            // If geolocation fails, use default location
                                            sendPackageMap.setCenter(defaultLocation);
                                        },
                                        { maximumAge: 10000, timeout: 5000 }
                                    );
                                } else {
                                    // Browser doesn't support Geolocation, use default location
                                    sendPackageMap.setCenter(defaultLocation);
                                }

                                // Listen for clicks on the map for delivery location
                                sendPackageMap.addListener('click', function(event) {
                                    // Update delivery marker
                                    if (deliveryMarker) deliveryMarker.setMap(null);
                                    deliveryMarker = new google.maps.Marker({
                                        position: event.latLng,
                                        map: sendPackageMap,
                                        icon: {
                                            url: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png',
                                            scaledSize: new google.maps.Size(32, 32)
                                        },
                                        title: 'Delivery Location'
                                    });

                                    // Get address for clicked location
                                    sendPackageGeocoder.geocode({ location: event.latLng }, function(results, status) {
                                        if (status === 'OK') {
                                            if (results[0]) {
                                                document.getElementById('deliveryAddress').value = results[0].formatted_address;
                                            }
                                        }
                                    });
                                });
                            }

                            // Handle "Use Current Location" button click
                            document.getElementById('useCurrentLocation').addEventListener('click', function() {
                                if (navigator.geolocation) {
                                    navigator.geolocation.getCurrentPosition(
                                        (position) => {
                                            const pos = {
                                                lat: position.coords.latitude,
                                                lng: position.coords.longitude
                                            };
                                            
                                            // Update map and marker
                                            sendPackageMap.setCenter(pos);
                                            if (pickupMarker) pickupMarker.setMap(null);
                                            pickupMarker = new google.maps.Marker({
                                                position: pos,
                                                map: sendPackageMap,
                                                icon: {
                                                    url: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png',
                                                    scaledSize: new google.maps.Size(32, 32)
                                                },
                                                title: 'Pickup Location'
                                            });

                                            // Get and set address
                                            sendPackageGeocoder.geocode({ location: pos }, function(results, status) {
                                                if (status === 'OK') {
                                                    if (results[0]) {
                                                        document.getElementById('pickupAddress').value = results[0].formatted_address;
                                                    }
                                                }
                                            });
                                        }
                                    );
                                }
                            });

                            // Initialize send package map when the tab is shown
                            document.querySelector('a[href="#send-package"]').addEventListener('click', function() {
                                setTimeout(function() {
                                    if (!sendPackageMap) {
                                        initSendPackageMap();
                                    }
                                }, 100);
                            });
                        </script>

                        <!-- Track Packages Tab -->
                        <div class="tab-pane fade" id="track-packages">
                            <h3>Track Packages</h3>
                            
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
                                            <th>Pickup Address</th>
                                            <th>Delivery Address</th>
                                            <th>Status</th>
                                            <th>Courier</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${myPackages}" var="deliveryPackage">
                                            <tr data-package-id="${deliveryPackage.id}">
                                                <td>${deliveryPackage.id}</td>
                                                <td>${deliveryPackage.pickupAddress}</td>
                                                <td>${deliveryPackage.deliveryAddress}</td>
                                                <td class="package-status">${deliveryPackage.status}</td>
                                                <td>${deliveryPackage.courierUsername != null ? deliveryPackage.courierUsername : 'Not Assigned'}</td>
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
        // Global variables
        var stompClient = null;
        var userId = "${user.id}";
        var map = null;
        var sendPackageMap = null;
        var geocoder = null;
        var markers = new Map();
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
                    
                    // Subscribe to package updates for this customer
                    stompClient.subscribe('/topic/customer/' + userId + '/package-updates', function(message) {
                        try {
                            var data = JSON.parse(message.body);
                            if (data.type === 'NEW_PACKAGE' || data.type === 'STATUS_UPDATE') {
                                var deliveryPackage = {
                                    id: data.id,
                                    pickupAddress: data.pickupAddress,
                                    deliveryAddress: data.deliveryAddress,
                                    status: data.status,
                                    courierUsername: data.courierUsername
                                };
                                updatePackageInTable(deliveryPackage);
                                // Update map markers if map is initialized
                                if (map && geocoder) {
                                    addPackageToMap(deliveryPackage);
                                }
                            }
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
            var row = document.querySelector('tr[data-package-id="' + deliveryPackage.id + '"]');
            if (row) {
                row.querySelector('.package-status').textContent = deliveryPackage.status;
                // Update courier if assigned
                var courierCell = row.cells[4];
                courierCell.textContent = deliveryPackage.courierUsername || 'Not Assigned';
                courierCell.textContent = deliveryPackage.courier ? deliveryPackage.courier.username : 'Not Assigned';
            } else {
                // If package not in table, add it
                var tbody = document.querySelector('#track-packages table tbody');
                var newRow = document.createElement('tr');
                newRow.setAttribute('data-package-id', deliveryPackage.id);
                newRow.innerHTML = `
                    <td>\${deliveryPackage.id}</td>
                    <td>\${deliveryPackage.pickupAddress}</td>
                    <td>\${deliveryPackage.deliveryAddress}</td>
                    <td class="package-status">\${deliveryPackage.status}</td>
                    <td>\${deliveryPackage.courier ? deliveryPackage.courier.username : 'Not Assigned'}</td>
                `;
                tbody.appendChild(newRow);
            }
        }

        // Remove duplicate declarations and update the form submission
        document.getElementById('sendPackageForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            if (!stompClient || !stompClient.connected) {
                alert('Connection to server lost. Please try again.');
                return;
            }
            
            const formData = new FormData(this);
            const data = {};
            formData.forEach((value, key) => data[key] = value);
            
            fetch('/api/packages?username=' + encodeURIComponent('${user.username}'), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(package => {
                // Switch to track packages tab first to ensure map is initialized
                document.querySelector('a[href="#track-packages"]').click();
                
                // Wait for map initialization
                setTimeout(() => {
                    // Update local table
                    updatePackageInTable(package);
                    
                    // Notify all couriers about new package
                    if (stompClient && stompClient.connected) {
                        stompClient.send("/app/packages/new", {}, JSON.stringify(package));
                    }
                    
                    // Reset form
                    this.reset();
                    
                    // Show success message
                    alert('Package created successfully!');
                }, 500);
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Failed to create package. Please try again.');
            });
        });

        function initMap() {
            if (!geocoder) {
                geocoder = new google.maps.Geocoder();
            }
            
            if (!map) {
                map = new google.maps.Map(document.getElementById('map'), {
                    zoom: 12,
                    center: { lat: 0, lng: 0 }
                });

                // Try to get user's current location
                if (navigator.geolocation) {
                    navigator.geolocation.getCurrentPosition(
                        (position) => {
                            const pos = {
                                lat: position.coords.latitude,
                                lng: position.coords.longitude
                            };
                            map.setCenter(pos);
                            
                            // Add a marker for current location
                            new google.maps.Marker({
                                position: pos,
                                map: map,
                                icon: {
                                    url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
                                    scaledSize: new google.maps.Size(32, 32)
                                },
                                title: 'Your Location'
                            });

                            // Initialize markers for existing packages
                            const packages = Array.from(document.querySelectorAll('tr[data-package-id]')).map(row => ({
                                id: row.getAttribute('data-package-id'),
                                pickupAddress: row.cells[1].textContent,
                                deliveryAddress: row.cells[2].textContent,
                                status: row.cells[3].textContent
                            }));

                            packages.forEach(addPackageToMap);
                        },
                        () => handleGeolocationError()
                    );
                } else {
                    handleGeolocationError();
                }
            }
        }

        function handleGeolocationError() {
            // Fallback to first package location
            const packages = Array.from(document.querySelectorAll('tr[data-package-id]')).map(row => ({
                id: row.getAttribute('data-package-id'),
                pickupAddress: row.cells[1].textContent,
                deliveryAddress: row.cells[2].textContent,
                status: row.cells[3].textContent
            }));
            
            if (packages.length > 0) {
                geocoder.geocode({ address: packages[0].pickupAddress }, function(results, status) {
                    if (status === 'OK') {
                        map.setCenter(results[0].geometry.location);
                        packages.forEach(addPackageToMap);
                    }
                });
            }
        }

        function addPackageToMap(deliveryPackage) {
            if (!geocoder || !map) return; // Ensure geocoder and map are initialized

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

        // Initialize map when the track packages tab is shown
        document.querySelector('a[href="#track-packages"]').addEventListener('click', function() {
            setTimeout(function() {
                initMap();
            }, 100);
        });

        // Connect when page loads
        document.addEventListener('DOMContentLoaded', function() {
            connect();
        });
    </script>
</body>
</html> 