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
let stompClient = null;
let deliveryTable = null;

function handlePackageUpdate(update) {
    try {
        const table = $('#deliveryHistoryTable').DataTable();
        const role = '${role}';
        
      
        if (update.type === 'STATUS_UPDATE') {
            // For delivery history page, we only care about DELIVERED status
            if (role === 'COURIER') {
                // For courier: show in history if DELIVERED, remove if status changes from DELIVERED
                if (update.newStatus === 'DELIVERED' && update.courierId === '${user.id}') {
                    refreshTableData();
                } else if (update.previousStatus === 'DELIVERED' && update.courierId === '${user.id}') {
                    const row = table.row(`[data-package-id="${update.packageId}"]`);
                    if (row.length) {
                        row.remove().draw();
                    }
                }
            } else {
                // For customer: show in history if DELIVERED, remove if status changes from DELIVERED
                if (update.newStatus === 'DELIVERED' && update.customerId === '${user.id}') {
                    refreshTableData();
                } else if (update.previousStatus === 'DELIVERED' && update.customerId === '${user.id}') {
                    const row = table.row(`[data-package-id="${update.packageId}"]`);
                    if (row.length) {
                        row.remove().draw();
                    }
                }
            }
        }
    } catch (error) {
        console.error('Error handling package update:', error);
    }
}

function connectWebSocket() {
    try {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        
        // Disable STOMP debug logging
        stompClient.debug = null;
        
        stompClient.connect({}, function(frame) {
            console.debug('Connected to WebSocket');
            
            // Subscribe to package updates
            stompClient.subscribe('/topic/package-updates', function(message) {
                try {
                    const update = JSON.parse(message.body);
                    handlePackageUpdate(update);
                } catch (error) {
                    console.error('Error processing WebSocket message:', error);
                }
            });

            // Subscribe to specific user's package updates
            const username = '${user.username}';
            const role = '${role}';
            const userSpecificTopic = role === 'COURIER' 
                ? `/topic/courier/${username}/packages`
                : `/topic/customer/${username}/packages`;
                
            stompClient.subscribe(userSpecificTopic, function(message) {
                try {
                    const update = JSON.parse(message.body);
                    handlePackageUpdate(update);
                } catch (error) {
                    console.error('Error processing user-specific WebSocket message:', error);
                }
            });
        }, function(error) {
            console.error('WebSocket connection error:', error);
            // Attempt to reconnect after 5 seconds
            setTimeout(connectWebSocket, 5000);
        });
    } catch (error) {
        console.error('Error establishing WebSocket connection:', error);
    }
}

function refreshTableData() {
    const role = '${role}';
    const endpoint = role === 'COURIER' ? '/api/courier/completed-deliveries' : '/api/customer/delivery-history';
    
    try {
        // Get CSRF token
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        
        if (!csrfHeader || !csrfToken) {
            console.error('CSRF tokens not found');
            return;
        }

        $.ajax({
            url: endpoint,
            method: 'GET',
            headers: {
                [csrfHeader]: csrfToken
            },
            success: function(data) {
                try {
                    const table = $('#deliveryHistoryTable').DataTable();
                    const currentPage = table.page();
                    const currentPageLength = table.page.len();
                    const currentSearch = table.search();
                    
                    table.clear();
                    
                    data.forEach(pkg => {
                        const row = table.row.add([
                            pkg.package_id,
                            `<span class="badge badge-secondary">${pkg.trackingNumber}</span>`,
                            pkg.pickupAddress,
                            pkg.deliveryAddress,
                            `<div class="d-flex align-items-center">
                                <i class="fas fa-user-circle fa-fw text-gray-400 mr-2"></i>
                                ${pkg.courierUsername}
                            </div>`,
                            pkg.formattedDeliveryDate || 'N/A',
                            `<button class="btn btn-info btn-sm" onclick="viewDetails('${pkg.package_id}')">
                                <i class="fas fa-info-circle"></i> Details
                            </button>`
                        ]).node();
                        
                        // Add data attributes for easier row manipulation
                        $(row).attr('data-package-id', pkg.package_id);
                        $(row).attr('data-tracking-number', pkg.trackingNumber);
                    });
                    
                    // Restore table state
                    table.page(currentPage).draw('page');
                    table.page.len(currentPageLength);
                    table.search(currentSearch).draw();
                } catch (error) {
                    console.error('Error updating table data:', error);
                }
            },
            error: function(xhr, status, error) {
                console.error('Error fetching table data:', error);
                console.error('Status:', status);
                console.error('Response:', xhr.responseText);
            }
        });
    } catch (error) {
        console.error('Error in refreshTableData:', error);
    }
}

$(document).ready(function() {
    try {
        deliveryTable = $('#deliveryHistoryTable').DataTable({
            "order": [[5, "desc"]], // Sort by delivered date by default
            "pageLength": 10,
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

        // Initialize WebSocket connection
        connectWebSocket();
    } catch (error) {
        console.error('Error initializing page:', error);
    }
});

function viewDetails(packageId) {
    try {
        const role = '${role}';
        const baseUrl = role === 'COURIER' ? '/courier' : '/customer';
        window.location.href = `${baseUrl}/packages/${packageId}`;
    } catch (error) {
        console.error('Error navigating to package details:', error);
    }
}


$(window).focus(function() {
    try {
        if (!stompClient || !stompClient.connected) {
            connectWebSocket();
        }
    } catch (error) {
        console.error('Error handling window focus:', error);
    }
});
</script>
</body>
</html> 