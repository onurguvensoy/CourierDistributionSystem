<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Admin Dashboard" />

<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<%@ include file="common/topbar.jsp" %>

<!-- Page Heading -->
<div class="d-sm-flex align-items-center justify-content-between mb-4">
    <h1 class="h3 mb-0 text-gray-800">Admin Dashboard</h1>
    <a href="/admin/reports" class="d-none d-sm-inline-block btn btn-sm btn-primary shadow-sm">
        <i class="fas fa-download fa-sm text-white-50"></i> Generate Report
    </a>
</div>

<!-- Content Row -->
<div class="row">
    <!-- Total Users Card -->
    <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-primary shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                            Total Users</div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">${totalUsers}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-users fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Active Couriers Card -->
    <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-success shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                            Active Couriers</div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">${activeCouriers}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-truck fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Pending Packages Card -->
    <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-info shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                            Pending Packages</div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">${pendingPackages}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-box fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Total Deliveries Card -->
    <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-warning shadow h-100 py-2">
            <div class="card-body">
                <div class="row no-gutters align-items-center">
                    <div class="col mr-2">
                        <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                            Total Deliveries</div>
                        <div class="h5 mb-0 font-weight-bold text-gray-800">${totalDeliveries}</div>
                    </div>
                    <div class="col-auto">
                        <i class="fas fa-shipping-fast fa-2x text-gray-300"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Content Row -->
<div class="row">
    <!-- Recent Users Table -->
    <div class="col-12">
        <div class="card shadow mb-4">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Recent Users</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered" id="recentUsersTable" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${recentUsers}" var="user">
                                <tr>
                                    <td>${user.id}</td>
                                    <td>${user.firstName} ${user.lastName}</td>
                                    <td>${user.email}</td>
                                    <td>
                                        <span class="badge badge-${user.role == 'ADMIN' ? 'danger' : 
                                            user.role == 'COURIER' ? 'success' : 'primary'}">
                                            ${user.role}
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge badge-${user.active ? 'success' : 'danger'}">
                                            ${user.active ? 'Active' : 'Inactive'}
                                        </span>
                                    </td>
                                    <td>
                                        <button class="btn btn-primary btn-sm" onclick="editUser(${user.id})">
                                            <i class="fas fa-edit"></i>
                                        </button>
                                        <button class="btn btn-${user.active ? 'danger' : 'success'} btn-sm" 
                                            onclick="toggleUserStatus(${user.id}, ${user.active})">
                                            <i class="fas fa-${user.active ? 'ban' : 'check'}"></i>
                                        </button>
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

<!-- Content Row -->
<div class="row">
    <!-- Recent Deliveries Table -->
    <div class="col-12">
        <div class="card shadow mb-4">
            <div class="card-header py-3">
                <h6 class="m-0 font-weight-bold text-primary">Recent Deliveries</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered" id="recentDeliveriesTable" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Customer</th>
                                <th>Courier</th>
                                <th>Pickup Location</th>
                                <th>Delivery Location</th>
                                <th>Status</th>
                                <th>Created At</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${recentDeliveries}" var="delivery">
                                <tr>
                                    <td>${delivery.id}</td>
                                    <td>${delivery.customer.firstName} ${delivery.customer.lastName}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${delivery.courier != null}">
                                                ${delivery.courier.firstName} ${delivery.courier.lastName}
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">Not Assigned</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${delivery.pickupLocation}</td>
                                    <td>${delivery.deliveryLocation}</td>
                                    <td>
                                        <span class="badge badge-${delivery.status == 'PENDING' ? 'warning' : 
                                            delivery.status == 'PICKED_UP' ? 'info' : 
                                            delivery.status == 'IN_TRANSIT' ? 'primary' : 'success'}">
                                            ${delivery.status}
                                        </span>
                                    </td>
                                    <td>${delivery.createdAt}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        // Initialize DataTables
        $('#recentUsersTable').DataTable();
        $('#recentDeliveriesTable').DataTable();
    });

    function editUser(userId) {
        // Implement user edit functionality
        window.location.href = `/admin/users/${userId}/edit`;
    }

    function toggleUserStatus(userId, currentStatus) {
        const newStatus = !currentStatus;
        
        fetch(`/api/admin/users/${userId}/status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            },
            body: JSON.stringify({ active: newStatus })
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                toastr.success('User status updated successfully');
                setTimeout(() => location.reload(), 1000);
            } else {
                throw new Error(data.message || 'Failed to update user status');
            }
        })
        .catch(error => {
            toastr.error(error.message);
        });
    }
</script>

<%@ include file="common/footer.jsp" %>
