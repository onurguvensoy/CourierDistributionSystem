<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Delivery History" />

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

>
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
                    <h1 class="h3 mb-0 text-gray-800">Delivery History</h1>
                    <c:choose>
                        <c:when test="${role == 'COURIER'}">
                            <a href="/courier/dashboard" class="d-none d-sm-inline-block btn btn-secondary shadow-sm">
                                <i class="fas fa-arrow-left fa-sm text-white-50"></i> Back to Courier Dashboard
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="/customer/dashboard" class="d-none d-sm-inline-block btn btn-secondary shadow-sm">
                                <i class="fas fa-arrow-left fa-sm text-white-50"></i> Back to Customer Dashboard
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>

 
                <div class="row">
              
                    <div class="col-12">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                                <h6 class="m-0 font-weight-bold text-primary">
                                    <c:choose>
                                        <c:when test="${role == 'COURIER'}">
                                            Your Completed Deliveries
                                        </c:when>
                                        <c:otherwise>
                                            Your Package History
                                        </c:otherwise>
                                    </c:choose>
                                </h6>
                                <div class="dropdown no-arrow">
                                    <a class="dropdown-toggle" href="#" role="button" id="dropdownMenuLink"
                                        data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                        <i class="fas fa-ellipsis-v fa-sm fa-fw text-gray-400"></i>
                                    </a>
                                    <div class="dropdown-menu dropdown-menu-right shadow animated--fade-in"
                                        aria-labelledby="dropdownMenuLink">
                                        <div class="dropdown-header">Export Options:</div>
                                        <a class="dropdown-item" href="#"><i class="fas fa-file-csv fa-sm fa-fw mr-2 text-gray-400"></i>Export CSV</a>
                                        <a class="dropdown-item" href="#"><i class="fas fa-file-pdf fa-sm fa-fw mr-2 text-gray-400"></i>Export PDF</a>
                                    </div>
                                </div>
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table class="table table-bordered" id="deliveryHistoryTable" width="100%" cellspacing="0">
                                        <thead>
                                            <tr>
                                                <th>Package ID</th>
                                                <th>Tracking Number</th>
                                                <th>Pickup Location</th>
                                                <th>Delivery Location</th>
                                                <th>Courier</th>
                                                <th>Delivered At</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${completedPackages}" var="pkg">
                                                <tr>
                                                    <td>${pkg.package_id}</td>
                                                    <td><span class="badge badge-secondary">${pkg.trackingNumber}</span></td>
                                                    <td>${pkg.pickupAddress}</td>
                                                    <td>${pkg.deliveryAddress}</td>
                                                    <td>
                                                        <div class="d-flex align-items-center">
                                                            <i class="fas fa-user-circle fa-fw text-gray-400 mr-2"></i>
                                                            ${pkg.courierUsername}
                                                        </div>
                                                    </td>
                                                    <c:choose>
                                                        <c:when test="${empty pkg.formattedDeliveryDate}">
                                                            <td>N/A</td>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <td>${pkg.formattedDeliveryDate}</td>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <td>
                                                        <button class="btn btn-info btn-sm" onclick="viewDetails('${pkg.package_id}')">
                                                            <i class="fas fa-info-circle"></i> Details
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
            </div>
      
        </div>
 


        <%@ include file="common/footer.jsp" %>
   
    </div>

</div>

<a class="scroll-to-top rounded" href="#page-top">
    <i class="fas fa-angle-up"></i>
</a>

<script>
