<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Login Page" />
    <meta name="author" content="" />
    <title>Login - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="/css/sb-admin.css" rel="stylesheet" />
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js"></script>
</head>
<body class="bg-primary">
    <div id="layoutAuthentication">
        <div id="layoutAuthentication_content">
            <main>
                <div class="container">
                    <div class="row justify-content-center">
                        <div class="col-lg-5">
                            <div class="card shadow-lg border-0 rounded-lg mt-5">
                                <div class="card-header">
                                    <h3 class="text-center font-weight-light my-4">Login</h3>
                                </div>
                                <div class="card-body">
                                    <c:if test="${not empty error}">
                                        <div class="alert alert-danger" role="alert">
                                            ${error}
                                        </div>
                                    </c:if>

                                    <c:if test="${not empty message}">
                                        <div class="alert alert-success" role="alert">
                                            ${message}
                                        </div>
                                    </c:if>

                                    <form id="loginForm" class="needs-validation" novalidate>
                                        <div class="form-floating mb-3">
                                            <input type="text" class="form-control" id="username" name="username" placeholder="Username" required>
                                            <label for="username">Username</label>
                                            <div class="invalid-feedback">
                                                Please enter your username.
                                            </div>
                                        </div>

                                        <div class="form-floating mb-3">
                                            <input type="password" class="form-control" id="password" name="password" placeholder="Password" required>
                                            <label for="password">Password</label>
                                            <div class="invalid-feedback">
                                                Please enter your password.
                                            </div>
                                        </div>

                                        <div class="d-flex align-items-center justify-content-between mt-4 mb-0">
                                            <a class="small" href="/auth/forgot-password">Forgot Password?</a>
                                            <button type="submit" class="btn btn-primary">Login</button>
                                        </div>
                                    </form>
                                </div>
                                <div class="card-footer text-center py-3">
                                    <div class="small"><a href="/auth/signup">Need an account? Sign up!</a></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
        <div id="layoutAuthentication_footer">
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
    <script src="/js/sb-admin.js"></script>
    <script>
        document.getElementById('loginForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            const submitButton = this.querySelector('button[type="submit"]');
            const originalButtonText = submitButton.innerHTML;
            submitButton.disabled = true;
            submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Loading...';
            
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            // Client-side validation
            if (!username || !password) {
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-danger';
                alertDiv.role = 'alert';
                alertDiv.textContent = 'Please enter both username and password.';
                
                const existingAlert = document.querySelector('.alert');
                if (existingAlert) {
                    existingAlert.remove();
                }
                
                document.querySelector('.card-body').insertBefore(alertDiv, document.getElementById('loginForm'));
                submitButton.disabled = false;
                submitButton.innerHTML = originalButtonText;
                return;
            }

            fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest',
                    'Accept': 'application/json',
                    'Origin': window.location.origin
                },
                body: JSON.stringify({
                    username: username.trim(),
                    password: password
                }),
                credentials: 'include',
                mode: 'cors'
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        console.error('Login failed:', data);
                        throw new Error(data.message || 'Authentication failed');
                    });
                }
                return response.json();
            })
            .then(data => {
                // Clear any existing alerts
                const existingAlert = document.querySelector('.alert');
                if (existingAlert) {
                    existingAlert.remove();
                }

                // Show success message
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-success';
                alertDiv.role = 'alert';
                alertDiv.textContent = 'Login successful! Redirecting...';
                document.querySelector('.card-body').insertBefore(alertDiv, document.getElementById('loginForm'));

                // Redirect to dashboard based on role
                setTimeout(() => {
                    if (data.role === 'ADMIN') {
                        window.location.href = '/admin/dashboard';
                    } else if (data.role === 'COURIER') {
                        window.location.href = '/courier/dashboard';
                    } else {
                        window.location.href = '/customer/dashboard';
                    }
                }, 1000);
            })
            .catch(error => {
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-danger';
                alertDiv.role = 'alert';
                alertDiv.textContent = error.message || 'Login failed. Please try again.';
                
                const existingAlert = document.querySelector('.alert');
                if (existingAlert) {
                    existingAlert.remove();
                }
                
                document.querySelector('.card-body').insertBefore(alertDiv, document.getElementById('loginForm'));
            })
            .finally(() => {
                submitButton.disabled = false;
                submitButton.innerHTML = originalButtonText;
            });
        });
    </script>
</body>
</html>
