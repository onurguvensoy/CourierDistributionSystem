<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Courier Dashboard - Courier Distribution System</title>
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
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
        <div class="dashboard-container">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Courier Dashboard</h2>
                <div>
                    <span class="me-3">Welcome, ${user.username}</span>
                    <a href="/auth/logout" class="btn btn-outline-danger">Logout</a>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-3">
                <div class="dashboard-container">
                    <div class="nav flex-column nav-pills">
                        <a class="nav-link active" href="#overview" data-bs-toggle="pill">Overview</a>
                        <a class="nav-link" href="#available-packages" data-bs-toggle="pill">Available Packages</a>
                        <a class="nav-link" href="#active-deliveries" data-bs-toggle="pill">Active Deliveries</a>
                    </div>
                </div>
            </div>

            <div class="col-md-9">
                <div class="dashboard-container">
                    <div class="tab-content">
                        <div class="tab-pane fade show active" id="overview">
                            <h3>Overview</h3>
                            <div class="row mt-4">
                                <div class="col-md-6">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Active Deliveries</h5>
                                            <p class="card-text display-4">${activeDeliveries.size()}</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="card">
                                        <div class="card-body">
                                            <h5 class="card-title">Available Packages</h5>
                                            <p class="card-text display-4">${availablePackages.size()}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="tab-pane fade" id="available-packages">
                            <h3>Available Packages</h3>
                            <div class="map-container mb-4">
                                <div id="map"></div>
                            </div>
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Customer</th>
                                            <th>Pickup</th>
                                            <th>Delivery</th>
                                            <th>Weight</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody id="available-packages-table">
                                        <c:forEach items="${availablePackages}" var="packageData">
                                            <tr>
                                                <td>${packageData.id}</td>
                                                <td>${packageData.customer.username}</td>
                                                <td>${packageData.pickupAddress}</td>
                                                <td>${packageData.deliveryAddress}</td>
                                                <td>${packageData.weight} kg</td>
                                                <td>
                                                    <form action="/courier/delivery/take" method="POST" style="display: inline;">
                                                        <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                        <button type="submit" class="btn btn-primary btn-sm">Take Delivery</button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div class="tab-pane fade" id="active-deliveries">
                            <h3>Active Deliveries</h3>
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Customer</th>
                                            <th>Pickup</th>
                                            <th>Delivery</th>
                                            <th>Status</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody id="active-deliveries-table">
                                        <c:forEach items="${activeDeliveries}" var="packageData">
                                            <tr>
                                                <td>${packageData.id}</td>
                                                <td>${packageData.customer.username}</td>
                                                <td>${packageData.pickupAddress}</td>
                                                <td>${packageData.deliveryAddress}</td>
                                                <td>${packageData.status}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${packageData.status eq 'ASSIGNED'}">
                                                            <form action="/courier/delivery/update-status" method="POST" style="display: inline;">
                                                                <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                <input type="hidden" name="status" value="PICKED_UP"/>
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="btn btn-info btn-sm">Mark as Picked Up</button>
                                                            </form>
                                                            <form action="/courier/delivery/drop" method="POST" style="display: inline;">
                                                                <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="btn btn-danger btn-sm">Drop Delivery</button>
                                                            </form>
                                                        </c:when>
                                                        <c:when test="${packageData.status eq 'PICKED_UP'}">
                                                            <form action="/courier/delivery/update-status" method="POST" style="display: inline;">
                                                                <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                <input type="hidden" name="status" value="IN_TRANSIT"/>
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="btn btn-warning btn-sm">Start Delivery</button>
                                                            </form>
                                                            <form action="/courier/delivery/drop" method="POST" style="display: inline;">
                                                                <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="btn btn-danger btn-sm">Drop Delivery</button>
                                                            </form>
                                                        </c:when>
                                                        <c:when test="${packageData.status eq 'IN_TRANSIT'}">
                                                            <form action="/courier/delivery/update-status" method="POST" style="display: inline;">
                                                                <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                <input type="hidden" name="status" value="DELIVERED"/>
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="btn btn-success btn-sm">Mark as Delivered</button>
                                                            </form>
                                                            <form action="/courier/delivery/drop" method="POST" style="display: inline;">
                                                                <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="btn btn-danger btn-sm">Drop Delivery</button>
                                                            </form>
                                                        </c:when>
                                                        <c:when test="${packageData.status eq 'DELIVERED'}">
                                                            <span class="text-success">Delivered</span>
                                                        </c:when>
                                                    </c:choose>
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
    <script>
        let map;
        let markers = new Map();
        let geocoder;
        let directionsService;
        let directionsRenderer;

        function initMap() {
            const defaultLocation = { lat: 41.0082, lng: 28.9784 }; // Istanbul coordinates
            
            map = new google.maps.Map(document.getElementById('map'), {
                center: defaultLocation,
                zoom: 12
            });

            geocoder = new google.maps.Geocoder();
            directionsService = new google.maps.DirectionsService();
            directionsRenderer = new google.maps.DirectionsRenderer();
            directionsRenderer.setMap(map);

            // Get user's location
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    (position) => {
                        const pos = {
                            lat: position.coords.latitude,
                            lng: position.coords.longitude
                        };
                        map.setCenter(pos);
                        // Add user marker
                        new google.maps.Marker({
                            position: pos,
                            map: map,
                            title: 'Your Location',
                            icon: {
                                url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'
                            }
                        });
                    },
                    () => {
                        console.log('Error: The Geolocation service failed.');
                        map.setCenter(defaultLocation);
                    }
                );
            }

            // Add markers for available packages
            const availablePackages = Array.from(document.querySelectorAll('#available-packages-table tr'));
            availablePackages.forEach(packageRow => {
                const pickupAddress = packageRow.querySelector('td:nth-child(3)').textContent;
                const deliveryAddress = packageRow.querySelector('td:nth-child(4)').textContent;
                
                // Add pickup location marker
                geocodeAndAddMarker(pickupAddress, 'pickup');
                // Add delivery location marker
                geocodeAndAddMarker(deliveryAddress, 'delivery');
            });
        }

        function geocodeAndAddMarker(address, type) {
            geocoder.geocode({ address: address }, (results, status) => {
                if (status === 'OK' && results[0]) {
                    const position = results[0].geometry.location;
                    new google.maps.Marker({
                        map: map,
                        position: position,
                        title: type === 'pickup' ? 'Pickup Location' : 'Delivery Location',
                        icon: {
                            url: type === 'pickup' ? 
                                'http://maps.google.com/mapfiles/ms/icons/green-dot.png' : 
                                'http://maps.google.com/mapfiles/ms/icons/red-dot.png'
                        }
                    });
                }
            });
        }

        // Initialize map when the page loads
        document.addEventListener('DOMContentLoaded', function() {
            initMap();
        });
    </script>
</body>
</html>