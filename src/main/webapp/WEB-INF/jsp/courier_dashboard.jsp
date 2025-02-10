<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="Courier Dashboard" />

<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<%@ include file="common/topbar.jsp" %>

<!-- Begin Page Content -->
<div class="container-fluid">

    <!-- Page Heading -->
    <div class="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 class="h3 mb-0 text-gray-800">Courier Dashboard</h1>
        <div class="d-none d-sm-inline-block">
            <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" id="availabilityToggle" ${user.available ? 'checked' : ''}>
                <label class="form-check-label text-gray-800" for="availabilityToggle">Available for Deliveries</label>
            </div>
        </div>
    </div>

    <!-- Content Row -->
    <div class="row">
        <!-- Active Deliveries Card -->
        <div class="col-xl-6 col-md-6 mb-4">
            <div class="card border-left-primary shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                Active Deliveries</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800" id="activeDeliveriesCount">${activeDeliveries.size()}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-truck fa-2x text-gray-300"></i>
                </div>
                    </div>
                </div>
                </div>
        </div>

        <!-- Available Packages Card -->
        <div class="col-xl-6 col-md-6 mb-4">
            <div class="card border-left-success shadow h-100 py-2">
                                        <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                Available Packages</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">${availablePackages.size()}</div>
                                        </div>
                        <div class="col-auto">
                            <i class="fas fa-box fa-2x text-gray-300"></i>
                                        </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

    <!-- Content Row -->
    <div class="row">
        <!-- Available Packages Table -->
        <div class="col-12">
            <div class="card shadow mb-4">
                <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 class="m-0 font-weight-bold text-primary">Available Packages</h6>
                                </div>
                                <div class="card-body">
                                    <div id="map" class="mb-4" style="height: 400px;"></div>
                    <div class="table-responsive">
                        <table class="table table-bordered" id="availablePackagesTable" width="100%" cellspacing="0">
                                        <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>Tracking #</th>
                                                <th>Customer</th>
                                                <th>Pickup</th>
                                                <th>Delivery</th>
                                                <th>Weight</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${availablePackages}" var="packageData">
                                                <tr>
                                                    <td>${packageData.id}</td>
                                        <td><span class="badge badge-secondary">${packageData.trackingNumber}</span></td>
                                                    <td>${packageData.customer.username}</td>
                                                    <td>${packageData.pickupAddress}</td>
                                                    <td>${packageData.deliveryAddress}</td>
                                                    <td>${packageData.weight} kg</td>
                                                    <td>
                                                        <form action="/api/packages/${packageData.id}/assign" method="POST" class="delivery-form">
                                                            <input type="hidden" name="username" value="${user.username}"/>
                                                            <input type="hidden" name="courierId" value="${user.id}"/>
                                                            <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                            <input type="hidden" name="packageId" value="${packageData.id}" />
                                                <button type="submit" class="btn btn-primary btn-sm">
                                                    <i class="fas fa-truck fa-sm"></i> Take Delivery
                                                </button>
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

    <!-- Content Row -->
    <div class="row">
        <!-- Active Deliveries Table -->
        <div class="col-12">
            <div class="card shadow mb-4">
                <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 class="m-0 font-weight-bold text-primary">Active Deliveries</h6>
                                </div>
                                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-bordered" id="activeDeliveriesTable" width="100%" cellspacing="0">
                                        <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>Tracking #</th>
                                                <th>Customer</th>
                                                <th>Pickup</th>
                                                <th>Delivery</th>
                                                <th>Status</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${activeDeliveries}" var="packageData">
                                                <tr>
                                                    <td>${packageData.id}</td>
                                        <td><span class="badge badge-secondary">${packageData.trackingNumber}</span></td>
                                                    <td>${packageData.customer.username}</td>
                                                    <td>${packageData.pickupAddress}</td>
                                                    <td>${packageData.deliveryAddress}</td>
                                        <td>
                                            <span class="badge badge-${packageData.status eq 'DELIVERED' ? 'success' : 
                                                packageData.status eq 'IN_TRANSIT' ? 'warning' : 'info'}">
                                                ${packageData.status}
                                            </span>
                                        </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${packageData.status eq 'ASSIGNED'}">
                                                                <form action="/api/packages/${packageData.id}/status" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="username" value="${user.username}"/>
                                                                    <input type="hidden" name="status" value="PICKED_UP"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-info btn-sm">
                                                            <i class="fas fa-box fa-sm"></i> Mark as Picked Up
                                                        </button>
                                                                </form>
                                                                <form action="/api/packages/${packageData.id}/drop" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="username" value="${user.username}"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-danger btn-sm">
                                                            <i class="fas fa-times fa-sm"></i> Drop Delivery
                                                        </button>
                                                                </form>
                                                            </c:when>
                                                            <c:when test="${packageData.status eq 'PICKED_UP'}">
                                                                <form action="/api/packages/${packageData.id}/status" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="username" value="${user.username}"/>
                                                                    <input type="hidden" name="status" value="IN_TRANSIT"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-warning btn-sm">
                                                            <i class="fas fa-truck fa-sm"></i> Start Delivery
                                                        </button>
                                                                </form>
                                                                <form action="/api/packages/${packageData.id}/drop" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="username" value="${user.username}"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-danger btn-sm">
                                                            <i class="fas fa-times fa-sm"></i> Drop Delivery
                                                        </button>
                                                                </form>
                                                            </c:when>
                                                            <c:when test="${packageData.status eq 'IN_TRANSIT'}">
                                                                <form action="/api/packages/${packageData.id}/status" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="username" value="${user.username}"/>
                                                                    <input type="hidden" name="status" value="DELIVERED"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-success btn-sm">
                                                            <i class="fas fa-check fa-sm"></i> Mark as Delivered
                                                        </button>
                                                                </form>
                                                                <form action="/api/packages/${packageData.id}/drop" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="username" value="${user.username}"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-danger btn-sm">
                                                            <i class="fas fa-times fa-sm"></i> Drop Delivery
                                                        </button>
                                                                </form>
                                                            </c:when>
                                                            <c:when test="${packageData.status eq 'DELIVERED'}">
                                                    <span class="text-success">
                                                        <i class="fas fa-check-circle"></i> Delivered
                                                    </span>
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
<!-- End of Main Content -->

<script>
    $(document).ready(function() {
        // Initialize DataTables with Bootstrap styling
        $('#availablePackagesTable').DataTable({
            "dom": '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
                   '<"row"<"col-sm-12"tr>>' +
                   '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
            "language": {
                "lengthMenu": "_MENU_ records per page",
                "zeroRecords": "No packages available",
                "info": "Showing page _PAGE_ of _PAGES_",
                "infoEmpty": "No packages available",
                "infoFiltered": "(filtered from _MAX_ total records)",
                "search": "Search:",
                "paginate": {
                    "first": "First",
                    "last": "Last",
                    "next": "Next",
                    "previous": "Previous"
                }
            }
        });

        $('#activeDeliveriesTable').DataTable({
            "dom": '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
                   '<"row"<"col-sm-12"tr>>' +
                   '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
            "language": {
                "lengthMenu": "_MENU_ records per page",
                "zeroRecords": "No active deliveries",
                "info": "Showing page _PAGE_ of _PAGES_",
                "infoEmpty": "No active deliveries",
                "infoFiltered": "(filtered from _MAX_ total records)",
                "search": "Search:",
                "paginate": {
                    "first": "First",
                    "last": "Last",
                    "next": "Next",
                    "previous": "Previous"
                }
            }
        });

        // Initialize map
        initMap();

        // Handle availability toggle
        $('#availabilityToggle').change(function(event) {
            handleAvailabilityToggle(event);
        });

        // Handle delivery forms
        $('.delivery-form').submit(function(e) {
            e.preventDefault();
            const form = $(this);
            const submitButton = form.find('button[type="submit"]');
            const originalButtonText = submitButton.html();
            
            submitButton.prop('disabled', true)
                       .html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Processing...');

            $.ajax({
                url: form.attr('action'),
                type: 'POST',
                data: form.serialize(),
                success: function(response) {
                    if (response.status === 'success') {
                        toastr.success(response.message || 'Operation completed successfully');
                        setTimeout(() => window.location.reload(), 1000);
                    } else {
                        toastr.error(response.message || 'Operation failed');
                        submitButton.prop('disabled', false).html(originalButtonText);
                    }
                },
                error: function(xhr) {
                    toastr.error(xhr.responseJSON?.message || 'Operation failed');
                    submitButton.prop('disabled', false).html(originalButtonText);
                }
            });
        });
    });
    </script>

<%@ include file="common/footer.jsp" %>