<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="My Packages" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>${pageTitle} - Courier Distribution System</title>

    <!-- Custom fonts for this template-->
    <link href="/startbootstrap-sb-admin-2-4.1.3/vendor/fontawesome-free/css/all.min.css" rel="stylesheet" type="text/css">
    <link href="https://fonts.googleapis.com/css?family=Nunito:200,200i,300,300i,400,400i,600,600i,700,700i,800,800i,900,900i" rel="stylesheet">

    <!-- Custom styles for this template-->
    <link href="/startbootstrap-sb-admin-2-4.1.3/css/sb-admin-2.min.css" rel="stylesheet">
    
    <!-- Custom styles for datatables -->
    <link href="/startbootstrap-sb-admin-2-4.1.3/vendor/datatables/dataTables.bootstrap4.min.css" rel="stylesheet">
    
    <!-- Toastr CSS -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" rel="stylesheet">

    <!-- Core plugin JavaScript-->
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/jquery/jquery.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/jquery-easing/jquery.easing.min.js"></script>

    <!-- Custom scripts for all pages-->
    <script src="/startbootstrap-sb-admin-2-4.1.3/js/sb-admin-2.min.js"></script>

    <!-- Page level plugins -->
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/datatables/jquery.dataTables.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/datatables/dataTables.bootstrap4.min.js"></script>

    <!-- Toastr JS -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/js/toastr.min.js"></script>

    <!-- WebSocket -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
</head>

<body id="page-top">

<!-- Page Wrapper -->
<div id="wrapper">
    <!-- Sidebar -->
    <%@ include file="common/sidebar.jsp" %>

    <!-- Content Wrapper -->
    <div id="content-wrapper" class="d-flex flex-column">
        <!-- Main Content -->
        <div id="content">
            <!-- Topbar -->
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

                <!-- Content Row - Package Statistics -->
                <div class="row">
                    <!-- Pending Packages Card -->
                    <div class="col-xl-3 col-md-6 mb-4">
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
                    <div class="col-xl-3 col-md-6 mb-4">
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
                    <div class="col-xl-3 col-md-6 mb-4">
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
                <!-- Content Row -->
                <div class="row">
                    <!-- Active Packages Table -->
                    <div class="col-12">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                                <h6 class="m-0 font-weight-bold text-primary">Active Packages</h6>
                                <a href="/customer/delivery-history" class="btn btn-info btn-sm">
                                    <i class="fas fa-history fa-sm"></i> View Delivery History
                                </a>
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
                                            <c:forEach items="${activePackages}" var="pkg">
                                                <tr>
                                                    <td>${pkg.package_id}</td>
                                                    <td>${pkg.pickupAddress}</td>
                                                    <td>${pkg.deliveryAddress}</td>
                                                    <td>
                                                        <span class="badge badge-${pkg.status == 'PENDING' ? 'warning' : 
                                                            pkg.status == 'IN_TRANSIT' ? 'info' : 'success'}">
                                                            ${pkg.status}
                                                        </span>
                                                    </td>
                                                    <td>${pkg.courier != null ? pkg.courier.username : 'Not Assigned'}</td>
                                                    <td>${pkg.createdAt}</td>
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
            <!-- End of Page Content -->
        </div>
        <!-- End of Main Content -->

        <!-- Footer -->
        <%@ include file="common/footer.jsp" %>
        <!-- End of Footer -->
    </div>
    <!-- End of Content Wrapper -->
</div>
<!-- End of Page Wrapper -->

<!-- Scroll to Top Button-->
<a class="scroll-to-top rounded" href="#page-top">
    <i class="fas fa-angle-up"></i>
</a>

<!-- DataTables JavaScript -->
<script src="https://cdn.datatables.net/1.10.24/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/1.10.24/js/dataTables.bootstrap4.min.js"></script>
<link href="https://cdn.datatables.net/1.10.24/css/dataTables.bootstrap4.min.css" rel="stylesheet">

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
});
</script>
</body>
</html> 