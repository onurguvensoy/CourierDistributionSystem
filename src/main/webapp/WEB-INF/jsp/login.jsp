<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Login" />

<%@ include file="common/header.jsp" %>

<!-- Custom styles for login page -->
<style>
    .bg-login-image {
        background: url('https://source.unsplash.com/K4mSJ7kc0As/600x800');
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
</style>

<div class="container">
    <!-- Toast Container -->
    <div class="toast-container"></div>
    
    <!-- Outer Row -->
    <div class="row justify-content-center">
        <div class="col-xl-10 col-lg-12 col-md-9">
            <div class="card o-hidden border-0 shadow-lg my-5">
                <div class="card-body p-0">
                    <!-- Nested Row within Card Body -->
                    <div class="row">
                        <div class="col-lg-6 d-none d-lg-block bg-login-image"></div>
                        <div class="col-lg-6">
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
                                    <h1 class="h4 text-gray-900 mb-4">Welcome Back!</h1>
                                </div>
                                <div id="alertPlaceholder"></div>
                                <form id="loginForm" class="user">
                                    <div class="form-group">
                                        <div class="input-group">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text">
                                                    <i class="fas fa-user"></i>
                                                </span>
                                            </div>
                                            <input type="text" class="form-control form-control-user" id="username" 
                                                name="username" placeholder="Enter Username..." required>
                                        </div>
                                    </div>
                                    <div class="form-group">
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
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                    <button type="submit" class="btn btn-primary btn-user btn-block">
                                        <i class="fas fa-sign-in-alt mr-2"></i> Login
                                    </button>
                                </form>
                                <hr>
                                <div class="text-center">
                                    <a class="small" href="/auth/signup">
                                        <i class="fas fa-user-plus mr-1"></i> Create an Account!
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Initialize toastr options
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

    const loginForm = document.getElementById('loginForm');
    if (!loginForm) {
        console.error('Login form not found!');
        return;
    }

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        console.log('Login form submitted');
        
        const submitButton = this.querySelector('button[type="submit"]');
        const originalButtonText = submitButton.innerHTML;
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Loading...';
        
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        // Client-side validation
        if (!username || !password) {
            toastr.error('Please enter both username and password.');
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
            return;
        }

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]')?.value || ''
                },
                body: JSON.stringify({
                    username: username,
                    password: password
                }),
                credentials: 'include'
            });
            
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Invalid username or password');
            }

            if (data.error) {
                throw new Error(data.message || 'Login failed');
            }

            toastr.success('Login successful! Redirecting...');
            
            // Redirect based on role
            setTimeout(() => {
                const role = data.role;
                let redirectUrl;
                switch(role) {
                    case 'ADMIN':
                        redirectUrl = '/admin/dashboard';
                        break;
                    case 'COURIER':
                        redirectUrl = '/courier/dashboard';
                        break;
                    case 'CUSTOMER':
                        redirectUrl = '/customer/dashboard';
                        break;
                    default:
                        redirectUrl = '/dashboard';
                }
                window.location.href = redirectUrl;
            }, 1000);

        } catch (error) {
            console.error('Login error:', error);
            toastr.error(error.message || 'Login failed. Please try again.');
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonText;
        }
    });
});
</script>

<%@ include file="common/footer.jsp" %>
