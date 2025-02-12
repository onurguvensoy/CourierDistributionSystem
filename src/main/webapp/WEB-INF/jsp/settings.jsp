<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Settings - Courier Distribution System</title>
    
    <!-- Custom fonts -->
    <link href="/startbootstrap-sb-admin-2-4.1.3/vendor/fontawesome-free/css/all.min.css" rel="stylesheet" type="text/css">
    <link href="https://fonts.googleapis.com/css?family=Nunito:200,200i,300,300i,400,400i,600,600i,700,700i,800,800i,900,900i" rel="stylesheet">
    
    <!-- Custom styles -->
    <link href="/startbootstrap-sb-admin-2-4.1.3/css/sb-admin-2.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" rel="stylesheet">
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
                        <h1 class="h3 mb-0 text-gray-800">Settings</h1>
                    </div>

                    <!-- Content Row -->
                    <div class="row">
                        <!-- Profile Settings Card -->
                        <div class="col-xl-6 col-lg-6">
                            <div class="card shadow mb-4">
                                <div class="card-header py-3">
                                    <h6 class="m-0 font-weight-bold text-primary">Profile Settings</h6>
                                </div>
                                <div class="card-body">
                                    <form id="profileForm">
                                        <div class="form-group">
                                            <label for="username">Username</label>
                                            <input type="text" class="form-control" id="username" value="${user.username}" readonly>
                                        </div>
                                        <div class="form-group">
                                            <label for="email">Email</label>
                                            <input type="email" class="form-control" id="email" value="${user.email}">
                                        </div>
                                        <c:if test="${user.role != 'ADMIN'}">
                                            <div class="form-group">
                                                <label for="phoneNumber">Phone Number</label>
                                                <input type="tel" class="form-control" id="phoneNumber" value="${user.phoneNumber}">
                                            </div>
                                        </c:if>
                                        <c:if test="${user.role == 'COURIER'}">
                                            <div class="form-group">
                                                <label for="vehicleType">Vehicle Type</label>
                                                <select class="form-control" id="vehicleType">
                                                    <option value="BICYCLE" ${user.vehicleType == 'BICYCLE' ? 'selected' : ''}>Bicycle</option>
                                                    <option value="MOTORCYCLE" ${user.vehicleType == 'MOTORCYCLE' ? 'selected' : ''}>Motorcycle</option>
                                                    <option value="CAR" ${user.vehicleType == 'CAR' ? 'selected' : ''}>Car</option>
                                                    <option value="VAN" ${user.vehicleType == 'VAN' ? 'selected' : ''}>Van</option>
                                                </select>
                                            </div>
                                        </c:if>
                                        <button type="submit" class="btn btn-primary">
                                            <i class="fas fa-save"></i> Save Changes
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <!-- Password Settings Card -->
                        <div class="col-xl-6 col-lg-6">
                            <div class="card shadow mb-4">
                                <div class="card-header py-3">
                                    <h6 class="m-0 font-weight-bold text-primary">Change Password</h6>
                                </div>
                                <div class="card-body">
                                    <form id="passwordForm">
                                        <div class="form-group">
                                            <label for="currentPassword">Current Password</label>
                                            <input type="password" class="form-control" id="currentPassword" required>
                                        </div>
                                        <div class="form-group">
                                            <label for="newPassword">New Password</label>
                                            <input type="password" class="form-control" id="newPassword" required>
                                        </div>
                                        <div class="form-group">
                                            <label for="confirmPassword">Confirm New Password</label>
                                            <input type="password" class="form-control" id="confirmPassword" required>
                                        </div>
                                        <button type="submit" class="btn btn-primary">
                                            <i class="fas fa-key"></i> Change Password
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- End of Page Content -->
            </div>
            <!-- End of Main Content -->
            
            <!-- Footer -->
            <jsp:include page="common/footer.jsp" />
        </div>
        <!-- End of Content Wrapper -->
    </div>
    <!-- End of Page Wrapper -->
    
    <!-- Scroll to Top Button -->
    <a class="scroll-to-top rounded" href="#page-top">
        <i class="fas fa-angle-up"></i>
    </a>
    
    <!-- Bootstrap core JavaScript -->
    <script src="vendor/jquery/jquery.min.js"></script>
    <script src="vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
    
    <!-- Core plugin JavaScript -->
    <script src="vendor/jquery-easing/jquery.easing.min.js"></script>
    
    <!-- Custom scripts -->
    <script src="js/sb-admin-2.min.js"></script>
    <script>
        $(document).ready(function() {
            // Profile form submission
            $('#profileForm').on('submit', function(e) {
                e.preventDefault();
                
                const data = {
                    email: $('#email').val(),
                    phoneNumber: $('#phoneNumber').val(),
                    vehicleType: $('#vehicleType').val()
                };
                
                $.ajax({
                    url: '/api/user/profile',
                    type: 'PUT',
                    contentType: 'application/json',
                    data: JSON.stringify(data),
                    success: function(response) {
                        alert('Profile updated successfully!');
                    },
                    error: function(xhr) {
                        alert('Error updating profile: ' + xhr.responseText);
                    }
                });
            });
            
            // Password form submission
            $('#passwordForm').on('submit', function(e) {
                e.preventDefault();
                
                if ($('#newPassword').val() !== $('#confirmPassword').val()) {
                    alert('New passwords do not match!');
                    return;
                }
                
                const data = {
                    currentPassword: $('#currentPassword').val(),
                    newPassword: $('#newPassword').val()
                };
                
                $.ajax({
                    url: '/api/user/password',
                    type: 'PUT',
                    contentType: 'application/json',
                    data: JSON.stringify(data),
                    success: function(response) {
                        alert('Password changed successfully!');
                        $('#passwordForm')[0].reset();
                    },
                    error: function(xhr) {
                        alert('Error changing password: ' + xhr.responseText);
                    }
                });
            });
        });
    </script>
</body>
</html> 