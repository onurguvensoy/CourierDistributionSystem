<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Register" />

<%@ include file="common/auth_header.jsp" %>

<style>
    html, body {
        height: 100%;
    }
    .container {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .card {
        width: 100%;
        max-width: 900px;
    }
    .bg-register-image {
        background: url('https://source.unsplash.com/Mv9hjnEUHR4/600x800');
        background-position: center;
        background-size: cover;
    }
    .form-control-user {
        color: #6e707e !important;
        background-color: #fff !important;
        border: 1px solid #d1d3e2;
    }
    .form-control-user::placeholder {
        color: #858796;
        opacity: 1;
    }
    .form-control-user:focus {
        border-color: #4e73df;
        box-shadow: 0 0 0 0.2rem rgba(78, 115, 223, 0.25);
    }
    .alert {
        margin-bottom: 1rem;
    }
    .toast-container {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 1050;
    }
    select.form-control-user {
        height: calc(1.5em + 0.75rem + 2px);
        padding: 0.375rem 0.75rem;
    }
    .brand-wrapper {
        text-align: center;
        margin-bottom: 2rem;
    }
    .brand-icon {
        font-size: 3.5rem;
        color: #4e73df;
        margin-bottom: 1rem;
    }
    .brand-text {
        font-size: 1.5rem;
        color: #5a5c69;
        font-weight: 700;
    }
    .input-group-text {
        border-top-left-radius: 2rem;
        border-bottom-left-radius: 2rem;
    }
    .input-group .form-control-user {
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
    }
    .my-5 {
        margin: 0 !important;
    }
</style>

<div class="container">

    <div class="toast-container"></div>

    <div class="card o-hidden border-0 shadow-lg">
        <div class="card-body p-0">
        
            <div class="row">
                <div class="col-lg-5 d-none d-lg-block bg-register-image"></div>
                <div class="col-lg-7">
                    <div class="p-5">
                        <div class="brand-wrapper">
                            <div class="brand-icon">
                                <i class="fas fa-truck fa-flip-horizontal"></i>
                            </div>
                            <div class="brand-text">
                                Courier Distribution System
                            </div>
                        </div>
                        <div class="text-center">
                            <h1 class="h4 text-gray-900 mb-4">Create an Account!</h1>
                        </div>
                        <div id="alertPlaceholder"></div>
                        <form id="signupForm" class="user">
                            <div class="form-group">
                                <div class="input-group">
                                    <div class="input-group-prepend">
                                        <span class="input-group-text">
                                            <i class="fas fa-user"></i>
                                        </span>
                                    </div>
                                    <input type="text" class="form-control form-control-user" id="username" 
                                        name="username" placeholder="Username" required>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="input-group">
                                    <div class="input-group-prepend">
                                        <span class="input-group-text">
                                            <i class="fas fa-envelope"></i>
                                        </span>
                                    </div>
                                    <input type="email" class="form-control form-control-user" id="email" 
                                        name="email" placeholder="Email Address" required>
                                </div>
                            </div>
                            <div class="form-group row">
                                <div class="col-sm-6 mb-3 mb-sm-0">
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <span class="input-group-text">
                                                <i class="fas fa-lock"></i>
                                            </span>
                                        </div>
                                        <input type="password" class="form-control form-control-user" id="password" 
                                            name="password" placeholder="Password" required>
                                    </div>
                                </div>
                                <div class="col-sm-6">
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <span class="input-group-text">
                                                <i class="fas fa-lock"></i>
                                            </span>
                                        </div>
                                        <input type="password" class="form-control form-control-user" id="confirmPassword" 
                                            name="confirmPassword" placeholder="Confirm Password" required>
                                    </div>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="input-group">
                                    <div class="input-group-prepend">
                                        <span class="input-group-text">
                                            <i class="fas fa-phone"></i>
                                        </span>
                                    </div>
                                    <input type="tel" class="form-control form-control-user" id="phoneNumber" 
                                        name="phoneNumber" placeholder="Phone Number" required>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="input-group">
                                    <div class="input-group-prepend">
                                        <span class="input-group-text">
                                            <i class="fas fa-user-tag"></i>
                                        </span>
                                    </div>
                                    <select class="form-control form-control-user" id="role" name="role" required>
                                        <option value="">Select Role</option>
                                        <option value="CUSTOMER">Customer</option>
                                        <option value="COURIER">Courier</option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group" id="vehicleTypeGroup" style="display: none;">
                                <div class="input-group">
                                    <div class="input-group-prepend">
                                        <span class="input-group-text">
                                            <i class="fas fa-truck"></i>
                                        </span>
                                    </div>
                                    <select class="form-control form-control-user" id="vehicleType" name="vehicleType">
                                        <option value="">Select Vehicle Type</option>
                                        <option value="MOTORCYCLE">Motorcycle</option>
                                        <option value="CAR">Car</option>
                                        <option value="VAN">Van</option>
                                    </select>
                                </div>
                            </div>
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                            <button type="submit" class="btn btn-primary btn-user btn-block">
                                <i class="fas fa-user-plus mr-2"></i> Register Account
                            </button>
                        </form>
                        <hr>
                        <div class="text-center">
                            <a class="small" href="/auth/login">
                                <i class="fas fa-sign-in-alt mr-1"></i> Already have an account? Login!
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {

    toastr.options = {
        "closeButton": true,
        "debug": false,
        "newestOnTop": true,
        "progressBar": true,
        "positionClass": "toast-top-right",
        "preventDuplicates": false,
        "showDuration": "300",
        "hideDuration": "1000",
        "timeOut": "5000",
        "extendedTimeOut": "1000",
        "showEasing": "swing",
        "hideEasing": "linear",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut"
    };

    const signupForm = document.getElementById('signupForm');
    const roleSelect = document.getElementById('role');
    const vehicleTypeGroup = document.getElementById('vehicleTypeGroup');
    const phoneNumberInput = document.getElementById('phoneNumber');

    if (!signupForm || !roleSelect || !vehicleTypeGroup || !phoneNumberInput) {
        console.error('Required elements not found!');
        return;
    }

    roleSelect.addEventListener('change', function() {
        if (this.value === 'COURIER') {
            vehicleTypeGroup.style.display = 'block';
            document.getElementById('vehicleType').required = true;
        } else {
            vehicleTypeGroup.style.display = 'none';
            document.getElementById('vehicleType').required = false;
        }
        
    
        if (this.value === 'CUSTOMER' || this.value === 'COURIER') {
            phoneNumberInput.required = true;
        } else {
            phoneNumberInput.required = false;
        }
    });

    signupForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const submitButton = this.querySelector('button[type="submit"]');
        const originalButtonText = submitButton.innerHTML;
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Processing...';

        // Get form values
        const username = document.getElementById('username').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const phoneNumber = phoneNumberInput.value.trim();
        const role = roleSelect.value;

        // Validation
        if (!username || !email || !password || !confirmPassword || !role) {
            toastr.error('Please fill in all required fields.');
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
            return;
        }

        if (password !== confirmPassword) {
            toastr.error('Passwords do not match.');
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
            return;
        }

  
        if ((role === 'CUSTOMER' || role === 'COURIER') && !phoneNumber) {
            toastr.error('Phone number is required for customers and couriers.');
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
            return;
        }

  
        const formData = {
            username: username,
            email: email,
            password: password,
            role: role,
            phoneNumber: phoneNumber
        };

    
        if (role === 'COURIER') {
            const vehicleType = document.getElementById('vehicleType').value;
            if (!vehicleType) {
                toastr.error('Please select a vehicle type.');
                submitButton.disabled = false;
                submitButton.innerHTML = originalButtonText;
                return;
            }
            formData.vehicleType = vehicleType;
        }

        try {
            const response = await fetch('/api/auth/signup', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]')?.value || ''
                },
                body: JSON.stringify(formData),
                credentials: 'include'
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Registration failed');
            }

            if (data.error) {
                throw new Error(data.message || 'Registration failed');
            }
            
            toastr.success('Registration successful! Redirecting to login page...');
            
            setTimeout(() => {
                window.location.href = '/auth/login';
            }, 2000);
        } catch (error) {
            console.error('Registration error:', error);
            toastr.error(error.message || 'Registration failed. Please try again.');
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
        }
    });
});
</script>

<%@ include file="common/auth_footer.jsp" %> 