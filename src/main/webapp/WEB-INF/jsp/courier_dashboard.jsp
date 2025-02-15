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
                                                <button class="btn btn-primary btn-sm take-delivery-btn" onclick="takeDelivery('${pkg.id}')" data-package-id="${pkg.id}">
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
                                            <td>${getActionButton(pkg.id, pkg.status)}</td>
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

function ensureDOMLoaded(callback) {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', callback);
    } else {
        callback();
    }
}

function initializeWebSocket() {
    try {
        let socket = new SockJS('/websocket');
        stompClient = Stomp.over(socket);
        
        const username = document.getElementById('username')?.value;
        if (!username) {
            console.error('Username not found in hidden input');
            toastr.error('Authentication error: Username not found');
            return;
        }
        
        const connectHeaders = {
            username: username,
            'X-CSRF-TOKEN': document.querySelector("meta[name='_csrf']")?.content
        };
        
        stompClient.connect(connectHeaders, function(frame) {
            console.log('Connected to WebSocket with username:', username);
            
            // Subscribe to available packages updates
            stompClient.subscribe('/user/queue/packages/available', function(message) {
                console.log('Received available packages update:', message);
                try {
                    const packages = JSON.parse(message.body);
                    updateAvailablePackagesTable(packages);
                } catch (error) {
                    console.error('Error processing available packages:', error);
                    toastr.error('Error processing available packages');
                }
            });
            
            // Subscribe to active deliveries updates
            stompClient.subscribe('/user/queue/packages/active', function(message) {
                console.log('Received active deliveries update:', message);
                try {
                    const deliveries = JSON.parse(message.body);
                    updateActiveDeliveriesTable(deliveries);
                } catch (error) {
                    console.error('Error processing active deliveries:', error);
                    toastr.error('Error processing active deliveries');
                }
            });
            
            // Subscribe to error messages
            stompClient.subscribe('/user/queue/errors', function(message) {
                console.log('Received error message:', message);
                try {
                    const response = JSON.parse(message.body);
                    if (response.message) {
                        toastr.error(response.message);
                    }
                } catch (error) {
                    console.error('Error processing error message:', error);
                }
            });
            
            // Subscribe to status updates
            stompClient.subscribe('/user/queue/package/status', function(message) {
                console.log('Received status update:', message);
                try {
                    const response = JSON.parse(message.body);
                    if (response.status === 'success') {
                        toastr.success(response.message);
                    } else if (response.status === 'error') {
                        toastr.error(response.message);
                    }
                } catch (error) {
                    console.error('Error processing status update:', error);
                }
            });

            // Initial data load
            requestInitialData();
        }, function(error) {
            console.error('WebSocket connection error:', error);
            toastr.error('Failed to connect to server. Please refresh the page.');
        });
    } catch (error) {
        console.error('Error initializing WebSocket:', error);
        toastr.error('Failed to initialize WebSocket connection');
    }
}

function requestInitialData() {
    if (stompClient?.connected) {
        stompClient.send("/ws/packages/available", {});
        stompClient.send("/ws/packages/active", {});
    } else {
        console.warn('WebSocket not connected for initial data load');
    }
}

function initializeTables() {
    const availablePackagesTable = $('#availablePackagesTable');
    const activeDeliveriesTable = $('#activeDeliveriesTable');
    
    if (availablePackagesTable.length) {
        availablePackagesTable.DataTable({
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
    }
    
    if (activeDeliveriesTable.length) {
        activeDeliveriesTable.DataTable({
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
    }
}

function initializeToastr() {
    toastr.options = {
        closeButton: true,
        progressBar: true,
        positionClass: "toast-top-right",
        timeOut: 5000
    };
}

ensureDOMLoaded(function() {
    try {
        initializeToastr();
        initializeTables();
        initializeWebSocket();
    } catch (error) {
        console.error('Error during initialization:', error);
        toastr.error('Failed to initialize the dashboard');
    }
});

function updateAvailablePackagesTable(rawData) {
    const table = $('#availablePackagesTable').DataTable();
    if (!table) {
        console.error('Available packages table not initialized');
        return;
    }

    table.clear();
    
    let packages = [];
    try {
        const parsedData = typeof rawData === 'string' ? JSON.parse(rawData) : rawData;
        
        if (!parsedData) {
            console.error('No data received');
            return;
        }
        
        packages = Array.isArray(parsedData) ? parsedData : [parsedData];
        
        console.log('Processing packages:', packages);
    } catch (error) {
        console.error('Error parsing packages data:', error);
        return;
    }
    
    packages.forEach(pkg => {
        if (!pkg) return;
        
        const packageId = pkg.id || pkg.package_id;
        if (!packageId) {
            console.error('Invalid package ID:', pkg);
            return;
        }
        
        const $takeDeliveryButton = $('<button>', {
            class: 'btn btn-primary btn-sm take-delivery-btn',
            'data-package-id': packageId
        }).append(
            $('<i>', { class: 'fas fa-truck' }),
            ' Take Delivery'
        );
        
        table.row.add([
            packageId,
            pkg.customerUsername || '',
            pkg.pickupAddress || '',
            pkg.deliveryAddress || '',
            (pkg.weight ? pkg.weight + ' kg' : ''),
            $takeDeliveryButton.prop('outerHTML')
        ]);
    });
    
    table.draw(false);
    
    $('#availablePackagesCount').text(packages.length.toString());
}

function updateActiveDeliveriesTable(rawData) {
    const table = $('#activeDeliveriesTable').DataTable();
    if (!table) {
        console.error('Active deliveries table not initialized');
        return;
    }

    table.clear();
    
    let deliveries = [];
    try {
        // Parse the data if it's a string
        const parsedData = typeof rawData === 'string' ? JSON.parse(rawData) : rawData;
        
        // Ensure we have valid data
        if (!parsedData) {
            console.error('No data received');
            return;
        }
        
        // Handle both array and single object responses
        deliveries = Array.isArray(parsedData) ? parsedData : [parsedData];
        
        console.log('Processing deliveries:', deliveries);
    } catch (error) {
        console.error('Error parsing deliveries data:', error);
        return;
    }
    
    deliveries.forEach(pkg => {
        if (!pkg) return;
        
        // Extract package ID safely
        const packageId = pkg.id || pkg.package_id;
        if (!packageId) {
            console.error('Invalid package ID:', pkg);
            return;
        }
        
        table.row.add([
            packageId,
            pkg.customerUsername || '',
            pkg.pickupAddress || '',
            pkg.deliveryAddress || '',
            getStatusBadgeHtml(pkg.status || ''),
            getActionButton(packageId, pkg.status)
        ]);
    });
    
    table.draw(false);
    
    const countElement = document.getElementById('activeDeliveriesCount');
    if (countElement) {
        countElement.textContent = deliveries.length.toString();
    }
}

function getStatusBadgeHtml(status) {
    const badgeClass = status === 'PENDING' ? 'warning' :
                      status === 'IN_PROGRESS' ? 'primary' :
                      status === 'DELIVERED' ? 'success' : 'danger';
    return `<span class="badge badge-${badgeClass}">${status}</span>`;
}

function getActionButton(packageId, status) {
    if (!packageId || !status) return '';
    
    switch (status) {
        case 'PENDING': {
            const $button = $('<button>', {
                class: 'btn btn-primary btn-sm take-delivery-btn',
                'data-package-id': packageId
            }).append(
                $('<i>', { class: 'fas fa-truck' }),
                ' Take Delivery'
            );
            return $button.prop('outerHTML');
        }
        case 'IN_PROGRESS': {
            const $markDeliveredButton = $('<button>', {
                class: 'btn btn-success btn-sm update-status-btn mr-2',
                'data-package-id': packageId,
                'data-status': 'DELIVERED'
            }).append(
                $('<i>', { class: 'fas fa-check' }),
                ' Mark Delivered'
            );

            const $dropButton = $('<button>', {
                class: 'btn btn-warning btn-sm drop-delivery-btn',
                'data-package-id': packageId
            }).append(
                $('<i>', { class: 'fas fa-times' }),
                ' Drop'
            );

            const $buttonContainer = $('<div>').append(markDeliveredButton, dropButton);
            return $buttonContainer.html();
        }
        default:
            return '';
    }
}

// Single initialization point for everything
$(document).ready(function() {
    try {
        // Initialize toastr
        toastr.options = {
            closeButton: true,
            progressBar: true,
            positionClass: "toast-top-right",
            timeOut: 5000
        };

        // Initialize DataTables
        const availablePackagesTable = $('#availablePackagesTable');
        const activeDeliveriesTable = $('#activeDeliveriesTable');
        
        if (availablePackagesTable.length && !$.fn.DataTable.isDataTable('#availablePackagesTable')) {
            availablePackagesTable.DataTable({
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
        }
        
        if (activeDeliveriesTable.length && !$.fn.DataTable.isDataTable('#activeDeliveriesTable')) {
            activeDeliveriesTable.DataTable({
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
        }
        
        // Set up event handlers using jQuery delegation
        $(document).on('click', '.take-delivery-btn', function(e) {
            e.preventDefault();
            const $button = $(this);
            const packageId = $button.data('package-id');
            
            if (!packageId) {
                console.error('No package ID found on button');
                toastr.error('Invalid package ID');
                return;
            }

            // Disable the button and show loading state
            $button.prop('disabled', true)
                   .html('<i class="fas fa-spinner fa-spin"></i> Processing...');

            // Convert to number and validate
            const numericPackageId = parseInt(packageId, 10);
            if (isNaN(numericPackageId)) {
                toastr.error('Invalid package ID format');
                $button.prop('disabled', false)
                       .html('<i class="fas fa-truck"></i> Take Delivery');
                return;
            }

            if (!stompClient?.connected) {
                toastr.error('Not connected to server. Please refresh the page.');
                $button.prop('disabled', false)
                       .html('<i class="fas fa-truck"></i> Take Delivery');
                return;
            }

            console.log('Taking delivery for package:', numericPackageId);
            stompClient.send("/ws/package/take", {}, JSON.stringify({
                packageId: numericPackageId
            }));

            // Note: Button state will be reset when we receive the response and tables are updated
        });
        
        $('#activeDeliveriesTable').on('click', '.update-status-btn', function() {
            const packageId = $(this).data('package-id');
            const status = $(this).data('status');
            if (packageId && status) {
                updatePackageStatus(packageId, status);
            } else {
                console.error('Missing package ID or status');
            }
        });
        
        $('#activeDeliveriesTable').on('click', '.drop-delivery-btn', function() {
            const packageId = $(this).data('package-id');
            if (packageId) {
                dropDelivery(packageId);
            } else {
                console.error('No package ID found on button');
            }
        });
        
        // Initialize WebSocket
        initializeWebSocket();
    } catch (error) {
        console.error('Error during initialization:', error);
        toastr.error('Failed to initialize the dashboard');
    }
});
</script>
</body>
</html>