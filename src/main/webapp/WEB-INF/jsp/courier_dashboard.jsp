<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Courier Dashboard - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
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

        function connect() {
            var socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            stompClient.connect({}, function(frame) {
                console.log('Connected: ' + frame);
                
                // Subscribe to personal package updates
                stompClient.subscribe('/topic/courier/' + userId + '/package-updates', function(message) {
                    var deliveryPackage = JSON.parse(message.body);
                    updatePackageInTable(deliveryPackage);
                });

                // Subscribe to available packages
                stompClient.subscribe('/topic/couriers/available-packages', function(message) {
                    var deliveryPackage = JSON.parse(message.body);
                    addNewAvailablePackage(deliveryPackage);
                });
            });
        }

        function updatePackageInTable(deliveryPackage) {
            // Find and update package in active deliveries table
            var row = document.querySelector('tr[data-package-id="' + deliveryPackage.id + '"]');
            if (row) {
                // Update status
                row.querySelector('.package-status').textContent = deliveryPackage.status;
                
                // Update action buttons based on status
                var actionCell = row.querySelector('.package-actions');
                if (deliveryPackage.status === 'ASSIGNED') {
                    actionCell.innerHTML = '<button onclick="updateStatus(' + deliveryPackage.id + ', \'PICKED_UP\')" class="btn btn-info btn-sm">Mark as Picked Up</button>';
                } else if (deliveryPackage.status === 'PICKED_UP') {
                    actionCell.innerHTML = '<button onclick="updateStatus(' + deliveryPackage.id + ', \'DELIVERED\')" class="btn btn-success btn-sm">Mark as Delivered</button>';
                } else if (deliveryPackage.status === 'DELIVERED') {
                    actionCell.innerHTML = '<span class="text-success">Delivered</span>';
                }
            }
        }

        function addNewAvailablePackage(deliveryPackage) {
            if (deliveryPackage.status === 'PENDING') {
                var tbody = document.querySelector('#available-packages table tbody');
                var row = document.createElement('tr');
                row.setAttribute('data-package-id', deliveryPackage.id);
                row.innerHTML = `
                    <td>\${deliveryPackage.id}</td>
                    <td>\${deliveryPackage.customer.username}</td>
                    <td>\${deliveryPackage.pickupAddress}</td>
                    <td>\${deliveryPackage.deliveryAddress}</td>
                    <td>\${deliveryPackage.weight} kg</td>
                    <td>
                        <form action="/courier/take-package" method="POST" style="display: inline;">
                            <input type="hidden" name="packageId" value="\${deliveryPackage.id}">
                            <button type="submit" class="btn btn-primary btn-sm">Take Package</button>
                        </form>
                    </td>
                `;
                tbody.appendChild(row);
            }
        }

        // Connect when page loads
        document.addEventListener('DOMContentLoaded', function() {
            connect();
        });
    </script>
</body>
</html> 