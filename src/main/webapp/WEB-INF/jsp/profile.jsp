<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Profile" />

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
    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" rel="stylesheet">
    
    <style>
        .wrapper {
            overflow-x: hidden;
        }
        #content-wrapper {
            width: 100%;
            overflow-x: hidden;
        }
        .container-fluid {
            padding-right: 1.5rem;
            padding-left: 1.5rem;
            width: 100%;
        }
    </style>
</head>

<body id="page-top">
    <div id="wrapper" class="wrapper">
        <%@ include file="common/sidebar.jsp" %>

        <div id="content-wrapper" class="d-flex flex-column">
            <div id="content">
                <%@ include file="common/topbar.jsp" %>

                <div class="container-fluid">
                    <div class="d-sm-flex align-items-center justify-content-between mb-4">
                        <h1 class="h3 mb-0 text-gray-800">Profile</h1>
                        <a href="/" class="d-none d-sm-inline-block btn btn-secondary shadow-sm">
                            <i class="fas fa-arrow-left fa-sm text-white-50"></i> Back to Dashboard
                        </a>
                    </div>

                    <div class="row">
                        <div class="col-xl-8 col-lg-7">
                            <div class="card shadow mb-4">
                                <div class="card-header py-3">
                                    <h6 class="m-0 font-weight-bold text-primary">Profile Information</h6>
                                </div>
                                <div class="card-body">
                                    <div class="row no-gutters align-items-center">
                                        <div class="col mr-2">
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Username</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${user.username}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Email</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${user.email}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Role</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${user.role}</div>
                                            
                                            <c:if test="${user.role != 'ADMIN'}">
                                                <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Phone Number</div>
                                                <div class="h5 mb-3 font-weight-bold text-gray-800">${user.phoneNumber}</div>
                                            </c:if>
                                            
                                            <c:if test="${user.role == 'COURIER'}">
                                                <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Vehicle Type</div>
                                                <div class="h5 mb-3 font-weight-bold text-gray-800">${user.vehicleType}</div>
                                                
                                                <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Status</div>
                                                <div class="h5 mb-3 font-weight-bold text-gray-800">
                                                    <c:choose>
                                                        <c:when test="${user.available}">
                                                            <span class="badge badge-success">Available</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-warning">Busy</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-xl-4 col-lg-5">
                            <div class="card shadow mb-4">
                                <div class="card-header py-3">
                                    <h6 class="m-0 font-weight-bold text-primary">Statistics</h6>
                                </div>
                                <div class="card-body">
                                    <c:choose>
                                        <c:when test="${user.role == 'CUSTOMER'}">
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Packages</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.totalPackages}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Active Shipments</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.activeShipments}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Delivered Packages</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.deliveredPackages}</div>
                                        </c:when>
                                        
                                        <c:when test="${user.role == 'COURIER'}">
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Deliveries</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.totalDeliveries}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Completed Deliveries</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.completedDeliveries}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Active Deliveries</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.activeDeliveries}</div>
                                        </c:when>
                                        
                                        <c:when test="${user.role == 'ADMIN'}">
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Users</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.totalUsers}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Customers</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.customerCount}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Couriers</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.courierCount}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Packages</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.totalPackages}</div>
                                            
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Active Packages</div>
                                            <div class="h5 mb-3 font-weight-bold text-gray-800">${stats.activePackages}</div>
                                        </c:when>
                                    </c:choose>
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

    <!-- Core JavaScript -->
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/jquery/jquery.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/jquery-easing/jquery.easing.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/js/sb-admin-2.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/js/toastr.min.js"></script>

    <script>
    $(document).ready(function() {
        toastr.options = {
            "closeButton": true,
            "debug": false,
            "newestOnTop": true,
            "progressBar": true,
            "positionClass": "toast-top-right",
            "preventDuplicates": false,
            "showDuration": "300",
            "hideDuration": "1000",
            "timeOut": "5000",
            "extendedTimeOut": "1000",
            "showEasing": "swing",
            "hideEasing": "linear",
            "showMethod": "fadeIn",
            "hideMethod": "fadeOut"
        };
    });
    </script>
</body>
</html> 