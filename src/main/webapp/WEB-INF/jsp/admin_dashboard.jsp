<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Admin Dashboard" />

<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<%@ include file="common/topbar.jsp" %>

<!-- Add required libraries -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>
<link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.css" rel="stylesheet">

<script>
// Configure toastr options
toastr.options = {
    "closeButton": true,
    "debug": false,
    "newestOnTop": true,
    "progressBar": true,
    "positionClass": "toast-top-right",
    "preventDuplicates": false,
    "onclick": null,
    "showDuration": "300",
    "hideDuration": "1000",
    "timeOut": "5000",
    "extendedTimeOut": "1000",
    "showEasing": "swing",
    "hideEasing": "linear",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut",
    "containerId": "toast-container"
};
</script>

<!-- Page Heading -->
<div class="d-sm-flex align-items-center justify-content-between mb-4">
    <h1 class="h3 mb-0 text-gray-800">Admin Dashboard</h1>
    <div>
        <button class="btn btn-primary btn-sm shadow-sm mr-2" data-toggle="modal" data-target="#createUserModal">
            <i class="fas fa-user-plus fa-sm text-white-50"></i> Create User
        </button>
        <button class="btn btn-info btn-sm shadow-sm mr-2" data-toggle="modal" data-target="#createPackageModal">
            <i class="fas fa-box-open fa-sm text-white-50"></i> Create Package
        </button>
        <a href="/admin/reports" class="d-none d-sm-inline-block btn btn-sm btn-success shadow-sm">
            <i class="fas fa-download fa-sm text-white-50"></i> Generate Report
        </a>
    </div>
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
    <!-- Users Table -->
    <div class="col-12">
        <div class="card shadow mb-4">
            <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                <h6 class="m-0 font-weight-bold text-primary">User Management</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered" id="usersTable" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Username</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${users}" var="user">
                                <tr>
                                    <td>${user.id}</td>
                                    <td>${user.username}</td>
                                    <td>${user.email}</td>
                                    <td><span class="badge badge-info">${user.role}</span></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${user.role eq 'COURIER'}">
                                                <c:choose>
                                                    <c:when test="${user.available}">
                                                        <span class="badge badge-success">Available</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-warning">Busy</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-secondary">N/A</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <button class="btn btn-info btn-sm" onclick="editUser('${user.id}')">
                                            <i class="fas fa-edit"></i>
                                        </button>
                                        <button class="btn btn-danger btn-sm" onclick="deleteUser('${user.id}')">
                                            <i class="fas fa-trash"></i>
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
    <!-- Packages Table -->
    <div class="col-12">
        <div class="card shadow mb-4">
            <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                <h6 class="m-0 font-weight-bold text-primary">Package Management</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered" id="packagesTable" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Customer</th>
                                <th>Courier</th>
                                <th>Status</th>
                                <th>Created At</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${packages}" var="deliveryPackage">
                                <tr>
                                    <td>${deliveryPackage.id}</td>
                                    <td>${deliveryPackage.trackingNumber}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${deliveryPackage.courier != null}">
                                                ${deliveryPackage.courier.username}
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">Not Assigned</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${deliveryPackage.status eq 'PENDING'}">
                                                <span class="badge badge-warning">${deliveryPackage.status}</span>
                                            </c:when>
                                            <c:when test="${deliveryPackage.status eq 'PICKED_UP'}">
                                                <span class="badge badge-info">${deliveryPackage.status}</span>
                                            </c:when>
                                            <c:when test="${deliveryPackage.status eq 'IN_TRANSIT'}">
                                                <span class="badge badge-primary">${deliveryPackage.status}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-success">${deliveryPackage.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${deliveryPackage.createdAt}</td>
                                    <td>
                                        <button class="btn btn-info btn-sm" onclick="viewPackage('${deliveryPackage.id}')">
                                            <i class="fas fa-eye"></i>
                                        </button>
                                        <button class="btn btn-success btn-sm" onclick="generateReport('${deliveryPackage.id}')" 
                                                ${deliveryPackage.status ne 'DELIVERED' ? 'disabled' : ''}>
                                            <i class="fas fa-file-alt"></i>
                                        </button>
                                        <button class="btn btn-danger btn-sm" onclick="deletePackage('${deliveryPackage.id}')">
                                            <i class="fas fa-trash"></i>
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

<!-- Reports Table -->
<div class="row">
    <div class="col-12">
        <div class="card shadow mb-4">
            <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                <h6 class="m-0 font-weight-bold text-primary">Delivery Reports</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered" id="reportsTable" width="100%" cellspacing="0">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Package ID</th>
                                <th>Courier</th>
                                <th>Delivery Time</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${reports}" var="report">
                                <tr>
                                    <td>${report.id}</td>
                                    <td>${report.deliveryPackage.id}</td>
                                    <td>${report.courier.username}</td>
                                    <td>${report.deliveryTime}</td>
                                    <td>
                                        <span class="badge badge-success">${report.status}</span>
                                    </td>
                                    <td>
                                        <button class="btn btn-info btn-sm" onclick="viewReport('${report.id}')">
                                            <i class="fas fa-eye"></i>
                                        </button>
                                        <button class="btn btn-danger btn-sm" onclick="deleteReport('${report.id}')">
                                            <i class="fas fa-trash"></i>
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

<!-- View Report Modal -->
<div class="modal fade" id="viewReportModal" tabindex="-1" role="dialog" aria-labelledby="viewReportModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="viewReportModalLabel">Delivery Report Details</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <pre id="reportContent" class="bg-light p-3" style="white-space: pre-wrap;"></pre>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" onclick="printReport()">Print Report</button>
            </div>
        </div>
    </div>
</div>

<!-- Create User Modal -->
<div class="modal fade" id="createUserModal" tabindex="-1" role="dialog" aria-labelledby="createUserModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="createUserModalLabel">Create New User</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="createUserForm">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" class="form-control" id="username" name="username" required>
                    </div>
                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email" class="form-control" id="email" name="email" required>
                    </div>
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" class="form-control" id="password" name="password" required>
                    </div>
                    <div class="form-group">
                        <label for="role">Role</label>
                        <select class="form-control" id="role" name="role" required>
                            <option value="CUSTOMER">Customer</option>
                            <option value="COURIER">Courier</option>
                            <option value="ADMIN">Admin</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="phoneNumber">Phone Number</label>
                        <input type="text" class="form-control" id="phoneNumber" name="phoneNumber">
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" onclick="createUser()">Create User</button>
            </div>
        </div>
    </div>
</div>

<script>
$(document).ready(function() {
    // Initialize DataTables
    $('#usersTable').DataTable({
        "order": [[0, "desc"]],
        "pageLength": 10,
        "responsive": true
    });

    $('#packagesTable').DataTable({
        "order": [[0, "desc"]],
        "pageLength": 10,
        "responsive": true
    });

    // Load initial data
    refreshDashboardStats();
});

function refreshDashboardStats() {
    fetch('/api/admin/stats')
        .then(response => response.json())
        .then(data => {
            // Update dashboard cards
            updateDashboardCards(data);
        })
        .catch(error => {
            console.error('Error:', error);
            toastr.error('Failed to load dashboard statistics');
        });
}

function updateDashboardCards(data) {
    // Update the statistics in the cards
    document.querySelector('[data-stat="totalUsers"]').textContent = data.totalUsers;
    document.querySelector('[data-stat="activeCouriers"]').textContent = data.activeCouriers;
    document.querySelector('[data-stat="pendingPackages"]').textContent = data.pendingPackages;
    document.querySelector('[data-stat="totalDeliveries"]').textContent = data.totalDeliveries;
}

function createUser() {
    const form = document.getElementById('createUserForm');
    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    fetch('/api/auth/signup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        if (data.message) {
            toastr.success('User created successfully');
            $('#createUserModal').modal('hide');
            location.reload();
        } else {
            throw new Error(data.error || 'Failed to create user');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        toastr.error(error.message);
    });
}

function editUser(userId) {
    $.ajax({
        url: `/api/admin/users/${userId}`,
        type: 'GET',
        success: function(response) {
            if (response.status === 'success') {
                // Populate edit modal with user data
                $('#editUserId').val(response.data.id);
                $('#editUsername').val(response.data.username);
                $('#editEmail').val(response.data.email);
                $('#editRole').val(response.data.role);
                $('#editUserModal').modal('show');
            } else {
                toastr.error('Failed to load user details');
            }
        },
        error: function() {
            toastr.error('Failed to load user details');
        }
    });
}

function deleteUser(userId) {
    if (confirm('Are you sure you want to delete this user?')) {
        $.ajax({
            url: `/api/admin/users/${userId}`,
            type: 'DELETE',
            success: function(response) {
                if (response.status === 'success') {
                    toastr.success('User deleted successfully');
                    location.reload();
                } else {
                    toastr.error(response.message || 'Failed to delete user');
                }
            },
            error: function() {
                toastr.error('Failed to delete user');
            }
        });
    }
}

function viewPackage(packageId) {
    $.ajax({
        url: `/api/packages/${packageId}`,
        type: 'GET',
        success: function(response) {
            if (response.status === 'success') {
                // Populate view modal with package data
                $('#viewPackageId').text(response.data.id);
                $('#viewTrackingNumber').text(response.data.trackingNumber);
                $('#viewCustomer').text(response.data.customer.username);
                $('#viewStatus').text(response.data.status);
                $('#viewCreatedAt').text(response.data.createdAt);
                $('#viewPackageModal').modal('show');
            } else {
                toastr.error('Failed to load package details');
            }
        },
        error: function() {
            toastr.error('Failed to load package details');
        }
    });
}

function deletePackage(packageId) {
    if (confirm('Are you sure you want to delete this package?')) {
        $.ajax({
            url: `/api/admin/packages/${packageId}`,
            type: 'DELETE',
            success: function(response) {
                if (response.status === 'success') {
                    toastr.success('Package deleted successfully');
                    location.reload();
                } else {
                    toastr.error(response.message || 'Failed to delete package');
                }
            },
            error: function() {
                toastr.error('Failed to delete package');
            }
        });
    }
}

function viewReport(reportId) {
    fetch(`/api/delivery-reports/${reportId}`)
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                document.getElementById('reportContent').textContent = data.data.content;
                $('#viewReportModal').modal('show');
            } else {
                toastr.error('Failed to load report details');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            toastr.error('Failed to load report details');
        });
}

function generateReport(packageId) {
    $.ajax({
        url: `/api/delivery-reports/generate/${packageId}`,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            username: '${user.username}'  // Get the logged-in admin's username
        }),
        success: function(response) {
            if (response.status === 'success') {
                toastr.success('Report generated successfully');
                window.location.href = '/admin/reports';
            } else {
                toastr.error(response.message || 'Failed to generate report');
            }
        },
        error: function(xhr) {
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Failed to generate report';
            toastr.error(errorMessage);
        }
    });
}

function deleteReport(reportId) {
    if (confirm('Are you sure you want to delete this report?')) {
        fetch(`/api/delivery-reports/${reportId}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                toastr.success('Report deleted successfully');
                location.reload();
            } else {
                toastr.error(data.message || 'Failed to delete report');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            toastr.error('Failed to delete report');
        });
    }
}

function printReport() {
    const reportContent = document.getElementById('reportContent').textContent;
    const printWindow = window.open('', '', 'height=600,width=800');
    printWindow.document.write('<html><head><title>Delivery Report</title>');
    printWindow.document.write('<style>body { font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; }</style>');
    printWindow.document.write('</head><body>');
    printWindow.document.write('<pre>' + reportContent + '</pre>');
    printWindow.document.write('</body></html>');
    printWindow.document.close();
    printWindow.print();
}
</script>

<%@ include file="common/footer.jsp" %>
