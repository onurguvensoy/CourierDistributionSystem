<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
    <meta name="description" content="Sign Up Page" />
    <meta name="author" content="" />
    <title>Sign Up - Courier Distribution System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="/css/sb-admin.css" rel="stylesheet" />
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
</head>
<body class="bg-primary">
    <div id="layoutAuthentication">
        <div id="layoutAuthentication_content">
            <main>
                <div class="container">
                    <div class="row justify-content-center">
                        <div class="col-lg-7">
                            <div class="card shadow-lg border-0 rounded-lg mt-5">
                                <div class="card-header">
                                    <h3 class="text-center font-weight-light my-4">Create Account</h3>
                                </div>
                                <div class="card-body">
                                    <form id="signupForm" class="needs-validation" novalidate>
                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <div class="form-floating mb-3">
                                                    <input class="form-control" id="username" name="username" type="text" placeholder="Enter your username" required pattern="^[a-zA-Z0-9_]{3,20}$" />
                                                    <label for="username">Username</label>
                                                    <div class="invalid-feedback">
                                                        Username must be between 3 and 20 characters and can only contain letters, numbers, and underscores.
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <div class="form-floating mb-3">
                                                    <input class="form-control" id="email" name="email" type="email" placeholder="name@example.com" required />
                                                    <label for="email">Email address</label>
                                                    <div class="invalid-feedback">
                                                        Please enter a valid email address.
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="form-floating mb-3">
                                            <input class="form-control" id="password" name="password" type="password" placeholder="Create a password" required minlength="6" />
                                            <label for="password">Password</label>
                                            <div class="invalid-feedback">
                                                Password must be at least 6 characters long.
                                            </div>
                                        </div>
                                        <div class="form-floating mb-3">
                                            <select class="form-select" id="roleType" name="roleType" required>
                                                <option value="">Select role</option>
                                                <option value="CUSTOMER">Customer</option>
                                                <option value="COURIER">Courier</option>
                                            </select>
                                            <label for="roleType">Role</label>
                                            <div class="invalid-feedback">
                                                Please select a role.
                                            </div>
                                        </div>
                                        <div class="form-floating mb-3">
                                            <input class="form-control" id="phoneNumber" name="phoneNumber" type="tel" placeholder="Enter your phone number" required pattern="^\+?[1-9]\d{1,14}$" />
                                            <label for="phoneNumber">Phone Number</label>
                                            <div class="invalid-feedback">
                                                Please enter a valid phone number.
                                            </div>
                                        </div>
                                        <div id="customerFields" style="display: none;">
                                            <div class="form-floating mb-3">
                                                <input class="form-control" id="deliveryAddress" name="deliveryAddress" type="text" placeholder="Enter your delivery address" />
                                                <label for="deliveryAddress">Delivery Address</label>
                                                <div class="invalid-feedback">
                                                    Please enter your delivery address.
                                                </div>
                                            </div>
                                        </div>
                                        <div id="courierFields" style="display: none;">
                                            <div class="form-floating mb-3">
                                                <select class="form-select" id="vehicleType" name="vehicleType">
                                                    <option value="">Select vehicle type</option>
                                                    <option value="CAR">Car</option>
                                                    <option value="MOTORCYCLE">Motorcycle</option>
                                                    <option value="BICYCLE">Bicycle</option>
                                                </select>
                                                <label for="vehicleType">Vehicle Type</label>
                                                <div class="invalid-feedback">
                                                    Please select a vehicle type.
                                                </div>
                                            </div>
                                        </div>
                                        <div class="mt-4 mb-0">
                                            <div class="d-grid">
                                                <button class="btn btn-primary btn-block" type="submit">Create Account</button>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                                <div class="card-footer text-center py-3">
                                    <div class="small"><a href="/auth/login">Have an account? Go to login</a></div>
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
        document.getElementById('roleType').addEventListener('change', function() {
            const customerFields = document.getElementById('customerFields');
            const courierFields = document.getElementById('courierFields');
            const deliveryAddress = document.getElementById('deliveryAddress');
            const vehicleType = document.getElementById('vehicleType');
            
            if (this.value === 'CUSTOMER') {
                customerFields.style.display = 'block';
                courierFields.style.display = 'none';
                deliveryAddress.required = true;
                vehicleType.required = false;
            } else if (this.value === 'COURIER') {
                customerFields.style.display = 'none';
                courierFields.style.display = 'block';
                deliveryAddress.required = false;
                vehicleType.required = true;
            } else {
                customerFields.style.display = 'none';
                courierFields.style.display = 'none';
                deliveryAddress.required = false;
                vehicleType.required = false;
            }
        });

        document.getElementById('signupForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            if (!this.checkValidity()) {
                e.stopPropagation();
                this.classList.add('was-validated');
                return;
            }

            const formData = {
                username: document.getElementById('username').value,
                email: document.getElementById('email').value,
                password: document.getElementById('password').value,
                roleType: document.getElementById('roleType').value,
                phoneNumber: document.getElementById('phoneNumber').value
            };

            if (formData.roleType === 'CUSTOMER') {
                formData.deliveryAddress = document.getElementById('deliveryAddress').value;
            } else if (formData.roleType === 'COURIER') {
                formData.vehicleType = document.getElementById('vehicleType').value;
            }

            fetch('/api/auth/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    throw new Error(data.message || 'Registration failed');
                }
                // Show success message
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-success';
                alertDiv.role = 'alert';
                alertDiv.textContent = 'Registration successful! Redirecting to login page...';
                
                const existingAlert = document.querySelector('.alert');
                if (existingAlert) {
                    existingAlert.remove();
                }
                
                document.querySelector('.card-body').insertBefore(alertDiv, document.getElementById('signupForm'));
                
                // Redirect after a short delay
                setTimeout(() => {
                    window.location.href = '/auth/login';
                }, 2000);
            })
            .catch(error => {
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-danger';
                alertDiv.role = 'alert';
                alertDiv.textContent = error.message || 'Registration failed. Please try again.';
                
                const existingAlert = document.querySelector('.alert');
                if (existingAlert) {
                    existingAlert.remove();
                }
                
                document.querySelector('.card-body').insertBefore(alertDiv, document.getElementById('signupForm'));
            });
        });
    </script>
</body>
</html> 