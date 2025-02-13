<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="pageTitle" value="Courier Dashboard" />

<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<%@ include file="common/topbar.jsp" %>

<!-- Begin Page Content -->
<div class="container-fluid">
    <input type="hidden" id="username" value="${user.username}">
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

    <!-- Available Packages -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Available Packages</h6>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-bordered" id="availablePackagesTable" width="100%" cellspacing="0">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Pickup</th>
                            <th>Delivery</th>
                            <th>Weight</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${availablePackages}" var="package">
                            <tr id="package-row-${package.package_id}">
                                <td>${package.package_id}</td>
                                <td>${package.pickupAddress}</td>
                                <td>${package.deliveryAddress}</td>
                                <td>${package.weight} kg</td>
                                <td>
                                    <span id="package-status-${package.package_id}" 
                                          class="badge badge-${package.status == 'PENDING' ? 'warning' : 'info'}">
                                        ${package.status}
                                    </span>
                                </td>
                                <td>
                                    <button class="btn btn-primary btn-sm" 
                                            onclick="assignPackage('${package.package_id}')">
                                        <i class="fas fa-truck"></i> Take Package
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Active Deliveries -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Active Deliveries</h6>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-bordered" id="activeDeliveriesTable" width="100%" cellspacing="0">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Current Location</th>
                            <th>Delivery Address</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${activeDeliveries}" var="delivery">
                            <tr id="delivery-row-${delivery.package_id}">
                                <td>${delivery.package_id}</td>
                                <td id="location-${delivery.package_id}">
                                    ${delivery.currentLocation != null ? delivery.currentLocation : 'Not started'}
                                </td>
                                <td>${delivery.deliveryAddress}</td>
                                <td>
                                    <span id="delivery-status-${delivery.package_id}" 
                                          class="badge badge-${getStatusBadgeClass(delivery.status)}">
                                        ${delivery.status}
                                    </span>
                                </td>
                                <td>
                                    <div class="btn-group">
                                        <button class="btn btn-info btn-sm" 
                                                onclick="updateLocation('${delivery.package_id}')">
                                            <i class="fas fa-location-arrow"></i> Update Location
                                        </button>
                                        <button class="btn btn-success btn-sm" 
                                                onclick="updateStatus('${delivery.package_id}')">
                                            <i class="fas fa-check"></i> Update Status
                                        </button>
                                        <button class="btn btn-danger btn-sm" 
                                                onclick="dropPackage('${delivery.package_id}')">
                                            <i class="fas fa-times"></i> Drop
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Location Update Modal -->
<div class="modal fade" id="locationModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Update Location</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="locationForm">
                    <input type="hidden" id="locationPackageId">
                    <div class="form-group">
                        <label>Current Location</label>
                        <input type="text" class="form-control" id="locationInput" required>
                    </div>
                    <div class="form-group">
                        <label>Latitude</label>
                        <input type="number" step="0.000001" class="form-control" id="latitudeInput" required>
                    </div>
                    <div class="form-group">
                        <label>Longitude</label>
                        <input type="number" step="0.000001" class="form-control" id="longitudeInput" required>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" onclick="submitLocationUpdate()">Update</button>
            </div>
        </div>
    </div>
</div>

<!-- Status Update Modal -->
<div class="modal fade" id="statusModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Update Status</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="statusForm">
                    <input type="hidden" id="statusPackageId">
                    <div class="form-group">
                        <label>New Status</label>
                        <select class="form-control" id="statusSelect" required>
                            <option value="PICKED_UP">Picked Up</option>
                            <option value="IN_TRANSIT">In Transit</option>
                            <option value="DELIVERED">Delivered</option>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" onclick="submitStatusUpdate()">Update</button>
            </div>
        </div>
    </div>
</div>


<script>
let currentPackageId = null;

function updateLocation(packageId) {
    currentPackageId = packageId;
    $('#locationPackageId').val(packageId);
    $('#locationModal').modal('show');
}

function updateStatus(packageId) {
    currentPackageId = packageId;
    $('#statusPackageId').val(packageId);
    $('#statusModal').modal('show');
}

function submitLocationUpdate() {
    const packageId = $('#locationPackageId').val();
    const location = $('#locationInput').val();
    const latitude = parseFloat($('#latitudeInput').val());
    const longitude = parseFloat($('#longitudeInput').val());

    updatePackageLocation(packageId, latitude, longitude, location);
    $('#locationModal').modal('hide');
}

function submitStatusUpdate() {
    const packageId = $('#statusPackageId').val();
    const status = $('#statusSelect').val();

    updatePackageStatus(packageId, status);
    $('#statusModal').modal('hide');
}

window.refreshPackageList = function() {
    location.reload();
};

window.refreshPackageDetails = function(packageId) {
    location.reload();
};

window.showNewPackageAlert = function(message) {
    toastr.info('New package available for delivery!', 'New Package');
    refreshPackageList();
};

window.updatePackageLocation = function(packageId, latitude, longitude, location) {
    const locationElement = document.getElementById(`location-${packageId}`);
    if (locationElement) {
        locationElement.textContent = location;
    }
};

function getStatusBadgeClass(status) {
    switch(status) {
        case 'PENDING': return 'warning';
        case 'ASSIGNED': return 'info';
        case 'PICKED_UP': return 'primary';
        case 'IN_TRANSIT': return 'info';
        case 'DELIVERED': return 'success';
        case 'CANCELLED': return 'danger';
        default: return 'secondary';
    }
}

// Handle availability toggle
$('#availabilityToggle').change(function(event) {
    const isAvailable = $(this).is(':checked');
    const userId = '${user.id}';
    
    $.ajax({
        url: '/api/courier/' + userId + '/availability?available=' + isAvailable,
        method: 'POST',
        beforeSend: function(xhr) {
            xhr.setRequestHeader('X-CSRF-TOKEN', '${_csrf.token}');
        },
        success: function(response) {
            if (response.status === 'success') {
                toastr.success('Availability status updated successfully');
            } else {
                toastr.error(response.message || 'Failed to update availability');
                // Revert the toggle if update failed
                $('#availabilityToggle').prop('checked', !isAvailable);
            }
        },
        error: function(xhr) {
            toastr.error(xhr.responseJSON?.message || 'Failed to update availability');
            // Revert the toggle if update failed
            $('#availabilityToggle').prop('checked', !isAvailable);
        }
    });
});

// Initialize DataTables
$(document).ready(function() {
    $('#availablePackagesTable').DataTable({
        order: [[0, 'desc']]
    });
    $('#activeDeliveriesTable').DataTable({
        order: [[0, 'desc']]
    });
});
</script>

<%@ include file="common/footer.jsp" %>