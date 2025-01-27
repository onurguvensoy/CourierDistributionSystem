<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Courier Dashboard - Courier Distribution System</title>
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
                                            <tr data-package-id="${packageData.id}">
                                                <td>${packageData.id}</td>
                                                <td>${packageData.customer.username}</td>
                                                <td>${packageData.pickupAddress}</td>
                                                <td>${packageData.deliveryAddress}</td>
                                                <td>${packageData.weight} kg</td>
                                                <td>
                                                    <button onclick="takeDelivery(${packageData.id})" class="btn btn-primary btn-sm">Take Delivery</button>
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
                                            <tr data-package-id="${packageData.id}">
                                                <td>${packageData.id}</td>
                                                <td>${packageData.customer.username}</td>
                                                <td>${packageData.pickupAddress}</td>
                                                <td>${packageData.deliveryAddress}</td>
                                                <td>${packageData.status}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${packageData.status == 'ASSIGNED'}">
                                                            <button onclick="updateDeliveryStatus(${packageData.id}, 'PICKED_UP')" class="btn btn-info btn-sm">Mark as Picked Up</button>
                                                        </c:when>
                                                        <c:when test="${packageData.status == 'PICKED_UP'}">
                                                            <button onclick="updateDeliveryStatus(${packageData.id}, 'IN_TRANSIT')" class="btn btn-warning btn-sm">Start Delivery</button>
                                                        </c:when>
                                                        <c:when test="${packageData.status == 'IN_TRANSIT'}">
                                                            <button onclick="updateDeliveryStatus(${packageData.id}, 'DELIVERED')" class="btn btn-success btn-sm">Mark as Delivered</button>
                                                        </c:when>
                                                        <c:when test="${packageData.status == 'DELIVERED'}">
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
    <script async defer
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyB9TgwsRIwLSpC3wEXhzvbpG7C9kTIZjKI&libraries=places&callback=initMap">
    </script>
    <script>
        let map;
        let markers = new Map();
        let geocoder;
        let directionsService;
        let directionsRenderer;
        let stompClient = null;
        
        function connect() {
            const socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            
            stompClient.connect({}, function(frame) {
                console.log('Connected to WebSocket');
                
                // Subscribe to package updates
                stompClient.subscribe('/topic/packages', function(message) {
                    const data = JSON.parse(message.body);
                    handleDeliveryUpdate(data);
                });
                
                // Subscribe to courier-specific updates
                stompClient.subscribe('/topic/courier/${user.username}/packages', function(message) {
                    const data = JSON.parse(message.body);
                    handleDeliveryUpdate(data);
                });
            });
        }
        
        function takeDelivery(deliveryId) {
            if (!deliveryId) {
                console.error('Delivery ID is undefined');
                return;
            }
            
            const username = '${user.username}';
            fetch(`/api/deliveries/${deliveryId}/assign?username=${username}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    console.log('Delivery taken successfully');
                    const row = document.querySelector(`#available-packages-table tr[data-delivery-id="${deliveryId}"]`);
                    if (row) {
                        row.remove();
                    }
                    // Add to active deliveries
                    const delivery = data.data;
                    const activeTable = document.getElementById('active-deliveries-table');
                    const newRow = document.createElement('tr');
                    newRow.setAttribute('data-delivery-id', delivery.id);
                    newRow.innerHTML = `
                        <td>${delivery.id}</td>
                        <td>${delivery.customer.username}</td>
                        <td>${delivery.pickupAddress}</td>
                        <td>${delivery.deliveryAddress}</td>
                        <td>${delivery.status}</td>
                        <td>
                            <button onclick="updateDeliveryStatus(${delivery.id}, 'PICKED_UP')" class="btn btn-info btn-sm">Mark as Picked Up</button>
                        </td>
                    `;
                    activeTable.appendChild(newRow);
                } else {
                    throw new Error(data.message || 'Failed to take delivery');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Failed to take delivery: ' + error.message);
            });
        }
        
        function updateDeliveryStatus(deliveryId, newStatus) {
            if (!deliveryId) {
                console.error('Delivery ID is undefined');
                return;
            }
            
            const username = '${user.username}';
            fetch(`/api/deliveries/${deliveryId}/status?username=${username}&status=${newStatus}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    console.log('Status updated successfully');
                    const delivery = data.data;
                    updateDeliveryInTables(delivery);
                    if (map) updateDeliveryOnMap(delivery);
                } else {
                    throw new Error(data.message || 'Failed to update status');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Failed to update status: ' + error.message);
            });
        }
        
        function handleDeliveryUpdate(data) {
            switch(data.type) {
                case 'NEW_PACKAGE':
                    if (data.status === 'PENDING') {
                        addDeliveryToAvailableTable(data);
                        if (map) addDeliveryToMap(data);
                    }
                    break;
                case 'STATUS_UPDATE':
                    updateDeliveryInTables(data);
                    if (map) updateDeliveryOnMap(data);
                    break;
                case 'LOCATION_UPDATE':
                    if (map) updateDeliveryLocation(data);
                    break;
            }
        }
        
        function addDeliveryToAvailableTable(deliveryData) {
            const tbody = document.getElementById('available-packages-table');
            const existingRow = document.querySelector(`tr[data-delivery-id="${deliveryData.id}"]`);
            if (existingRow) return;
            
            const row = document.createElement('tr');
            row.setAttribute('data-delivery-id', deliveryData.id);
            row.innerHTML = 
                '<td>' + deliveryData.id + '</td>' +
                '<td>' + deliveryData.customerUsername + '</td>' +
                '<td>' + deliveryData.pickupAddress + '</td>' +
                '<td>' + deliveryData.deliveryAddress + '</td>' +
                '<td>' + deliveryData.weight + ' kg</td>' +
                '<td>' +
                    '<button onclick="takeDelivery(' + deliveryData.id + ')" class="btn btn-primary btn-sm">Take Delivery</button>' +
                '</td>';
            tbody.appendChild(row);
        }
        
        function updateDeliveryInTables(deliveryData) {
            const availableRow = document.querySelector(`#available-packages-table tr[data-delivery-id="${deliveryData.id}"]`);
            const activeRow = document.querySelector(`#active-deliveries-table tr[data-delivery-id="${deliveryData.id}"]`);
            
            if (deliveryData.status === 'PENDING' && !availableRow) {
                addDeliveryToAvailableTable(deliveryData);
            } else if (deliveryData.status !== 'PENDING' && availableRow) {
                availableRow.remove();
            }
            
            if (activeRow) {
                activeRow.querySelector('td:nth-child(5)').textContent = deliveryData.status;
                const actionsCell = activeRow.querySelector('td:last-child');
                
                switch (deliveryData.status) {
                    case 'ASSIGNED':
                        actionsCell.innerHTML = '<button onclick="updateDeliveryStatus(' + deliveryData.id + ', \'PICKED_UP\')" class="btn btn-info btn-sm">Mark as Picked Up</button>';
                        break;
                    case 'PICKED_UP':
                        actionsCell.innerHTML = '<button onclick="updateDeliveryStatus(' + deliveryData.id + ', \'IN_TRANSIT\')" class="btn btn-warning btn-sm">Start Delivery</button>';
                        break;
                    case 'IN_TRANSIT':
                        actionsCell.innerHTML = '<button onclick="updateDeliveryStatus(' + deliveryData.id + ', \'DELIVERED\')" class="btn btn-success btn-sm">Mark as Delivered</button>';
                        break;
                    case 'DELIVERED':
                        actionsCell.innerHTML = '<span class="text-success">Delivered</span>';
                        break;
                }
            }
        }
    </script>
</body>
</html>