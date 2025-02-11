<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" value="Customer Dashboard" />

<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<%@ include file="common/topbar.jsp" %>

<!-- Begin Page Content -->
<div class="container-fluid">
    <input type="hidden" id="username" value="${user.username}">
    
    <!-- Page Heading -->
    <div class="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 class="h3 mb-0 text-gray-800">My Packages</h1>
        <a href="/customer/new-package" class="d-none d-sm-inline-block btn btn-primary shadow-sm">
            <i class="fas fa-plus fa-sm text-white-50"></i> Create New Package
        </a>
    </div>

    <!-- Active Packages -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Active Packages</h6>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-bordered" id="activePackagesTable" width="100%" cellspacing="0">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tracking #</th>
                            <th>Pickup</th>
                            <th>Delivery</th>
                            <th>Status</th>
                            <th>Courier</th>
                            <th>Current Location</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${activePackages}" var="package">
                            <tr id="package-row-${package.package_id}">
                                <td>${package.package_id}</td>
                                <td>
                                    <span class="badge badge-secondary">${package.trackingNumber}</span>
                                </td>
                                <td>${package.pickupAddress}</td>
                                <td>${package.deliveryAddress}</td>
                                <td>
                                    <span id="package-status-${package.package_id}" 
                                          class="badge badge-${getStatusBadgeClass(package.status)}">
                                        ${package.status}
                                    </span>
                                </td>
                                <td id="package-courier-${package.package_id}">
                                    ${package.courier != null ? package.courier.username : 'Not assigned'}
                                </td>
                                <td id="package-location-${package.package_id}">
                                    ${package.currentLocation != null ? package.currentLocation : 'Not available'}
                                </td>
                                <td>
                                    <button class="btn btn-info btn-sm" 
                                            onclick="showTrackingModal('${package.package_id}')">
                                        <i class="fas fa-map-marker-alt"></i> Track
                                    </button>
                                    <c:if test="${package.status == 'PENDING'}">
                                        <button class="btn btn-danger btn-sm" 
                                                onclick="cancelPackage('${package.package_id}')">
                                            <i class="fas fa-times"></i> Cancel
                                        </button>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Delivery History -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">Delivery History</h6>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-bordered" id="deliveryHistoryTable" width="100%" cellspacing="0">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tracking #</th>
                            <th>Delivery Address</th>
                            <th>Delivered At</th>
                            <th>Courier</th>
                            <th>Rating</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${deliveryHistory}" var="package">
                            <tr>
                                <td>${package.package_id}</td>
                                <td>
                                    <span class="badge badge-secondary">${package.trackingNumber}</span>
                                </td>
                                <td>${package.deliveryAddress}</td>
                                <td>${package.deliveredAt}</td>
                                <td>${package.courier.username}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${package.rating != null}">
                                            <div class="rating-stars">
                                                <c:forEach begin="1" end="5" var="i">
                                                    <i class="fas fa-star ${i <= package.rating.courierRating ? 'text-warning' : 'text-gray-300'}"></i>
                                                </c:forEach>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <button class="btn btn-primary btn-sm" 
                                                    onclick="showRatingModal('${package.package_id}')">
                                                <i class="fas fa-star"></i> Rate Delivery
                                            </button>
                                        </c:otherwise>
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

<!-- Tracking Modal -->
<div class="modal fade" id="trackingModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Package Tracking</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-md-6">
                        <div id="packageDetails"></div>
                    </div>
                    <div class="col-md-6">
                        <div id="packageMap" style="height: 400px;"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Rating Modal -->
<div class="modal fade" id="ratingModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Rate Delivery</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="ratingForm">
                    <input type="hidden" id="ratingPackageId">
                    <div class="form-group">
                        <label>Rate the Delivery Service (1-5 stars)</label>
                        <div class="rating-input">
                            <i class="fas fa-star" data-rating="1"></i>
                            <i class="fas fa-star" data-rating="2"></i>
                            <i class="fas fa-star" data-rating="3"></i>
                            <i class="fas fa-star" data-rating="4"></i>
                            <i class="fas fa-star" data-rating="5"></i>
                        </div>
                        <input type="hidden" id="ratingValue" required>
                    </div>
                    <div class="form-group">
                        <label>Comments</label>
                        <textarea class="form-control" id="ratingComment" rows="3"></textarea>
                    </div>
                    <div class="form-group">
                        <div class="custom-control custom-checkbox">
                            <input type="checkbox" class="custom-control-input" id="anonymousRating">
                            <label class="custom-control-label" for="anonymousRating">Submit Anonymously</label>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" onclick="submitRating()">Submit Rating</button>
            </div>
        </div>
    </div>
</div>

<!-- Include WebSocket JavaScript -->
<script src="/webjars/sockjs-client/sockjs.min.js"></script>
<script src="/webjars/stomp-websocket/stomp.min.js"></script>
<script src="/js/customer-websocket.js"></script>

<script>
let map = null;
let currentMarker = null;

function showTrackingModal(packageId) {
    $('#trackingModal').modal('show');
    initializeMap();
    loadPackageDetails(packageId);
}

function showRatingModal(packageId) {
    $('#ratingPackageId').val(packageId);
    $('#ratingModal').modal('show');
}

function initializeMap() {
    if (!map) {
        map = L.map('packageMap').setView([0, 0], 2);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);
    }
}

function loadPackageDetails(packageId) {
    $.get(`/api/packages/${packageId}/track?username=${$('#username').val()}`, function(response) {
        if (response.status === 'success') {
            const data = response.data;
            updatePackageDetailsUI(data);
            updateMapLocation(data.currentLocation?.latitude, data.currentLocation?.longitude);
        } else {
            toastr.error('Failed to load package details');
        }
    });
}

function updatePackageDetailsUI(data) {
    let html = `
        <h6>Package Status</h6>
        <p><strong>Status:</strong> ${data.status}</p>
        <p><strong>Current Location:</strong> ${data.currentLocation || 'Not available'}</p>
    `;
    if (data.courierName) {
        html += `
            <h6>Courier Details</h6>
            <p><strong>Name:</strong> ${data.courierName}</p>
            <p><strong>Phone:</strong> ${data.courierPhone}</p>
        `;
    }
    $('#packageDetails').html(html);
}

function updateMapLocation(lat, lng) {
    if (lat && lng) {
        if (currentMarker) {
            map.removeLayer(currentMarker);
        }
        currentMarker = L.marker([lat, lng]).addTo(map);
        map.setView([lat, lng], 13);
    }
}

// Rating functionality
$('.rating-input .fa-star').hover(
    function() {
        const rating = $(this).data('rating');
        updateStars(rating);
    },
    function() {
        const rating = $('#ratingValue').val() || 0;
        updateStars(rating);
    }
).click(function() {
    const rating = $(this).data('rating');
    $('#ratingValue').val(rating);
    updateStars(rating);
});

function updateStars(rating) {
    $('.rating-input .fa-star').each(function() {
        const starRating = $(this).data('rating');
        $(this).toggleClass('text-warning', starRating <= rating);
    });
}

function submitRating() {
    const packageId = $('#ratingPackageId').val();
    const rating = $('#ratingValue').val();
    const comment = $('#ratingComment').val();
    const anonymous = $('#anonymousRating').is(':checked');

    if (!rating) {
        toastr.error('Please select a rating');
        return;
    }

    $.ajax({
        url: `/api/packages/${packageId}/rate`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            rating: rating,
            comment: comment,
            anonymous: anonymous
        }),
        success: function(response) {
            if (response.status === 'success') {
                toastr.success('Rating submitted successfully');
                $('#ratingModal').modal('hide');
                location.reload();
            } else {
                toastr.error(response.message || 'Failed to submit rating');
            }
        },
        error: function(xhr) {
            toastr.error(xhr.responseJSON?.message || 'Failed to submit rating');
        }
    });
}

function cancelPackage(packageId) {
    if (confirm('Are you sure you want to cancel this package?')) {
        $.ajax({
            url: `/api/packages/${packageId}/cancel`,
            method: 'POST',
            data: { username: $('#username').val() },
            success: function(response) {
                if (response.status === 'success') {
                    toastr.success('Package cancelled successfully');
                    location.reload();
                } else {
                    toastr.error(response.message || 'Failed to cancel package');
                }
            },
            error: function(xhr) {
                toastr.error(xhr.responseJSON?.message || 'Failed to cancel package');
            }
        });
    }
}

// WebSocket UI update functions
window.updatePackageLocationOnMap = function(packageId, latitude, longitude, location) {
    if (map && currentMarker && $('#trackingModal').is(':visible')) {
        updateMapLocation(latitude, longitude);
        updatePackageDetailsUI({
            currentLocation: location,
            status: $(`#package-status-${packageId}`).text()
        });
    }
};

window.refreshPackageDetails = function(packageId) {
    location.reload();
};

// Initialize DataTables
$(document).ready(function() {
    $('#activePackagesTable').DataTable({
        order: [[0, 'desc']]
    });
    $('#deliveryHistoryTable').DataTable({
        order: [[0, 'desc']]
    });
});
</script>

<%@ include file="common/footer.jsp" %> 