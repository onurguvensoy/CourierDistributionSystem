<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
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
            border-radius: 8px;
            margin-top: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .map-container {
            position: relative;
            margin-bottom: 20px;
        }
        .card {
            background-color: rgba(255, 255, 255, 0.9);
            border-radius: 8px;
            transition: transform 0.2s;
            margin-bottom: 20px;
        }
        .card:hover {
            transform: translateY(-2px);
        }
        .table {
            background-color: white;
            border-radius: 8px;
            margin-top: 20px;
        }
        .stat-card {
            padding: 20px;
            text-align: center;
        }
        .stat-card h5 {
            color: #495057;
            margin-bottom: 10px;
        }
        .stat-card h3 {
            font-size: 2.5rem;
            margin: 0;
            color: #0d6efd;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="dashboard-container">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2>Admin Dashboard</h2>
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
                        <a class="nav-link" href="#users" data-bs-toggle="pill">Manage Users</a>
                        <a class="nav-link" href="#packages" data-bs-toggle="pill">Packages</a>
                        <a class="nav-link" href="#deliveries" data-bs-toggle="pill">Deliveries</a>
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
                            <h3 class="mb-4">System Overview</h3>
                            <div class="row">
                                <div class="col-md-3">
                                    <div class="card">
                                        <div class="stat-card">
                                            <h5>Total Users</h5>
                                            <h3 id="totalUsers">0</h3>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="card">
                                        <div class="stat-card">
                                            <h5>Customers</h5>
                                            <h3 id="totalCustomers">0</h3>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="card">
                                        <div class="stat-card">
                                            <h5>Couriers</h5>
                                            <h3 id="totalCouriers">0</h3>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="card">
                                        <div class="stat-card">
                                            <h5>Admins</h5>
                                            <h3 id="totalAdmins">0</h3>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Users Tab -->
                        <div class="tab-pane fade" id="users">
                            <div class="d-flex justify-content-between align-items-center mb-4">
                                <h3>User Management</h3>
                                <div>
                                    <button class="btn btn-primary" onclick="refreshUserList()">
                                        <i class="bi bi-arrow-clockwise"></i> Refresh
                                    </button>
                                </div>
                            </div>

                            <!-- User List Tabs -->
                            <ul class="nav nav-tabs mb-3">
                                <li class="nav-item">
                                    <a class="nav-link active" data-bs-toggle="tab" href="#allUsers">All Users</a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" data-bs-toggle="tab" href="#customersList">Customers</a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" data-bs-toggle="tab" href="#couriersList">Couriers</a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" data-bs-toggle="tab" href="#adminsList">Admins</a>
                                </li>
                            </ul>

                            <!-- User List Content -->
                            <div class="tab-content">
                                <div class="tab-pane fade show active" id="allUsers">
                                    <div class="table-responsive">
                                        <table class="table">
                                            <thead>
                                                <tr>
                                                    <th>ID</th>
                                                    <th>Username</th>
                                                    <th>Email</th>
                                                    <th>Role</th>
                                                    <th>Created At</th>
                                                    <th>Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody id="allUsersTable">
                                                <!-- Will be populated by JavaScript -->
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                                <!-- Similar tables for other user types -->
                            </div>
                        </div>

                        <!-- Other tabs remain the same -->
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Load user statistics
        function loadUserStats() {
            fetch('/api/users/stats')
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        document.getElementById('totalUsers').textContent = data.data.totalUsers;
                        document.getElementById('totalCustomers').textContent = data.data.totalCustomers;
                        document.getElementById('totalCouriers').textContent = data.data.totalCouriers;
                        document.getElementById('totalAdmins').textContent = data.data.totalAdmins;
                    }
                })
                .catch(error => console.error('Error loading user stats:', error));
        }

        // Load all users
        function loadAllUsers() {
            fetch('/api/users')
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        const tbody = document.getElementById('allUsersTable');
                        tbody.innerHTML = '';
                        data.data.forEach(user => {
                            tbody.innerHTML += `
                                <tr>
                                    <td>${user.id}</td>
                                    <td>${user.username}</td>
                                    <td>${user.email}</td>
                                    <td>${user.role}</td>
                                    <td><fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    <td>
                                        <button class="btn btn-sm btn-danger" onclick="deleteUser(${user.id}, '${user.role}')">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            `;
                        });
                    }
                })
                .catch(error => console.error('Error loading users:', error));
        }

        // Delete user
        function deleteUser(id, role) {
            if (confirm('Are you sure you want to delete this user?')) {
                fetch(`/api/users/${id}?role=${role}`, {
                    method: 'DELETE'
                })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        refreshUserList();
                    } else {
                        alert('Error deleting user: ' + data.message);
                    }
                })
                .catch(error => console.error('Error deleting user:', error));
            }
        }

        // Refresh user list and stats
        function refreshUserList() {
            loadUserStats();
            loadAllUsers();
        }

        // Initial load
        document.addEventListener('DOMContentLoaded', function() {
            refreshUserList();
        });
    </script>
</body>
</html>
