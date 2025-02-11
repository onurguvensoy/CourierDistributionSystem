<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="My Profile" />

<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<%@ include file="common/topbar.jsp" %>

<!-- Begin Page Content -->
<div class="container-fluid">

    <!-- Page Heading -->
    <div class="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 class="h3 mb-0 text-gray-800">My Profile</h1>
    </div>

    <!-- Content Row -->
    <div class="row">
        <!-- Profile Card -->
        <div class="col-xl-4 col-lg-5">
            <div class="card shadow mb-4">
                <!-- Card Header -->
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Profile Information</h6>
                </div>
                <!-- Card Body -->
                <div class="card-body">
                    <div class="text-center mb-4">
                        <img class="img-fluid px-3 px-sm-4 mt-3 mb-4" style="width: 15rem;" 
                             src="/img/user_profile.svg" alt="User Profile">
                    </div>
                    <div class="mb-3">
                        <h5 class="font-weight-bold">${user.username}</h5>
                        <p class="text-muted mb-0">
                            <i class="fas fa-user-tag mr-2"></i>${user.role}
                        </p>
                    </div>
                    <hr>
                    <div class="mb-3">
                        <p class="mb-2">
                            <i class="fas fa-envelope mr-2"></i>${user.email}
                        </p>
                        <c:if test="${user.role == 'CUSTOMER' || user.role == 'COURIER'}">
                            <p class="mb-2">
                                <i class="fas fa-phone mr-2"></i>${user.phoneNumber}
                            </p>
                        </c:if>
                        <p class="mb-0">
                            <i class="fas fa-calendar mr-2"></i>Member since: ${user.createdAt}
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Statistics Card -->
        <div class="col-xl-8 col-lg-7">
            <div class="card shadow mb-4">
                <!-- Card Header -->
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Statistics</h6>
                </div>
                <!-- Card Body -->
                <div class="card-body">
                    <c:choose>
                        <%-- Customer Statistics --%>
                        <c:when test="${user.role == 'CUSTOMER'}">
                            <div class="row">
                                <div class="col-md-6 mb-4">
                                    <div class="card border-left-primary shadow h-100 py-2">
                                        <div class="card-body">
                                            <div class="row no-gutters align-items-center">
                                                <div class="col mr-2">
                                                    <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                                        Total Packages</div>
                                                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                                                        ${totalPackages}
                                                    </div>
                                                </div>
                                                <div class="col-auto">
                                                    <i class="fas fa-box fa-2x text-gray-300"></i>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-6 mb-4">
                                    <div class="card border-left-success shadow h-100 py-2">
                                        <div class="card-body">
                                            <div class="row no-gutters align-items-center">
                                                <div class="col mr-2">
                                                    <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                                        Delivered Packages</div>
                                                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                                                        ${deliveredPackagesCount}
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
                        </c:when>

                        <%-- Courier Statistics --%>
                        <c:when test="${user.role == 'COURIER'}">
                            <div class="row">
                                <div class="col-md-4 mb-4">
                                    <div class="card border-left-primary shadow h-100 py-2">
                                        <div class="card-body">
                                            <div class="row no-gutters align-items-center">
                                                <div class="col mr-2">
                                                    <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                                        Total Deliveries</div>
                                                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                                                        ${totalDeliveries}
                                                    </div>
                                                </div>
                                                <div class="col-auto">
                                                    <i class="fas fa-truck fa-2x text-gray-300"></i>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4 mb-4">
                                    <div class="card border-left-success shadow h-100 py-2">
                                        <div class="card-body">
                                            <div class="row no-gutters align-items-center">
                                                <div class="col mr-2">
                                                    <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                                        Average Rating</div>
                                                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                                                        ${user.averageRating}/5.0
                                                    </div>
                                                </div>
                                                <div class="col-auto">
                                                    <i class="fas fa-star fa-2x text-gray-300"></i>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4 mb-4">
                                    <div class="card border-left-info shadow h-100 py-2">
                                        <div class="card-body">
                                            <div class="row no-gutters align-items-center">
                                                <div class="col mr-2">
                                                    <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                                                        Status</div>
                                                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                                                        ${user.available ? 'Available' : 'Busy'}
                                                    </div>
                                                </div>
                                                <div class="col-auto">
                                                    <i class="fas fa-toggle-on fa-2x text-gray-300"></i>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Recent Ratings -->
                            <div class="card shadow mb-4">
                                <div class="card-header py-3">
                                    <h6 class="m-0 font-weight-bold text-primary">Recent Ratings</h6>
                                </div>
                                <div class="card-body">
                                    <c:choose>
                                        <c:when test="${not empty user.ratings}">
                                            <div class="table-responsive">
                                                <table class="table table-bordered" width="100%" cellspacing="0">
                                                    <thead>
                                                        <tr>
                                                            <th>Package ID</th>
                                                            <th>Rating</th>
                                                            <th>Comment</th>
                                                            <th>Date</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach items="${user.ratings}" var="rating">
                                                            <tr>
                                                                <td>${rating.deliveryPackage.package_id}</td>
                                                                <td>
                                                                    <c:forEach begin="1" end="5" var="star">
                                                                        <i class="fas fa-star ${star <= rating.courierRating ? 'text-warning' : 'text-gray-300'}"></i>
                                                                    </c:forEach>
                                                                </td>
                                                                <td>${rating.comment}</td>
                                                                <td>${rating.createdAt}</td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="text-center text-muted">No ratings yet</p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:when>

                        <%-- Admin Statistics --%>
                        <c:when test="${user.role == 'ADMIN'}">
                            <div class="row">
                                <div class="col-md-4 mb-4">
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
                                <div class="col-md-4 mb-4">
                                    <div class="card border-left-success shadow h-100 py-2">
                                        <div class="card-body">
                                            <div class="row no-gutters align-items-center">
                                                <div class="col mr-2">
                                                    <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                                        Active Deliveries</div>
                                                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                                                        ${activeDeliveries}
                                                    </div>
                                                </div>
                                                <div class="col-auto">
                                                    <i class="fas fa-truck fa-2x text-gray-300"></i>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4 mb-4">
                                    <div class="card border-left-info shadow h-100 py-2">
                                        <div class="card-body">
                                            <div class="row no-gutters align-items-center">
                                                <div class="col mr-2">
                                                    <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                                                        System Status</div>
                                                    <div class="h5 mb-0 font-weight-bold text-gray-800">
                                                        Operational
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
                        </c:when>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- End of Main Content -->

<%@ include file="common/footer.jsp" %> 