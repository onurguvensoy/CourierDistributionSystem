<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Customer Dashboard - Courier Distribution System</title>
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
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
        .rating-modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
        }

        .rating-modal-content {
            background-color: white;
            margin: 15% auto;
            padding: 20px;
            border-radius: 8px;
            width: 80%;
            max-width: 500px;
        }

        .star-rating {
            display: inline-block;
            font-size: 24px;
            cursor: pointer;
        }

        .star-rating .star {
            color: #ddd;
            transition: color 0.2s;
        }

        .star-rating .star.active {
            color: #ffd700;
        }

        .package-status {
            padding: 5px 10px;
            border-radius: 15px;
            font-size: 0.9em;
            font-weight: 500;
        }

        .status-pending { background-color: #ffeeba; color: #856404; }
        .status-picked-up { background-color: #b8daff; color: #004085; }
        .status-in-transit { background-color: #c3e6cb; color: #155724; }
        .status-delivered { background-color: #d4edda; color: #155724; }
        .status-cancelled { background-color: #f8d7da; color: #721c24; }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="dashboard-container">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2>Customer Dashboard</h2>
                    <p class="text-muted mb-0">Welcome, <c:out value="${sessionScope.fullName != null ? sessionScope.fullName : sessionScope.username}"/></p>
                </div>
                <div class="d-flex align-items-center">
                    <div class="text-end me-3">
                        <small class="d-block text-muted"><c:out value="${sessionScope.email}"/></small>
                        <small class="d-block text-muted"><c:out value="${sessionScope.phoneNumber}"/></small>
                    </div>
                    <form id="logoutForm" action="/api/auth/logout" method="POST" style="margin: 0;">
                        <button type="submit" class="btn btn-outline-danger">Logout</button>
                    </form>
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
                        <a class="nav-link" href="#profile" data-bs-toggle="pill">Profile</a>
                    </div>
                </div>
            </div>


            <div class="col-md-9">
                <div class="dashboard-container">
                    <div class="tab-content">
                        <div class="tab-pane fade show active" id="overview">
                            <h3>Overview</h3>
                            <div class="row mt-4">
                                <div class="col-md-4">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Active Shipments</h5>
                                            <p class="card-text display-4">${stats.activeShipments}</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Total Packages</h5>
                                            <p class="card-text display-4">${stats.totalPackages}</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Delivered</h5>
                                            <p class="card-text display-4">${stats.deliveredPackages}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="tab-pane fade" id="send-package">
                            <h3>Send a Package</h3>
                            <div id="sendPackageAlertPlaceholder"></div>
                            <form class="mt-4" id="sendPackageForm">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" id="username" name="username" value="${sessionScope.username}"/>
                                
                                <!-- Pickup Address Section -->
                                <div class="mb-3">
                                    <label class="form-label">Pickup Address</label>
                                    <input type="text" class="form-control" name="pickupAddress" id="pickupAddress" required>
                                    <small class="text-muted">Enter the complete address where the package will be picked up</small>
                                </div>

                                <!-- Delivery Address Section -->
                                <div class="mb-3">
                                    <label class="form-label">Delivery Address</label>
                                    <input type="text" class="form-control" name="deliveryAddress" id="deliveryAddress" required>
                                    <small class="text-muted">Enter the complete delivery address</small>
                                </div>

                                <!-- Package Details -->
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="mb-3">
                                            <label class="form-label">Weight (kg)</label>
                                            <input type="number" class="form-control" name="weight" id="weight" step="0.1" min="0.1" required>
                                        </div>
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Package Description</label>
                                    <textarea class="form-control" name="description" id="description" rows="2" required></textarea>
                                    <small class="text-muted">Briefly describe the contents of your package</small>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Special Instructions (Optional)</label>
                                    <textarea class="form-control" name="specialInstructions" id="specialInstructions" rows="2"></textarea>
                                    <small class="text-muted">Any special handling instructions or delivery notes</small>
                                </div>

                                <div class="d-grid gap-2">
                                    <button type="submit" class="btn btn-primary">Create Delivery Request</button>
                                </div>
                            </form>
                        </div>

                        <script>
                            // Wait for DOM to be fully loaded
                            document.addEventListener('DOMContentLoaded', function() {
                                const form = document.getElementById('sendPackageForm');
                                if (!form) {
                                    console.error('Send package form not found');
                                    return;
                                }

                                form.addEventListener('submit', function(e) {
                                    e.preventDefault();
                                    
                                    const formData = {
                                        pickupAddress: document.getElementById('pickupAddress')?.value || '',
                                        deliveryAddress: document.getElementById('deliveryAddress')?.value || '',
                                        weight: parseFloat(document.getElementById('weight')?.value || '0'),
                                        description: document.getElementById('description')?.value || '',
                                        specialInstructions: document.getElementById('specialInstructions')?.value || ''
                                    };

                                    // Get CSRF token from meta tags or form
                                    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || 
                                                    document.querySelector('input[name="_csrf"]')?.value;
                                    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 
                                                     'X-CSRF-TOKEN';

                                    const headers = {
                                        'Content-Type': 'application/json'
                                    };
                                    
                                    if (csrfToken) {
                                        headers[csrfHeader] = csrfToken;
                                    }

                                    fetch('/api/packages/create', {
                                        method: 'POST',
                                        headers: headers,
                                        body: JSON.stringify(formData),
                                        credentials: 'include'
                                    })
                                    .then(response => {
                                        if (!response.ok) {
                                            return response.json().then(data => {
                                                throw new Error(data.message || 'Failed to create package');
                                            });
                                        }
                                        return response.json();
                                    })
                                    .then(data => {
                                        const alertPlaceholder = document.getElementById('sendPackageAlertPlaceholder');
                                        if (alertPlaceholder) {
                                            showAlert('success', 'Package delivery request created successfully!', 'sendPackageAlertPlaceholder');
                                            form.reset();
                                            // Refresh the active packages list
                                            setTimeout(() => window.location.reload(), 2000);
                                        }
                                    })
                                    .catch(error => {
                                        console.error('Error creating package:', error);
                                        const alertPlaceholder = document.getElementById('sendPackageAlertPlaceholder');
                                        if (alertPlaceholder) {
                                            showAlert('danger', error.message || 'Failed to create package. Please try again.', 'sendPackageAlertPlaceholder');
                                        }
                                    });
                                });
                            });

                            // Helper function to show alerts
                            function showAlert(type, message, containerId) {
                                const alertPlaceholder = document.getElementById(containerId);
                                if (!alertPlaceholder) {
                                    console.error(`Alert container ${containerId} not found`);
                                    return;
                                }

                                const wrapper = document.createElement('div');
                                wrapper.innerHTML = [
                                    `<div class="alert alert-${type} alert-dismissible fade show" role="alert">`,
                                    `   <div>${message}</div>`,
                                    '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
                                    '</div>'
                                ].join('');
                                
                                alertPlaceholder.innerHTML = '';
                                alertPlaceholder.append(wrapper);
                            }
                        </script>

                        <!-- Track Packages Tab -->
                        <div class="tab-pane fade" id="track-packages">
                            <h3>Track Packages</h3>
                            <div class="row mt-4" id="activePackagesContainer">
                                <c:forEach items="${activePackages}" var="pkg">
                                    <div class="col-md-6 mb-4">
                                        <div class="card h-100" data-package-id="${pkg.package_id}">
                                            <div class="card-body">
                                                <div class="d-flex justify-content-between align-items-center mb-3">
                                                    <h5 class="card-title mb-0">Package #<c:out value="${pkg.package_id}"/></h5>
                                                    <span class="package-status status-${fn:toLowerCase(pkg.status)}"><c:out value="${pkg.status}"/></span>
                                                </div>
                                                <p class="card-text">
                                                    <strong>From:</strong> <c:out value="${pkg.pickupAddress}"/><br>
                                                    <strong>To:</strong> <c:out value="${pkg.deliveryAddress}"/><br>
                                                    <strong>Current Location:</strong> <span class="current-location"><c:out value="${pkg.currentLocation}"/></span><br>
                                                    <strong>Weight:</strong> <c:out value="${pkg.weight}"/> kg<br>
                                                    <strong>Description:</strong> <c:out value="${pkg.description}"/>
                                                </p>
                                                <c:if test="${not empty pkg.specialInstructions}">
                                                    <p class="card-text">
                                                        <strong>Special Instructions:</strong><br>
                                                        <c:out value="${pkg.specialInstructions}"/>
                                                    </p>
                                                </c:if>
                                                <c:if test="${not empty pkg.courier}">
                                                    <p class="card-text">
                                                        <strong>Courier:</strong> <c:out value="${pkg.courierUsername}"/><br>
                                                        <strong>Contact:</strong> <c:out value="${pkg.courier.phoneNumber}"/>
                                                    </p>
                                                </c:if>
                                            </div>
                                            <div class="card-footer bg-transparent">
                                                <button class="btn btn-primary btn-sm track-package-btn" data-package-id="${pkg.package_id}">
                                                    Track on Map
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                            
                            <!-- Map Container -->
                            <div class="map-container mt-4" style="display: none;">
                                <div id="map"></div>
                            </div>
                        </div>

                        <!-- Shipping History Tab -->
                        <div class="tab-pane fade" id="shipping-history">
                            <h3>Shipping History</h3>
                            <div class="row mt-4">
                                <c:forEach items="${completedPackages}" var="pkg">
                                    <div class="col-md-6 mb-4">
                                        <div class="card h-100" data-package-id="${pkg.id}">
                                            <div class="card-body">
                                                <div class="d-flex justify-content-between align-items-center mb-3">
                                                    <h5 class="card-title mb-0">Package #<c:out value="${pkg.id}"/></h5>
                                                    <span class="package-status status-${fn:toLowerCase(pkg.status)}"><c:out value="${pkg.status}"/></span>
                                                </div>
                                                <p class="card-text">
                                                    <strong>From:</strong> <c:out value="${pkg.pickupAddress}"/><br>
                                                    <strong>To:</strong> <c:out value="${pkg.deliveryAddress}"/><br>
                                                    <strong>Weight:</strong> <c:out value="${pkg.weight}"/> kg<br>
                                                    <strong>Description:</strong> <c:out value="${pkg.description}"/><br>
                                                    <c:if test="${pkg.status == 'DELIVERED'}">
                                                        <strong>Delivered On:</strong> <c:out value="${pkg.deliveredAt}"/>
                                                    </c:if>
                                                    <c:if test="${pkg.status == 'CANCELLED'}">
                                                        <strong>Cancelled On:</strong> <c:out value="${pkg.cancelledAt}"/>
                                                    </c:if>
                                                </p>
                                                <c:if test="${empty pkg.rating}">
                                                    <button class="btn btn-primary btn-sm rate-package-btn" data-package-id="${pkg.id}">
                                                        <c:if test="${pkg.status == 'DELIVERED'}">Rate Delivery</c:if>
                                                        <c:if test="${pkg.status == 'CANCELLED'}">Rate Cancellation</c:if>
                                                    </button>
                                                </c:if>
                                                <c:if test="${not empty pkg.rating}">
                                                    <div class="mt-3">
                                                        <strong>Your Rating:</strong>
                                                        <div class="star-rating-display">
                                                            <c:forEach begin="1" end="5" var="star">
                                                                <span class="star ${star <= pkg.rating.rating ? 'active' : ''}">★</span>
                                                            </c:forEach>
                                                        </div>
                                                        <c:if test="${not empty pkg.rating.comment}">
                                                            <p class="mt-2 mb-0"><small class="text-muted"><c:out value="${pkg.rating.comment}"/></small></p>
                                                        </c:if>
                                                    </div>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
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
        let stompClient = null;
        const userId = "${user.id}";
        const username = "${sessionScope.username}";
        let map = null;
        let sendPackageMap = null;
        let geocoder = null;
        let markers = new Map();
        let isConnecting = false;
        let reconnectTimeout = null;
        let packageMarkers = {};

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
                            handlePackageUpdate(data);
                        } catch (error) {
                            console.error('Error processing message:', error);
                        }
                    });

                    // Subscribe to location updates
                    stompClient.subscribe('/user/' + username + '/queue/location-updates', function(message) {
                        const update = JSON.parse(message.body);
                        updatePackageLocation(update);
                    });

                    // Subscribe to rating prompts
                    stompClient.subscribe('/user/' + username + '/queue/rating-prompts', function(message) {
                        const prompt = JSON.parse(message.body);
                        showRatingModal(prompt.packageId);
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

        function initMap() {
            if (!geocoder) {
                geocoder = new google.maps.Geocoder();
            }
            
            if (!map) {
                map = new google.maps.Map(document.getElementById('map'), {
                    zoom: 12,
                    center: { lat: 0, lng: 0 },
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

        function showPackageOnMap(packageId) {
            const mapContainer = document.querySelector('.map-container');
            if (mapContainer) {
                mapContainer.style.display = 'block';
            }

            if (!map) {
                initMap();
            }

            // Fetch package location
            fetch(`/api/deliveries/${packageId}/track?username=${username}`)
                .then(response => response.json())
                .then(result => {
                    if (result.status === 'success') {
                        const data = result.data;
                        const position = {
                            lat: data.latitude,
                            lng: data.longitude,
                        };

                        // Create or update marker
                        if (!packageMarkers[packageId]) {
                            packageMarkers[packageId] = new google.maps.Marker({
                                position: position,
                                map: map,
                                title: `Package #${packageId}`,
                                icon: {
                                    url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
                                },
                            });
                        } else {
                            packageMarkers[packageId].setPosition(position);
                        }

                        // Center map on marker
                        map.setCenter(position);
                    }
                })
                .catch(error => console.error('Error fetching package location:', error));
        }

        function handlePackageUpdate(update) {
            if (update.type === 'NEW_PACKAGE' || update.type === 'STATUS_UPDATE') {
                const deliveryPackage = {
                    id: update.id,
                    pickupAddress: update.pickupAddress,
                    deliveryAddress: update.deliveryAddress,
                    status: update.status,
                    courierUsername: update.courierUsername
                };
                updatePackageInTable(deliveryPackage);
                // Update map markers if map is initialized
                if (map && geocoder) {
                    addPackageToMap(deliveryPackage);
                }
            }

            const packageCard = document.querySelector(`[data-package-id="${update.packageId}"]`);
            if (packageCard) {
                // Update status
                const statusBadge = packageCard.querySelector('.package-status');
                if (statusBadge) {
                    statusBadge.className = `package-status status-${update.status.toLowerCase()}`;
                    statusBadge.textContent = update.status;
                }

                // Update location if provided
                if (update.currentLocation) {
                    const locationElement = packageCard.querySelector('.current-location');
                    if (locationElement) {
                        locationElement.textContent = update.currentLocation;
                    }
                }

                // Show rating modal if delivered
                if (update.status === 'DELIVERED') {
                    checkAndShowRatingModal(update.packageId);
                }
            }
        }

        function updatePackageLocation(update) {
            const packageCard = document.querySelector(`[data-package-id="${update.packageId}"]`);
            if (packageCard) {
                const locationElement = packageCard.querySelector('.current-location');
                if (locationElement) {
                    locationElement.textContent = update.location;
                }

                // Update map marker if map is visible
                if (packageMarkers && packageMarkers[update.packageId]) {
                    const marker = packageMarkers[update.packageId];
                    marker.setPosition({
                        lat: update.latitude,
                        lng: update.longitude
                    });
                }
            }
        }

        function checkAndShowRatingModal(packageId) {
            fetch(`/api/ratings/delivery/${packageId}?username=${username}`)
                .then(response => response.json())
                .then(result => {
                    if (result.status === 'success' && result.showRatingPopup) {
                        showRatingModal(packageId);
                    }
                })
                .catch(error => console.error('Error checking rating:', error));
        }

        function showRatingModal(packageId) {
            const modal = document.getElementById('ratingModal');
            const packageIdInput = document.getElementById('ratingPackageId');
            if (modal && packageIdInput) {
                packageIdInput.value = packageId;
                modal.style.display = 'block';
            }
        }

        function submitRating(packageId, rating, comment = '') {
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

            if (!csrfHeader || !csrfToken) {
                console.error('CSRF tokens not found');
                return;
            }

            fetch(`/api/ratings/delivery/${packageId}?username=${username}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({
                    rating: rating,
                    comment: comment
                })
            })
            .then(response => response.json())
            .then(result => {
                if (result.status === 'success') {
                    const modal = document.getElementById('ratingModal');
                    if (modal) {
                        modal.style.display = 'none';
                    }
                    showAlert('success', 'Thank you for your rating!');
                } else {
                    throw new Error(result.message || 'Failed to submit rating');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showAlert('danger', error.message || 'Failed to submit rating. Please try again.');
            });
        }

        // Initialize WebSocket connection when page loads
        document.addEventListener('DOMContentLoaded', function() {
            connect();

            // Initialize event listeners for package tracking and rating buttons
            document.querySelectorAll('.track-package-btn').forEach(button => {
                button.addEventListener('click', function() {
                    const packageId = this.dataset.packageId;
                    showPackageOnMap(packageId);
                });
            });

            document.querySelectorAll('.rate-package-btn').forEach(button => {
                button.addEventListener('click', function() {
                    const packageId = this.dataset.packageId;
                    showRatingModal(packageId);
                });
            });
        });
    </script>

    <!-- Rating Modal -->
    <div id="ratingModal" class="rating-modal">
        <div class="rating-modal-content">
            <h4>Rate Your Delivery</h4>
            <input type="hidden" id="ratingPackageId">
            <div class="mb-3">
                <div class="star-rating" id="starRating">
                    <span class="star" data-rating="1">★</span>
                    <span class="star" data-rating="2">★</span>
                    <span class="star" data-rating="3">★</span>
                    <span class="star" data-rating="4">★</span>
                    <span class="star" data-rating="5">★</span>
                </div>
            </div>
            <div class="mb-3">
                <label for="ratingComment" class="form-label">Comment (Optional)</label>
                <textarea class="form-control" id="ratingComment" rows="3"></textarea>
            </div>
            <button type="button" class="btn btn-primary" onclick="submitRatingFromModal()">Submit Rating</button>
            <button type="button" class="btn btn-secondary" onclick="document.getElementById('ratingModal').style.display='none'">Close</button>
        </div>
    </div>

    <script>
        // Star rating functionality
        document.addEventListener('DOMContentLoaded', function() {
            const stars = document.querySelectorAll('.star-rating .star');
            if (!stars) return;

            stars.forEach(star => {
                star.addEventListener('mouseover', function() {
                    const rating = this.dataset.rating;
                    const currentStars = document.querySelectorAll('.star-rating .star');
                    if (currentStars) {
                        currentStars.forEach(s => {
                            s.classList.toggle('active', s.dataset.rating <= rating);
                        });
                    }
                });

                star.addEventListener('mouseout', function() {
                    const starRating = document.querySelector('.star-rating');
                    if (!starRating) return;
                    
                    const selectedRating = starRating.dataset.selectedRating;
                    const currentStars = document.querySelectorAll('.star-rating .star');
                    if (currentStars) {
                        currentStars.forEach(s => {
                            s.classList.toggle('active', s.dataset.rating <= (selectedRating || 0));
                        });
                    }
                });

                star.addEventListener('click', function() {
                    const rating = this.dataset.rating;
                    const starRating = document.querySelector('.star-rating');
                    if (starRating) {
                        starRating.dataset.selectedRating = rating;
                    }
                });
            });
        });

        function submitRatingFromModal() {
            const packageId = document.getElementById('ratingPackageId')?.value;
            const starRating = document.querySelector('.star-rating');
            const rating = starRating?.dataset?.selectedRating;
            const comment = document.getElementById('ratingComment')?.value || '';

            if (!packageId || !rating) {
                showAlert('danger', 'Please select a rating');
                return;
            }

            submitRating(packageId, parseInt(rating), comment);
        }
    </script>

    <script async defer
        src="https://maps.googleapis.com/maps/api/js?key=${googleMapsApiKey}&callback=initMap">
    </script>

    <script>
        document.getElementById('logoutForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => {
                window.location.href = '/auth/login';
            })
            .catch(error => {
                console.error('Logout failed:', error);
                window.location.href = '/auth/login';
            });
        });
    </script>
</body>
</html> 