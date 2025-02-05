<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Courier Dashboard" />
    <meta name="author" content="" />
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>Courier Dashboard - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="https://cdn.jsdelivr.net/npm/simple-datatables@7.1.2/dist/style.min.css" rel="stylesheet" />
    <link href="/css/sb-admin.css" rel="stylesheet" />
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
    <script src="https://maps.googleapis.com/maps/api/js?key=${googleMapsApiKey}&libraries=places"></script>
</head>
<body class="sb-nav-fixed">
    <nav class="sb-topnav navbar navbar-expand navbar-dark bg-dark">
        <!-- Navbar Brand-->
        <a class="navbar-brand ps-3" href="#">Courier System</a>
        <!-- Sidebar Toggle-->
        <button class="btn btn-link btn-sm order-1 order-lg-0 me-4 me-lg-0" id="sidebarToggle" href="#!"><i class="fas fa-bars"></i></button>
        <!-- Navbar-->
        <ul class="navbar-nav ms-auto me-3">
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" id="navbarDropdown" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="fas fa-user fa-fw"></i> ${user.username}
                </a>
                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
                    <li>
                        <form id="logoutForm" action="/api/auth/logout" method="POST" style="margin: 0;">
                            <button type="submit" class="dropdown-item">Logout</button>
                        </form>
                    </li>
                </ul>
            </li>
        </ul>
    </nav>
    <div id="layoutSidenav">
        <div id="layoutSidenav_nav">
            <nav class="sb-sidenav accordion sb-sidenav-dark" id="sidenavAccordion">
                <div class="sb-sidenav-menu">
                    <div class="nav">
                        <div class="sb-sidenav-menu-heading">Core</div>
                        <a class="nav-link active" href="#overview" data-bs-toggle="pill">
                            <div class="sb-nav-link-icon"><i class="fas fa-tachometer-alt"></i></div>
                            Overview
                        </a>
                        <a class="nav-link" href="#available-packages" data-bs-toggle="pill">
                            <div class="sb-nav-link-icon"><i class="fas fa-box"></i></div>
                            Available Packages
                        </a>
                        <a class="nav-link" href="#active-deliveries" data-bs-toggle="pill">
                            <div class="sb-nav-link-icon"><i class="fas fa-truck"></i></div>
                            Active Deliveries
                        </a>
                    </div>
                </div>
                <div class="sb-sidenav-footer">
                    <div class="small">Logged in as:</div>
                    ${user.username}
                </div>
            </nav>
        </div>
        <div id="layoutSidenav_content">
            <main>
                <div class="container-fluid px-4">
                    <h1 class="mt-4">Courier Dashboard</h1>
                    <ol class="breadcrumb mb-4">
                        <li class="breadcrumb-item active">Dashboard</li>
                    </ol>
                    <div class="tab-content">
                        <div class="tab-pane fade show active" id="overview">
                            <div class="row">
                                <div class="col-xl-6">
                                    <div class="card mb-4">
                                        <div class="card-header">
                                            <i class="fas fa-chart-bar me-1"></i>
                                            Active Deliveries
                                        </div>
                                        <div class="card-body">
                                            <h1 class="display-4 text-center">${activeDeliveries.size()}</h1>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-xl-6">
                                    <div class="card mb-4">
                                        <div class="card-header">
                                            <i class="fas fa-chart-pie me-1"></i>
                                            Available Packages
                                        </div>
                                        <div class="card-body">
                                            <h1 class="display-4 text-center">${availablePackages.size()}</h1>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="tab-pane fade" id="available-packages">
                            <div class="card mb-4">
                                <div class="card-header">
                                    <i class="fas fa-table me-1"></i>
                                    Available Packages
                                </div>
                                <div class="card-body">
                                    <div id="map" class="mb-4" style="height: 400px;"></div>
                                    <table id="availablePackagesTable" class="table table-striped">
                                        <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>Tracking #</th>
                                                <th>Customer</th>
                                                <th>Pickup</th>
                                                <th>Delivery</th>
                                                <th>Weight</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${availablePackages}" var="packageData">
                                                <tr>
                                                    <td>${packageData.id}</td>
                                                    <td><span class="badge bg-secondary">${packageData.trackingNumber}</span></td>
                                                    <td>${packageData.customer.username}</td>
                                                    <td>${packageData.pickupAddress}</td>
                                                    <td>${packageData.deliveryAddress}</td>
                                                    <td>${packageData.weight} kg</td>
                                                    <td>
                                                        <form action="/courier/delivery/take" method="POST" class="delivery-form">
                                                            <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                            <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                            <button type="submit" class="btn btn-primary btn-sm">Take Delivery</button>
                                                        </form>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <div class="tab-pane fade" id="active-deliveries">
                            <div class="card mb-4">
                                <div class="card-header">
                                    <i class="fas fa-table me-1"></i>
                                    Active Deliveries
                                </div>
                                <div class="card-body">
                                    <table id="activeDeliveriesTable" class="table table-striped">
                                        <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>Tracking #</th>
                                                <th>Customer</th>
                                                <th>Pickup</th>
                                                <th>Delivery</th>
                                                <th>Status</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${activeDeliveries}" var="packageData">
                                                <tr>
                                                    <td>${packageData.id}</td>
                                                    <td><span class="badge bg-secondary">${packageData.trackingNumber}</span></td>
                                                    <td>${packageData.customer.username}</td>
                                                    <td>${packageData.pickupAddress}</td>
                                                    <td>${packageData.deliveryAddress}</td>
                                                    <td><span class="badge bg-${packageData.status eq 'DELIVERED' ? 'success' : packageData.status eq 'IN_TRANSIT' ? 'warning' : 'info'}">${packageData.status}</span></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${packageData.status eq 'ASSIGNED'}">
                                                                <form action="/courier/delivery/update-status" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                    <input type="hidden" name="status" value="PICKED_UP"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                                    <button type="submit" class="btn btn-info btn-sm">Mark as Picked Up</button>
                                                                </form>
                                                                <form action="/courier/delivery/drop" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                                    <button type="submit" class="btn btn-danger btn-sm">Drop Delivery</button>
                                                                </form>
                                                            </c:when>
                                                            <c:when test="${packageData.status eq 'PICKED_UP'}">
                                                                <form action="/courier/delivery/update-status" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                    <input type="hidden" name="status" value="IN_TRANSIT"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                                    <button type="submit" class="btn btn-warning btn-sm">Start Delivery</button>
                                                                </form>
                                                                <form action="/courier/delivery/drop" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                                    <button type="submit" class="btn btn-danger btn-sm">Drop Delivery</button>
                                                                </form>
                                                            </c:when>
                                                            <c:when test="${packageData.status eq 'IN_TRANSIT'}">
                                                                <form action="/courier/delivery/update-status" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                    <input type="hidden" name="status" value="DELIVERED"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                                    <button type="submit" class="btn btn-success btn-sm">Mark as Delivered</button>
                                                                </form>
                                                                <form action="/courier/delivery/drop" method="POST" class="delivery-form d-inline">
                                                                    <input type="hidden" name="packageId" value="${packageData.id}"/>
                                                                    <input type="hidden" name="_csrf" value="${_csrf.token}" />
                                                                    <button type="submit" class="btn btn-danger btn-sm">Drop Delivery</button>
                                                                </form>
                                                            </c:when>
                                                            <c:when test="${packageData.status eq 'DELIVERED'}">
                                                                <span class="text-success">Delivered</span>
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
                </div>
            </main>
            <footer class="py-4 bg-light mt-auto">
                <div class="container-fluid px-4">
                    <div class="d-flex align-items-center justify-content-between small">
                        <div class="text-muted">Copyright &copy; Courier Distribution System 2024</div>
                    </div>
                </div>
            </footer>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/simple-datatables@7.1.2/dist/umd/simple-datatables.min.js"></script>
    <script src="/js/sb-admin.js"></script>
    <script>
        let map;
        let markers = new Map();
        let geocoder;
        let directionsService;
        let directionsRenderer;

        function initMap() {
            const defaultLocation = { lat: 41.0082, lng: 28.9784 }; // Istanbul coordinates
            
            map = new google.maps.Map(document.getElementById('map'), {
                center: defaultLocation,
                zoom: 12
            });

            geocoder = new google.maps.Geocoder();
            directionsService = new google.maps.DirectionsService();
            directionsRenderer = new google.maps.DirectionsRenderer();
            directionsRenderer.setMap(map);

            // Get user's location
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    (position) => {
                        const pos = {
                            lat: position.coords.latitude,
                            lng: position.coords.longitude
                        };
                        map.setCenter(pos);
                        // Add user marker
                        new google.maps.Marker({
                            position: pos,
                            map: map,
                            title: 'Your Location',
                            icon: {
                                url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'
                            }
                        });
                    },
                    () => {
                        console.log('Error: The Geolocation service failed.');
                        map.setCenter(defaultLocation);
                    }
                );
            }

            // Add markers for available packages
            const availablePackages = Array.from(document.querySelectorAll('#availablePackagesTable tr'));
            availablePackages.forEach(packageRow => {
                const pickupAddress = packageRow.querySelector('td:nth-child(3)').textContent;
                const deliveryAddress = packageRow.querySelector('td:nth-child(4)').textContent;
                
                // Add pickup location marker
                geocodeAndAddMarker(pickupAddress, 'pickup');
                // Add delivery location marker
                geocodeAndAddMarker(deliveryAddress, 'delivery');
            });
        }

        function geocodeAndAddMarker(address, type) {
            geocoder.geocode({ address: address }, (results, status) => {
                if (status === 'OK' && results[0]) {
                    const position = results[0].geometry.location;
                    new google.maps.Marker({
                        map: map,
                        position: position,
                        title: type === 'pickup' ? 'Pickup Location' : 'Delivery Location',
                        icon: {
                            url: type === 'pickup' ? 
                                'http://maps.google.com/mapfiles/ms/icons/green-dot.png' : 
                                'http://maps.google.com/mapfiles/ms/icons/red-dot.png'
                        }
                    });
                }
            });
        }

        // Add CSRF token to AJAX requests
        const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
        const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

        // Add event listeners to forms
        document.addEventListener('DOMContentLoaded', function() {
            const forms = document.querySelectorAll('.delivery-form');
            forms.forEach(form => {
                form.addEventListener('submit', function(e) {
                    e.preventDefault();
                    const formData = new FormData(form);
                    fetch(form.action, {
                        method: 'POST',
                        headers: {
                            [header]: token
                        },
                        body: new URLSearchParams(formData)
                    })
                    .then(response => {
                        if (response.ok) {
                            window.location.reload();
                        } else {
                            throw new Error('Network response was not ok');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        alert('An error occurred. Please try again.');
                    });
                });
            });

            initMap();
        });

        document.getElementById('logoutForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => {
                window.location.href = '/auth/login';
            })
            .catch(error => {
                console.error('Logout failed:', error);
                window.location.href = '/auth/login';
            });
        });
    </script>
</body>
</html>