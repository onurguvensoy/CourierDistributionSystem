<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Track Package - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-4">
        <h2>Package Tracking</h2>
        
        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <div class="card mb-4">
            <div class="card-body">
                <h5 class="card-title">Package Details</h5>
                <div class="row">
                    <div class="col-md-6">
                        <p><strong>Package ID:</strong> ${trackingInfo.id}</p>
                        <p><strong>Status:</strong> <span class="badge bg-primary">${trackingInfo.status}</span></p>
                        <p><strong>Created:</strong> ${trackingInfo.createdAt}</p>
                        <p><strong>Description:</strong> ${trackingInfo.description}</p>
                    </div>
                    <div class="col-md-6">
                        <p><strong>Pickup Address:</strong> ${trackingInfo.pickupAddress}</p>
                        <p><strong>Delivery Address:</strong> ${trackingInfo.deliveryAddress}</p>
                        <c:if test="${not empty trackingInfo.courierName}">
                            <p><strong>Courier:</strong> ${trackingInfo.courierName}</p>
                            <p><strong>Courier Phone:</strong> ${trackingInfo.courierPhone}</p>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-body">
                <h5 class="card-title">Live Tracking</h5>
                <jsp:include page="fragments/map.jsp" />
            </div>
        </div>

        <div class="mt-4">
            <a href="/customer/packages" class="btn btn-secondary">Back to Packages</a>
        </div>
    </div>
</body>
</html> 