<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" value="Courier Dashboard" />

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
                <!-- Page Heading -->
                <div class="d-sm-flex align-items-center justify-content-between mb-4">
                    <h1 class="h3 mb-0 text-gray-800">Courier Dashboard</h1>
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
                                        <div class="h5 mb-0 font-weight-bold text-gray-800" id="activeDeliveriesCount">
                                            <c:out value="${fn:length(activeDeliveries)}" default="0" />
                                        </div>
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
                                        <div class="h5 mb-0 font-weight-bold text-gray-800" id="availablePackagesCount">
                                            <c:out value="${fn:length(availablePackages)}" default="0" />
                                        </div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-box fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Debug Info (will be hidden in production) -->
                <div class="d-none">
                    <div>Active Deliveries Count: ${fn:length(activeDeliveries)}</div>
                    <div>Available Packages Count: ${fn:length(availablePackages)}</div>
                </div>

                <!-- Available Packages Table -->
                <div class="card shadow mb-4">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">Available Packages</h6>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-bordered" id="availablePackagesTable" width="100%" cellspacing="0">
                                <thead>
                                    <tr>
                                        <th>Package ID</th>
                                        <th>Customer</th>
                                        <th>Pickup Address</th>
                                        <th>Delivery Address</th>
                                        <th>Weight</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${availablePackages}" var="pkg">
                                        <tr>
                                            <td>${pkg.package_id}</td>
                                            <td>${pkg.customerUsername}</td>
                                            <td>${pkg.pickupAddress}</td>
                                            <td>${pkg.deliveryAddress}</td>
                                            <td>${pkg.weight} kg</td>
                                            <td>
                                                <button class="btn btn-primary btn-sm" onclick="takeDelivery('${pkg.package_id}')">
                                                    <i class="fas fa-truck"></i> Take Delivery
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Active Deliveries Table -->
                <div class="card shadow mb-4">
                    <div class="card-header py-3">
                        <h6 class="m-0 font-weight-bold text-primary">Active Deliveries</h6>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-bordered" id="activeDeliveriesTable" width="100%" cellspacing="0">
                                <thead>
                                    <tr>
                                        <th>Package ID</th>
                                        <th>Customer</th>
                                        <th>Pickup Address</th>
                                        <th>Delivery Address</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${activeDeliveries}" var="pkg">
                                        <tr>
                                            <td>${pkg.package_id}</td>
                                            <td>${pkg.customerUsername}</td>
                                            <td>${pkg.pickupAddress}</td>
                                            <td>${pkg.deliveryAddress}</td>
                                            <td>
                                                <span class="badge badge-${pkg.status == 'ASSIGNED' ? 'warning' : 
                                                    pkg.status == 'PICKED_UP' ? 'info' : 
                                                    pkg.status == 'IN_TRANSIT' ? 'primary' : 'success'}">
                                                    ${pkg.status}
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${pkg.status == 'ASSIGNED'}">
                                                        <button class="btn btn-info btn-sm" onclick="updatePackageStatus('${pkg.package_id}', 'PICKED_UP')">
                                                            <i class="fas fa-box"></i> Mark Picked Up
                                                        </button>
                                                        <button class="btn btn-warning btn-sm" onclick="dropDelivery('${pkg.package_id}')">
                                                            <i class="fas fa-times"></i> Drop
                                                        </button>
                                                    </c:when>
                                                    <c:when test="${pkg.status == 'PICKED_UP'}">
                                                        <button class="btn btn-info btn-sm" onclick="updatePackageStatus('${pkg.package_id}', 'IN_TRANSIT')">
                                                            <i class="fas fa-shipping-fast"></i> Start Transit
                                                        </button>
                                                    </c:when>
                                                    <c:when test="${pkg.status == 'IN_TRANSIT'}">
                                                        <button class="btn btn-success btn-sm" onclick="updatePackageStatus('${pkg.package_id}', 'DELIVERED')">
                                                            <i class="fas fa-check"></i> Mark Delivered
                                                        </button>
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

<!-- Page level custom scripts -->
<script>
let stompClient = null;
let isConnected = false;
let availablePackagesTable;
let activeDeliveriesTable;

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    const headers = {
        'X-CSRF-TOKEN': document.querySelector("meta[name='_csrf']").content
    };
    
    stompClient.connect(headers, function(frame) {
        console.log('Connected to WebSocket');
        isConnected = true;
        
    
        stompClient.subscribe('/user/queue/packages/available', onAvailablePackagesData);
        stompClient.subscribe('/user/queue/packages/active', onActiveDeliveriesData);
        stompClient.subscribe('/user/queue/errors', onError);
        
  
        refreshAvailablePackages();
        refreshActiveDeliveries();
        
    }, function(error) {
        console.error('WebSocket connection error:', error);
        isConnected = false;
        toastr.error('Failed to connect to real-time updates. Please refresh the page.');
        setTimeout(connectWebSocket, 5000); // Retry connection after 5 seconds
    });
}

function onAvailablePackagesData(message) {
    try {
        const packages = JSON.parse(message.body);
        console.log('Received available packages:', packages);
        availablePackagesTable.clear();
        if (Array.isArray(packages)) {
            packages.forEach(pkg => {
                availablePackagesTable.row.add([
                    pkg.package_id,
                    pkg.customerUsername,
                    pkg.pickupAddress,
                    pkg.deliveryAddress,
                    pkg.weight + ' kg',
                    `<button class="btn btn-primary btn-sm" onclick="takeDelivery('${pkg.package_id}')">
                        <i class="fas fa-truck"></i> Take Delivery
                    </button>`
                ]);
            });
        }
        availablePackagesTable.draw();
        
        // Update the count in the card
        const availableCount = packages ? packages.length : 0;
        document.querySelector('.text-success.text-uppercase.mb-1').nextElementSibling.textContent = availableCount;
    } catch (error) {
        console.error('Error processing available packages:', error);
        toastr.error('Error updating available packages');
    }
}

function onActiveDeliveriesData(message) {
    try {
        const packages = JSON.parse(message.body);
        console.log('Received active deliveries:', packages);
        activeDeliveriesTable.clear();
        if (Array.isArray(packages)) {
            packages.forEach(pkg => {
                const statusBadge = getStatusBadgeHtml(pkg.status);
                const actionButtons = getActionButtonsHtml(pkg);
                
                activeDeliveriesTable.row.add([
                    pkg.package_id,
                    pkg.customerUsername,
                    pkg.pickupAddress,
                    pkg.deliveryAddress,
                    statusBadge,
                    actionButtons
                ]);
            });
        }
        activeDeliveriesTable.draw();
        
        // Update count in the card
        const activeCount = packages ? packages.length : 0;
        document.getElementById('activeDeliveriesCount').textContent = activeCount;
    } catch (error) {
        console.error('Error processing active deliveries:', error);
        toastr.error('Error updating active deliveries');
    }
}

function getStatusBadgeHtml(status) {
    const badgeClass = status === 'ASSIGNED' ? 'warning' :
                      status === 'PICKED_UP' ? 'info' :
                      status === 'IN_TRANSIT' ? 'primary' : 'success';
    return `<span class="badge badge-${badgeClass}">${status}</span>`;
}

function getActionButtonsHtml(pkg) {
    switch (pkg.status) {
        case 'ASSIGNED':
            return `
                <button class="btn btn-info btn-sm" onclick="updatePackageStatus('${pkg.package_id}', 'PICKED_UP')">
                    <i class="fas fa-box"></i> Mark Picked Up
                </button>
                <button class="btn btn-warning btn-sm" onclick="dropDelivery('${pkg.package_id}')">
                    <i class="fas fa-times"></i> Drop
                </button>`;
        case 'PICKED_UP':
            return `
                <button class="btn btn-info btn-sm" onclick="updatePackageStatus('${pkg.package_id}', 'IN_TRANSIT')">
                    <i class="fas fa-shipping-fast"></i> Start Transit
                </button>`;
        case 'IN_TRANSIT':
            return `
                <button class="btn btn-success btn-sm" onclick="updatePackageStatus('${pkg.package_id}', 'DELIVERED')">
                    <i class="fas fa-check"></i> Mark Delivered
                </button>`;
        default:
            return '';
    }
}

function onError(message) {
    try {
        const error = JSON.parse(message.body);
        toastr.error(error.message || 'An error occurred');
    } catch (error) {
        console.error('Error processing error message:', error);
        toastr.error('An unexpected error occurred');
    }
}

function refreshAvailablePackages() {
    if (!isConnected) {
        console.warn('WebSocket not connected');
        return;
    }
    const headers = {
        'content-type': 'application/json'
    };
    stompClient.send("/app/packages/available", headers, JSON.stringify({}));
}

function refreshActiveDeliveries() {
    if (!isConnected) {
        console.warn('WebSocket not connected');
        return;
    }
    const headers = {
        'content-type': 'application/json'
    };
    stompClient.send("/app/packages/active", headers, JSON.stringify({}));
}

function takeDelivery(packageId) {
    if (!isConnected) {
        toastr.warning('Not connected to server. Please refresh the page.');
        return;
    }
    const headers = {
        'content-type': 'application/json'
    };
    const message = {
        packageId: packageId
    };
    console.log('Sending take delivery request:', message);
    stompClient.send("/app/package/take", headers, JSON.stringify(message));
}

function dropDelivery(packageId) {
    if (confirm('Are you sure you want to drop this delivery?')) {
        if (!isConnected) {
            toastr.warning('Not connected to server. Please refresh the page.');
            return;
        }
        const headers = {
            'content-type': 'application/json'
        };
        const message = {
            packageId: packageId
        };
        console.log('Sending drop delivery request:', message);
        stompClient.send("/app/package/drop", headers, JSON.stringify(message));
    }
}

function updatePackageStatus(packageId, status) {
    if (!isConnected) {
        toastr.warning('Not connected to server. Please refresh the page.');
        return;
    }
    const headers = {
        'content-type': 'application/json'
    };
    const message = {
        packageId: packageId,
        status: status
    };
    console.log('Sending status update request:', message);
    stompClient.send("/app/package/status/update", headers, JSON.stringify(message));
}

$(document).ready(function() {

    availablePackagesTable = $('#availablePackagesTable').DataTable({
        pageLength: 10,
        order: [[0, 'desc']],
        language: {
            emptyTable: "No packages available"
        },
        columnDefs: [
            {
                targets: -1,
                orderable: false
            }
        ]
    });
    
    activeDeliveriesTable = $('#activeDeliveriesTable').DataTable({
        pageLength: 10,
        order: [[0, 'desc']],
        language: {
            emptyTable: "No active deliveries"
        },
        columnDefs: [
            {
                targets: -1,
                orderable: false
            }
        ]
    });

    
    toastr.options = {
        closeButton: true,
        progressBar: true,
        positionClass: "toast-top-right",
        timeOut: 5000
    };


    connectWebSocket();
    
 
    setInterval(function() {
        if (isConnected) {
            refreshAvailablePackages();
            refreshActiveDeliveries();
        }
    }, 30000); 
});
</script>
</body>
</html>