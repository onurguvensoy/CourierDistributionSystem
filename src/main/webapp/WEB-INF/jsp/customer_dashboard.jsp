<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Customer Dashboard - Courier Distribution System</title>
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
                            <form class="mt-4" action="/api/packages" method="POST" id="sendPackageForm">
                                <input type="hidden" name="username" value="${user.username}">
                                <div class="mb-3">
                                    <label class="form-label">Pickup Address</label>
                                    <input type="text" class="form-control" name="pickupAddress" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">Delivery Address</label>
                                    <input type="text" class="form-control" name="deliveryAddress" required>
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

                        <!-- Track Packages Tab -->
                        <div class="tab-pane fade" id="track-packages">
                            <h3>Track Packages</h3>
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
                                                <td>${deliveryPackage.courier != null ? deliveryPackage.courier.username : 'Not Assigned'}</td>
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
                
                // Subscribe to package updates for this customer
                stompClient.subscribe('/topic/customer/' + userId + '/package-updates', function(message) {
                    var deliveryPackage = JSON.parse(message.body);
                    updatePackageInTable(deliveryPackage);
                });
            });
        }

        function updatePackageInTable(deliveryPackage) {
            var row = document.querySelector('tr[data-package-id="' + deliveryPackage.id + '"]');
            if (row) {
                row.querySelector('.package-status').textContent = deliveryPackage.status;
                // Update courier if assigned
                var courierCell = row.cells[4];
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

        // Handle form submission
        document.getElementById('sendPackageForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
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
            .then(response => response.json())
            .then(package => {
                updatePackageInTable(package);
                this.reset();
                // Switch to track packages tab
                document.querySelector('a[href="#track-packages"]').click();
            })
            .catch(error => console.error('Error:', error));
        });

        // Connect when page loads
        document.addEventListener('DOMContentLoaded', function() {
            connect();
        });
    </script>
</body>
</html> 