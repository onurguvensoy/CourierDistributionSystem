<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" value="Customer Dashboard" />

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

const successMessage = '${successMessage}';
const errorMessage = '${errorMessage}';

if (successMessage) {
    toastr.success(successMessage);
}
if (errorMessage) {
    toastr.error(errorMessage);
}
</script>

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

    <!-- Content Row - Package Statistics -->
    <div class="row">
        <!-- Pending Packages Card -->
        <div class="col-xl-4 col-md-6 mb-4">
            <div class="card border-left-warning shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                Pending Packages</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">
                                <c:set var="pendingCount" value="0" />
                                <c:forEach items="${activePackages}" var="pkg">
                                    <c:if test="${pkg.status == 'PENDING'}">
                                        <c:set var="pendingCount" value="${pendingCount + 1}" />
                                    </c:if>
                                </c:forEach>
                                ${pendingCount}
                            </div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-clock fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- In Transit Packages Card -->
        <div class="col-xl-4 col-md-6 mb-4">
            <div class="card border-left-info shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                                In Transit</div>
                            <div class="row no-gutters align-items-center">
                                <div class="col-auto">
                                    <div class="h5 mb-0 mr-3 font-weight-bold text-gray-800">
                                        <c:set var="inTransitCount" value="0" />
                                        <c:forEach items="${activePackages}" var="pkg">
                                            <c:if test="${pkg.status == 'IN_TRANSIT' || pkg.status == 'PICKED_UP' || pkg.status == 'ASSIGNED'}">
                                                <c:set var="inTransitCount" value="${inTransitCount + 1}" />
                                            </c:if>
                                        </c:forEach>
                                        ${inTransitCount}
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="progress progress-sm mr-2">
                                        <c:choose>
                                            <c:when test="${inTransitCount > 0}">
                                                <div class="progress-bar bg-info" role="progressbar" style="width:100%" 
                                                    aria-valuenow="${inTransitCount}" aria-valuemin="0" aria-valuemax="100">
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="progress-bar bg-info" role="progressbar" style="width:0%" 
                                                    aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-truck fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Delivered Packages Card -->
        <div class="col-xl-4 col-md-6 mb-4">
            <div class="card border-left-success shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                Delivered</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">
                                <c:set var="deliveredCount" value="${stats.deliveredPackages}" />
                                ${deliveredCount}
                            </div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-check-circle fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Content Row -->
    <div class="row">
        <!-- Active Packages Table -->
        <div class="col-12">
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Active Packages</h6>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-bordered" id="activePackagesTable" width="100%" cellspacing="0">
                            <thead>
                                <tr>
                                    <th>Package ID</th>
                                    <th>Pickup Location</th>
                                    <th>Delivery Location</th>
                                    <th>Status</th>
                                    <th>Courier</th>
                                    <th>Created At</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${activePackages}" var="deliveryPackage">
                                    <tr>
                                        <td>${deliveryPackage.package_id}</td>
                                        <td>${deliveryPackage.pickupAddress}</td>
                                        <td>${deliveryPackage.deliveryAddress}</td>
                                        <td>
                                            <span class="badge badge-${deliveryPackage.status == 'PENDING' ? 'warning' : 
                                                deliveryPackage.status == 'PICKED_UP' ? 'info' : 
                                                deliveryPackage.status == 'IN_TRANSIT' ? 'primary' : 'success'}">
                                                ${deliveryPackage.status}
                                            </span>
                                        </td>
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
                                        <td>${deliveryPackage.createdAt}</td>
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
        <!-- Delivery History Table -->
        <div class="col-12">
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Delivery History</h6>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-bordered" id="deliveryHistoryTable" width="100%" cellspacing="0">
                            <thead>
                                <tr>
                                    <th>Package ID</th>
                                    <th>Pickup Location</th>
                                    <th>Delivery Location</th>
                                    <th>Status</th>
                                    <th>Courier</th>
                                    <th>Delivered At</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${completedPackages}" var="deliveryPackage">
                                    <tr>
                                        <td>${deliveryPackage.package_id}</td>
                                        <td>${deliveryPackage.pickupAddress}</td>
                                        <td>${deliveryPackage.deliveryAddress}</td>
                                        <td>
                                            <span class="badge badge-success">DELIVERED</span>
                                        </td>
                                        <td>${deliveryPackage.courier.username}</td>
                                        <td>${deliveryPackage.deliveredAt}</td>
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
        $('#activePackagesTable').DataTable({
            "dom": '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
                   '<"row"<"col-sm-12"tr>>' +
                   '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
            "language": {
                "lengthMenu": "_MENU_ records per page",
                "zeroRecords": "No packages found",
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

        $('#deliveryHistoryTable').DataTable({
            "dom": '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
                   '<"row"<"col-sm-12"tr>>' +
                   '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
            "language": {
                "lengthMenu": "_MENU_ records per page",
                "zeroRecords": "No delivery history found",
                "info": "Showing page _PAGE_ of _PAGES_",
                "infoEmpty": "No delivery history available",
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

<%@ include file="common/footer.jsp" %> 