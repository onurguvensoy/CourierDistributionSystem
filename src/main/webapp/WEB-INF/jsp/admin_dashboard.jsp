<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Admin Dashboard" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>${pageTitle} - Courier Distribution System</title>


    <link href="/startbootstrap-sb-admin-2-4.1.3/vendor/fontawesome-free/css/all.min.css" rel="stylesheet" type="text/css">
    <link href="https://fonts.googleapis.com/css?family=Nunito:200,200i,300,300i,400,400i,600,600i,700,700i,800,800i,900,900i" rel="stylesheet">


    <link href="/startbootstrap-sb-admin-2-4.1.3/css/sb-admin-2.min.css" rel="stylesheet">
    

    <link href="/startbootstrap-sb-admin-2-4.1.3/vendor/datatables/dataTables.bootstrap4.min.css" rel="stylesheet">
    

    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" rel="stylesheet">


    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/jquery/jquery.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/jquery-easing/jquery.easing.min.js"></script>


    <script src="/startbootstrap-sb-admin-2-4.1.3/js/sb-admin-2.min.js"></script>


    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/datatables/jquery.dataTables.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/datatables/dataTables.bootstrap4.min.js"></script>


    <script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/js/toastr.min.js"></script>

  
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
</head>

<body id="page-top">


<div id="wrapper">

    <%@ include file="common/sidebar.jsp" %>


    <div id="content-wrapper" class="d-flex flex-column">

        <div id="content">

            <%@ include file="common/topbar.jsp" %>


            <div class="container-fluid">
   
                <div class="d-sm-flex align-items-center justify-content-between mb-4">
                    <h1 class="h3 mb-0 text-gray-800">Admin Dashboard</h1>
                    <a href="/admin/reports" class="d-none d-sm-inline-block btn btn-primary shadow-sm">
                        <i class="fas fa-download fa-sm text-white-50"></i> View Reports
                    </a>
                </div>


                <div class="row">
    
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-primary shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                            Total Users</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800">
                                            ${totalUsers}
                                        </div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-users fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>


                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-success shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                            Active Couriers</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800">
                                            ${activeCouriers}
                                        </div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-truck fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>


                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-warning shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                            Pending Packages</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800">
                                            ${pendingPackages}
                                        </div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-clock fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Total Deliveries Card -->
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-info shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                                            Total Deliveries</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800">
                                            ${totalDeliveries}
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
                    <!-- Users Table -->
                    <div class="col-12">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3">
                                <h6 class="m-0 font-weight-bold text-primary">System Users</h6>
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table class="table table-bordered" id="usersTable" width="100%" cellspacing="0">
                                        <thead>
                                            <tr>
                                                <th>Username</th>
                                                <th>Role</th>
                                                <th>Email</th>
                                                <th>Status</th>
                                                <th>Created At</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${users}" var="systemUser">
                                                <tr>
                                                    <td>${systemUser.username}</td>
                                                    <td>
                                                        <span class="badge badge-${systemUser.role == 'ADMIN' ? 'danger' : 
                                                            systemUser.role == 'COURIER' ? 'success' : 'primary'}">
                                                            ${systemUser.role}
                                                        </span>
                                                    </td>
                                                    <td>${systemUser.email}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${systemUser.role == 'COURIER'}">
                                                                <span class="badge badge-${systemUser.available ? 'success' : 'secondary'}">
                                                                    ${systemUser.available ? 'Available' : 'Busy'}
                                                                </span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge badge-success">Active</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>${systemUser.createdAt}</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Packages Table -->
                    <div class="col-12">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3">
                                <h6 class="m-0 font-weight-bold text-primary">All Packages</h6>
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table class="table table-bordered" id="packagesTable" width="100%" cellspacing="0">
                                        <thead>
                                            <tr>
                                                <th>Package ID</th>
                                                <th>Customer</th>
                                                <th>Courier</th>
                                                <th>Status</th>
                                                <th>Pickup Location</th>
                                                <th>Delivery Location</th>
                                                <th>Created At</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${packages}" var="pkg">
                                                <tr>
                                                    <td>${pkg.package_id}</td>
                                                    <td>${pkg.customer.username}</td>
                                                    <td>${pkg.courier != null ? pkg.courier.username : 'Not Assigned'}</td>
                                                    <td>
                                                        <span class="badge badge-${pkg.status == 'PENDING' ? 'warning' : 
                                                            pkg.status == 'ASSIGNED' ? 'info' : 
                                                            pkg.status == 'PICKED_UP' ? 'primary' :
                                                            pkg.status == 'IN_TRANSIT' ? 'info' :
                                                            pkg.status == 'DELIVERED' ? 'success' : 'danger'}">
                                                            ${pkg.status}
                                                        </span>
                                                    </td>
                                                    <td>${pkg.pickupAddress}</td>
                                                    <td>${pkg.deliveryAddress}</td>
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

        </div>



        <%@ include file="common/footer.jsp" %>

    </div>

</div>

<a class="scroll-to-top rounded" href="#page-top">
    <i class="fas fa-angle-up"></i>
</a>

<script src="https://cdn.datatables.net/1.10.24/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/1.10.24/js/dataTables.bootstrap4.min.js"></script>
<link href="https://cdn.datatables.net/1.10.24/css/dataTables.bootstrap4.min.css" rel="stylesheet">

<script>
$(document).ready(function() {

    $('#usersTable, #packagesTable').DataTable({
        "dom": '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
               '<"row"<"col-sm-12"tr>>' +
               '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
        "language": {
            "lengthMenu": "_MENU_ records per page",
            "zeroRecords": "No records found",
            "info": "Showing page _PAGE_ of _PAGES_",
            "infoEmpty": "No records available",
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
