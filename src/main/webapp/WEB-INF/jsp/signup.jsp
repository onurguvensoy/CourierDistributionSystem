<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sign Up - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .signup-container {
            max-width: 400px;
            margin: 100px auto;
            padding: 20px;
            background-color: white;
            border-radius: 5px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .error-message {
            color: red;
            margin-bottom: 15px;
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
            
            <form:form action="/auth/signup" method="post" modelAttribute="user">
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
                    <select name="roleType" class="form-control" id="roleType" required>
                        <option value="CUSTOMER">Customer</option>
                        <option value="COURIER">Courier</option>
                    </select>
                </div>
                
                <!-- Customer Fields -->
                <div id="customerFields" class="role-fields">
                    <div class="mb-3">
                        <label for="deliveryAddress" class="form-label">Delivery Address</label>
                        <input type="text" name="deliveryAddress" class="form-control"/>
                    </div>
                    <div class="mb-3">
                        <label for="billingAddress" class="form-label">Billing Address</label>
                        <input type="text" name="billingAddress" class="form-control"/>
                    </div>
                    <div class="mb-3">
                        <label for="phoneNumber" class="form-label">Phone Number</label>
                        <input type="text" name="phoneNumber" class="form-control"/>
                    </div>
                </div>
                
                <!-- Courier Fields -->
                <div id="courierFields" class="role-fields" style="display: none;">
                    <div class="mb-3">
                        <label for="vehicleType" class="form-label">Vehicle Type</label>
                        <select name="vehicleType" class="form-control">
                            <option value="CAR">Car</option>
                            <option value="MOTORCYCLE">Motorcycle</option>
                            <option value="BICYCLE">Bicycle</option>
                        </select>
                    </div>
                </div>

                <script>
                    document.getElementById('roleType').addEventListener('change', function() {
                        const customerFields = document.getElementById('customerFields');
                        const courierFields = document.getElementById('courierFields');
                        
                        if (this.value === 'CUSTOMER') {
                            customerFields.style.display = 'block';
                            courierFields.style.display = 'none';
                        } else {
                            customerFields.style.display = 'none';
                            courierFields.style.display = 'block';
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