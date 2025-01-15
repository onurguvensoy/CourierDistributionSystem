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
                    <label for="role" class="form-label">Role</label>
                    <form:select path="role" class="form-control" required="true">
                        <form:option value="CUSTOMER">Customer</form:option>
                        <form:option value="COURIER">Courier</form:option>
                    </form:select>
                    <form:errors path="role" cssClass="error-message"/>
                </div>
                
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