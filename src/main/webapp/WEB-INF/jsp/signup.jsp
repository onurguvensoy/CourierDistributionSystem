<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sign Up - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <style>
        body {
            display: flex;
            align-items: center;
            min-height: 100vh;
            padding: 20px 0;
        }
        .signup-container {
            max-width: 400px;
            width: 100%;
            margin: 0 auto;
            padding: 30px;
        }
        .error-message {
            color: #e53935;
            margin-bottom: 15px;
            font-size: 0.9em;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="signup-container">
            <h2 class="text-center mb-4">Sign Up</h2>
            
            <c:if test="${not empty error}">
                <div class="error-message text-center">${error}</div>
            </c:if>
            
            <form:form action="api/auth/signup" method="POST" modelAttribute="signupForm">
                <div class="mb-3">
                    <label for="username" class="form-label">Username</label>
                    <form:input path="username" class="form-control" required="true"/>
                    <form:errors path="username" cssClass="error-message"/>
                </div>
                
                <div class="mb-3">
                    <label for="email" class="form-label">Email</label>
                    <form:input path="email" type="email" class="form-control" required="true"/>
                    <form:errors path="email" cssClass="error-message"/>
                </div>
                
                <div class="mb-3">
                    <label for="password" class="form-label">Password</label>
                    <form:password path="password" class="form-control" required="true"/>
                    <form:errors path="password" cssClass="error-message"/>
                </div>
                
                <div class="mb-3">
                    <label for="roleType" class="form-label">Role</label>
                    <form:select path="roleType" class="form-control" required="true">
                        <form:option value="CUSTOMER">Customer</form:option>
                        <form:option value="COURIER">Courier</form:option>
                    </form:select>
                </div>
                
                <!-- Customer Fields -->
                <div id="customerFields" class="role-fields">
                    <div class="mb-3">
                        <label for="deliveryAddress" class="form-label">Delivery Address</label>
                        <form:input path="deliveryAddress" class="form-control"/>
                    </div>
                    <div class="mb-3">
                        <label for="phoneNumber" class="form-label">Phone Number</label>
                        <form:input path="phoneNumber" class="form-control"/>
                    </div>
                </div>
                
                <!-- Courier Fields -->
                <div id="courierFields" class="role-fields" style="display: none;">
                    <div class="mb-3">
                        <label for="vehicleType" class="form-label">Vehicle Type</label>
                        <form:select path="vehicleType" class="form-control">
                            <form:option value="CAR">Car</form:option>
                            <form:option value="MOTORCYCLE">Motorcycle</form:option>
                            <form:option value="BICYCLE">Bicycle</form:option>
                        </form:select>
                    </div>
                    <div class="mb-3">
                        <label for="phoneNumber" class="form-label">Phone Number</label>
                        <form:input path="phoneNumber" class="form-control"/>
                    </div>
                </div>
                <script>
                    document.getElementById('roleType').addEventListener('change', function() {
                        const customerFields = document.getElementById('customerFields');
                        const courierFields = document.getElementById('courierFields');
    
                        
                        // Hide all fields first
                        customerFields.style.display = 'none';
                        courierFields.style.display = 'none';
                        
                        // Show relevant fields based on role
                        switch(this.value) {
                            case 'CUSTOMER':
                                customerFields.style.display = 'block';
                                break;
                            case 'COURIER':
                                courierFields.style.display = 'block';
                                break;
                        }
                    });
                </script>
                
                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-primary">Sign Up</button>
                </div>
                
                <div class="text-center mt-3">
                    <a href="/auth/login">Already have an account? Login</a>
                </div>
            </form:form>
        </div>
    </div>
</body>
</html> 