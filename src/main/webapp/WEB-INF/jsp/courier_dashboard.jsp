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
        <div class="modern-switch-container">
            <form action="/api/courier/${user.id}/availability" method="POST" id="availabilityForm">
                <input type="hidden" name="_csrf" value="${_csrf.token}" />
                <div class="modern-switch">
                    <input type="checkbox" id="availabilityToggle" name="available" 
                           ${user.available ? 'checked' : ''}>
                    <label for="availabilityToggle" class="switch-label">
                        <div class="switch-indicator"></div>
                    </label>
                    <span class="switch-text ${user.available ? 'text-success' : 'text-muted'}">
                        Available for Deliveries
                    </span>
                </div>
            </form>
        </div>
    </div>

    <style>
        .modern-switch-container {
            display: flex;
            align-items: center;
        }

        .modern-switch {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .modern-switch input[type="checkbox"] {
            display: none;
        }

        .switch-label {
            position: relative;
            display: inline-block;
            width: 50px;
            height: 26px;
            background-color: #e9ecef;
            border-radius: 13px;
            cursor: pointer;
            transition: all 0.3s ease;
            margin: 0;
        }

        .switch-indicator {
            position: absolute;
            top: 3px;
            left: 3px;
            width: 20px;
            height: 20px;
            background-color: white;
            border-radius: 50%;
            transition: all 0.3s ease;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .modern-switch input[type="checkbox"]:checked + .switch-label {
            background-color: #1cc88a;
        }

        .modern-switch input[type="checkbox"]:checked + .switch-label .switch-indicator {
            transform: translateX(24px);
        }

        .switch-text {
            font-size: 0.875rem;
            font-weight: 500;
            white-space: nowrap;
        }

        .text-success {
            color: #1cc88a !important;
        }
    </style>

    <script>
        // Function to handle form submissions
        function handleFormSubmit(event, formId) {
            event.preventDefault();
            const form = document.getElementById(formId);
            const formData = new FormData(form);
            const url = form.action;

            // Disable the submit button
            const submitButton = form.querySelector('button[type="submit"]');
            const originalButtonHtml = submitButton.innerHTML;
            submitButton.disabled = true;
            submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';

            fetch(url, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    submitButton.innerHTML = '<i class="fas fa-check"></i> Success!';
                    submitButton.classList.remove('btn-primary', 'btn-info', 'btn-warning', 'btn-danger');
                    submitButton.classList.add('btn-success');
                    
                    // Refresh the page after 1 second
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                } else {
                    throw new Error('Network response was not ok');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                submitButton.innerHTML = '<i class="fas fa-exclamation-triangle"></i> Error';
                submitButton.classList.add('btn-danger');
                
                // Reset button after 2 seconds
                setTimeout(() => {
                    submitButton.disabled = false;
                    submitButton.innerHTML = originalButtonHtml;
                    submitButton.classList.remove('btn-danger');
                }, 2000);
            });
        }

        // Function to handle availability toggle
        function handleAvailabilityToggle(event) {
            const toggle = document.getElementById('availabilityToggle');
            const form = document.getElementById('availabilityForm');
            const formData = new FormData(form);
            
            // Update the available value based on the toggle state
            formData.set('available', toggle.checked);
            
            fetch(form.action, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.ok) {
                    const text = toggle.closest('.modern-switch').querySelector('.switch-text');
                    
                    if (toggle.checked) {
                        text.classList.remove('text-muted');
                        text.classList.add('text-success');
                        text.textContent = 'Available for Deliveries';
                    } else {
                        text.classList.remove('text-success');
                        text.classList.add('text-muted');
                        text.textContent = 'Not Available';
                    }

                    // Show success message
                    const message = toggle.checked ? 'You are now available for deliveries' : 'You are now offline';
                    const alertDiv = document.createElement('div');
                    alertDiv.className = `alert alert-${toggle.checked ? 'success' : 'secondary'} alert-dismissible fade show`;
                    alertDiv.innerHTML = `
                        <i class="fas fa-${toggle.checked ? 'check-circle' : 'info-circle'}"></i> ${message}
                        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    `;
                    form.parentNode.insertBefore(alertDiv, form);

                    // Remove the alert after 2 seconds
                    setTimeout(() => {
                        alertDiv.remove();
                        window.location.reload();
                    }, 2000);
                } else {
                    // Revert the toggle if request failed
                    toggle.checked = !toggle.checked;
                    throw new Error('Failed to update availability');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                // Show error message
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-danger alert-dismissible fade show';
                alertDiv.innerHTML = `
                    <i class="fas fa-exclamation-triangle"></i> Failed to update availability status
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                `;
                form.parentNode.insertBefore(alertDiv, form);

                // Remove the error alert after 3 seconds
                setTimeout(() => alertDiv.remove(), 3000);
            });
        }

        $(document).ready(function() {
            // Bind event handlers to forms
            document.getElementById('availabilityToggle').addEventListener('change', handleAvailabilityToggle);

            // Bind handlers to all delivery-related forms
            document.querySelectorAll('.delivery-form, .status-form, .drop-form').forEach(form => {
                form.addEventListener('submit', (e) => handleFormSubmit(e, form.id));
            });

            // Initialize DataTables
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
        });
    </script>

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

    <!-- Available Packages Table -->
    <div class="row">
        <div class="col-12">
            <div class="card shadow mb-4">
                <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 class="m-0 font-weight-bold text-primary">Available Packages</h6>
                </div>
                <div class="card-body">
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
                                            <form action="/api/packages/${packageData.id}/assign" method="POST" class="delivery-form" id="assignForm_${packageData.id}">
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

    <!-- Active Deliveries Table -->
    <div class="row">
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
                                                    <form action="/api/packages/${packageData.id}/status" method="POST" class="status-form" id="pickupForm_${packageData.id}" style="display: inline;">
                                                        <input type="hidden" name="username" value="${user.username}"/>
                                                        <input type="hidden" name="status" value="PICKED_UP"/>
                                                        <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-info btn-sm">
                                                            <i class="fas fa-box fa-sm"></i> Mark as Picked Up
                                                        </button>
                                                    </form>
                                                    <form action="/api/packages/${packageData.id}/drop" method="POST" class="drop-form" id="dropForm_${packageData.id}" style="display: inline;">
                                                        <input type="hidden" name="username" value="${user.username}"/>
                                                        <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-danger btn-sm">
                                                            <i class="fas fa-times fa-sm"></i> Drop Delivery
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:when test="${packageData.status eq 'PICKED_UP'}">
                                                    <form action="/api/packages/${packageData.id}/status" method="POST" class="status-form" id="startForm_${packageData.id}" style="display: inline;">
                                                        <input type="hidden" name="username" value="${user.username}"/>
                                                        <input type="hidden" name="status" value="IN_TRANSIT"/>
                                                        <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-warning btn-sm">
                                                            <i class="fas fa-truck fa-sm"></i> Start Delivery
                                                        </button>
                                                    </form>
                                                    <form action="/api/packages/${packageData.id}/drop" method="POST" class="drop-form" id="dropForm2_${packageData.id}" style="display: inline;">
                                                        <input type="hidden" name="username" value="${user.username}"/>
                                                        <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-danger btn-sm">
                                                            <i class="fas fa-times fa-sm"></i> Drop Delivery
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:when test="${packageData.status eq 'IN_TRANSIT'}">
                                                    <form action="/api/packages/${packageData.id}/status" method="POST" class="status-form" id="deliverForm_${packageData.id}" style="display: inline;">
                                                        <input type="hidden" name="username" value="${user.username}"/>
                                                        <input type="hidden" name="status" value="DELIVERED"/>
                                                        <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                        <button type="submit" class="btn btn-success btn-sm">
                                                            <i class="fas fa-check fa-sm"></i> Mark as Delivered
                                                        </button>
                                                    </form>
                                                    <form action="/api/packages/${packageData.id}/drop" method="POST" class="drop-form" id="dropForm3_${packageData.id}" style="display: inline;">
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

<%@ include file="common/footer.jsp" %>