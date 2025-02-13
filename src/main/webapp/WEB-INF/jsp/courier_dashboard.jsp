<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
                <input type="hidden" id="username" value="${user.username}">
                
                <div class="d-sm-flex align-items-center justify-content-between mb-4">
                    <h1 class="h3 mb-0 text-gray-800">Courier Dashboard</h1>
                </div>


                <div class="row">
                    <div class="col-xl-6 col-md-6 mb-4">
                        <div class="card border-left-primary shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                            Active Deliveries</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800" id="activeDeliveriesCount">0</div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-truck fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-6 col-md-6 mb-4">
                        <div class="card border-left-success shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                            Available Packages</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800" id="availablePackagesCount">0</div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-box fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

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
                                            <td>${pkg.id}</td>
                                            <td>${pkg.customerUsername}</td>
                                            <td>${pkg.pickupAddress}</td>
                                            <td>${pkg.deliveryAddress}</td>
                                            <td>${pkg.weight} kg</td>
                                            <td>
                                                <button class="btn btn-primary btn-sm" onclick="takeDelivery('${pkg.id}')">
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
                                            <td>${pkg.id}</td>
                                            <td>${pkg.customerUsername}</td>
                                            <td>${pkg.pickupAddress}</td>
                                            <td>${pkg.deliveryAddress}</td>
                                            <td>${getStatusBadgeHtml(pkg.status)}</td>
                                            <td>${getActionButton(pkg)}</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
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

let stompClient = null;

$(document).ready(function() {

    $('#availablePackagesTable').DataTable({
        pageLength: 10,
        order: [[0, 'desc']],
        autoWidth: false,
        columnDefs: [
            {
                targets: -1,
                orderable: false,
                searchable: false,
                width: '150px'
            }
        ],
        language: {
            emptyTable: "No packages available",
            zeroRecords: "No matching packages found",
            info: "Showing _START_ to _END_ of _TOTAL_ packages",
            infoEmpty: "Showing 0 to 0 of 0 packages",
            infoFiltered: "(filtered from _MAX_ total packages)"
        },
        responsive: true
    });
    
    $('#activeDeliveriesTable').DataTable({
        pageLength: 10,
        order: [[0, 'desc']],
        autoWidth: false,
        columnDefs: [
            {
                targets: -1,
                orderable: false,
                searchable: false,
                width: '200px'
            },
            {
                targets: 4,
                orderable: true,
                searchable: true,
                width: '100px'
            }
        ],
        language: {
            emptyTable: "No active deliveries",
            zeroRecords: "No matching deliveries found",
            info: "Showing _START_ to _END_ of _TOTAL_ deliveries",
            infoEmpty: "Showing 0 to 0 of 0 deliveries",
            infoFiltered: "(filtered from _MAX_ total deliveries)"
        },
        responsive: true
    });

    // Initialize toastr
    toastr.options = {
        closeButton: true,
        progressBar: true,
        positionClass: "toast-top-right",
        timeOut: 5000
    };

 
    let socket = new SockJS('/websocket');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected to WebSocket');
        
       
        stompClient.subscribe('/user/queue/packages/available', function(message) {
            console.log('Received available packages update:', message);
            try {
                const rawData = JSON.parse(message.body);
                updateAvailablePackagesTable(rawData);
            } catch (error) {
                console.error('Error processing available packages:', error);
            }
        });
        
      
        stompClient.subscribe('/user/queue/packages/active', function(message) {
            console.log('Received active deliveries update:', message);
            try {
                const rawData = JSON.parse(message.body);
                updateActiveDeliveriesTable(rawData);
            } catch (error) {
                console.error('Error processing active deliveries:', error);
            }
        });
        
      
        stompClient.subscribe('/user/queue/errors', function(message) {
            console.log('Received error message:', message);
            try {
                const response = JSON.parse(message.body);
                if (response.error) {
                    toastr.error(response.error);
                }
            } catch (error) {
                console.error('Error processing error message:', error);
            }
        });
        
     
        stompClient.subscribe('/user/queue/package/status', function(message) {
            console.log('Received success message:', message);
            try {
                const response = JSON.parse(message.body);
                if (response.message) {
                    toastr.success(response.message);
                }
            } catch (error) {
                console.error('Error processing success message:', error);
            }
        });
        

        stompClient.send("/ws/packages/available", {});
        stompClient.send("/ws/packages/active", {});
    }, function(error) {
        console.error('WebSocket connection error:', error);
        toastr.error('Failed to connect to server. Please refresh the page.');
    });
});

function takeDelivery(packageId) {
    if (!packageId) {
        toastr.error('Invalid package ID');
        return;
    }

    console.log('Taking delivery for package:', packageId);
    if (stompClient && stompClient.connected) {
        stompClient.send("/ws/package/take", {}, JSON.stringify({
            packageId: packageId
        }));
    } else {
        toastr.error('Not connected to server. Please refresh the page.');
    }
}

function updatePackageStatus(packageId, status) {
    if (!packageId || !status) {
        toastr.error('Invalid parameters');
        return;
    }

    console.log('Updating status for package:', packageId, 'to:', status);
    if (stompClient && stompClient.connected) {
        stompClient.send("/ws/package/status/update", {}, JSON.stringify({
            packageId: packageId,
            status: status
        }));
    } else {
        toastr.error('Not connected to server. Please refresh the page.');
    }
}

function dropDelivery(packageId) {
    if (!confirm('Are you sure you want to drop this delivery?')) {
        return;
    }

    console.log('Dropping delivery for package:', packageId);
    if (stompClient && stompClient.connected) {
        stompClient.send("/ws/package/drop", {}, JSON.stringify({
            packageId: packageId
        }));
    } else {
        toastr.error('Not connected to server. Please refresh the page.');
    }
}

function updateAvailablePackagesTable(rawData) {
    const table = $('#availablePackagesTable').DataTable();
    table.clear();
 
    const packages = Array.isArray(rawData) && rawData[1] ? rawData[1] : [];
    console.log('Processing available packages:', packages);
    
    packages.forEach(pkg => {
   
        console.log('Package data:', pkg);
        
        table.row.add([
            pkg.package_id || '',
            pkg.customer ? pkg.customer.username : pkg.customerUsername || '',
            pkg.pickupAddress || '',
            pkg.deliveryAddress || '',
            (pkg.weight ? pkg.weight + ' kg' : ''),
            `<button class="btn btn-primary btn-sm" onclick="takeDelivery('${pkg.package_id}')">
                <i class="fas fa-truck"></i> Take Delivery
            </button>`
        ]);
    });
    
    table.draw(false);
    $('#availablePackagesCount').text(packages.length);
}

function updateActiveDeliveriesTable(rawData) {
    const table = $('#activeDeliveriesTable').DataTable();
    table.clear();
  
    const deliveries = Array.isArray(rawData) && rawData[1] ? rawData[1] : [];
    console.log('Processing active deliveries:', deliveries);
    
    deliveries.forEach(pkg => {
     
        console.log('Delivery data:', pkg);
        
        table.row.add([
            pkg.package_id || '',
            pkg.customer ? pkg.customer.username : pkg.customerUsername || '',
            pkg.pickupAddress || '',
            pkg.deliveryAddress || '',
            getStatusBadgeHtml(pkg.status || ''),
            getActionButton(pkg)
        ]);
    });
    
    table.draw(false);
    $('#activeDeliveriesCount').text(deliveries.length);
}

function getStatusBadgeHtml(status) {
    const badgeClass = status === 'ASSIGNED' ? 'warning' :
                      status === 'PICKED_UP' ? 'info' :
                      status === 'IN_TRANSIT' ? 'primary' : 'success';
    return `<span class="badge badge-${badgeClass}">${status}</span>`;
}

function getActionButton(pkg) {
    if (!pkg || !pkg.status) return '';
    
    switch (pkg.status) {
        case 'ASSIGNED':
            return `
                <button class="btn btn-info btn-sm" onclick="takeDelivery('${pkg.package_id}')">
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
</script>
</body>
</html>