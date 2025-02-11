<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Create New Package" />

<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<%@ include file="common/topbar.jsp" %>

<!-- Add required libraries -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>
<link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.css" rel="stylesheet">

<script>
// Configure toastr options
toastr.options = {
    "closeButton": true,
    "debug": false,
    "newestOnTop": true,
    "progressBar": true,
    "positionClass": "toast-top-right",
    "preventDuplicates": false,
    "onclick": null,
    "showDuration": "300",
    "hideDuration": "1000",
    "timeOut": "5000",
    "extendedTimeOut": "1000",
    "showEasing": "swing",
    "hideEasing": "linear",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut",
    "containerId": "toast-container"
};
</script>

<!-- Begin Page Content -->
<div class="container-fluid">

    <!-- Page Heading -->
    <div class="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 class="h3 mb-0 text-gray-800">Create New Package</h1>
    </div>

    <!-- Create Package Form -->
    <div class="row">
        <div class="col-12">
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Package Details</h6>
                </div>
                <div class="card-body">
                    <form id="createPackageForm" class="needs-validation" novalidate>
                        <input type="hidden" name="username" value="${user.username}">
                        <div class="form-group">
                            <label for="pickupAddress">Pickup Address <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="pickupAddress" name="pickupAddress" 
                                   required minlength="5" maxlength="200">
                            <div class="invalid-feedback">
                                Please provide a pickup address (5-200 characters).
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="deliveryAddress">Delivery Address <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="deliveryAddress" name="deliveryAddress" 
                                   required minlength="5" maxlength="200">
                            <div class="invalid-feedback">
                                Please provide a delivery address (5-200 characters).
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="weight">Weight (kg) <span class="text-danger">*</span></label>
                            <input type="number" class="form-control" id="weight" name="weight" 
                                   step="0.1" min="0.1" max="1000" required>
                            <div class="invalid-feedback">
                                Please provide a valid weight between 0.1 and 1000 kg.
                            </div>
                            <small class="form-text text-muted">Enter the package weight in kilograms (0.1 - 1000 kg).</small>
                        </div>

                        <div class="form-group">
                            <label for="description">Package Description <span class="text-danger">*</span></label>
                            <textarea class="form-control" id="description" name="description" 
                                    rows="3" required minlength="10" maxlength="500"></textarea>
                            <div class="invalid-feedback">
                                Please provide a package description (10-500 characters).
                            </div>
                            <small class="form-text text-muted">
                                Describe the package contents and any relevant details (10-500 characters).
                                <span id="descriptionLength" class="float-right">0/500</span>
                            </small>
                        </div>

                        <div class="form-group">
                            <label for="specialInstructions">Special Instructions</label>
                            <textarea class="form-control" id="specialInstructions" name="specialInstructions" 
                                    rows="2" maxlength="200"></textarea>
                            <small class="form-text text-muted">
                                Optional: Add any special handling instructions or notes for the courier (max 200 characters).
                                <span id="instructionsLength" class="float-right">0/200</span>
                            </small>
                        </div>

                        <div class="alert alert-info" role="alert">
                            <i class="fas fa-info-circle"></i> Please review all details carefully before submitting. 
                            Once created, the package will be available for courier pickup.
                        </div>

                        <button type="submit" class="btn btn-primary" id="submitButton">
                            <i class="fas fa-box"></i> Create Package
                        </button>
                        <a href="/customer/dashboard" class="btn btn-secondary">
                            <i class="fas fa-times"></i> Cancel
                        </a>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
$(document).ready(function() {
    // Character count for textareas
    $('#description').on('input', function() {
        $('#descriptionLength').text($(this).val().length + '/500');
    });

    $('#specialInstructions').on('input', function() {
        $('#instructionsLength').text($(this).val().length + '/200');
    });

    // Form validation and submission
    $('#createPackageForm').on('submit', function(e) {
        e.preventDefault();
        
        const form = $(this);
        
        // Get form data
        const formData = {
            username: '${user.username}',
            pickupAddress: $('#pickupAddress').val().trim(),
            deliveryAddress: $('#deliveryAddress').val().trim(),
            weight: parseFloat($('#weight').val()),
            description: $('#description').val().trim(),
            specialInstructions: $('#specialInstructions').val().trim() || ''
        };

        // Basic validation
        if (!formData.pickupAddress || formData.pickupAddress.length < 5) {
            toastr.error('Pickup address must be at least 5 characters');
            return;
        }

        if (!formData.deliveryAddress || formData.deliveryAddress.length < 5) {
            toastr.error('Delivery address must be at least 5 characters');
            return;
        }

        if (!formData.description || formData.description.length < 10) {
            toastr.error('Description must be at least 10 characters');
            return;
        }

        if (isNaN(formData.weight) || formData.weight <= 0 || formData.weight > 1000) {
            toastr.error('Weight must be between 0.1 and 1000 kg');
            return;
        }

        const submitButton = $('#submitButton');
        submitButton.prop('disabled', true)
                   .html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Creating...');

        // Send API request
        fetch('/api/packages/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.package_id) {
                toastr.success('Package created successfully!');
                setTimeout(() => window.location.href = '/customer/dashboard', 1500);
            } else {
                throw new Error(data.message || 'Failed to create package');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            toastr.error(error.message || 'Failed to create package. Please try again.');
        })
        .finally(() => {
            submitButton.prop('disabled', false)
                       .html('<i class="fas fa-box"></i> Create Package');
        });
    });

    // Initialize tooltips
    $('[data-toggle="tooltip"]').tooltip();

    // Show warning when leaving page with unsaved changes
    let formChanged = false;
    $('#createPackageForm :input').on('change input', function() {
        formChanged = true;
    });

    $(window).on('beforeunload', function() {
        if (formChanged) {
            return 'You have unsaved changes. Are you sure you want to leave?';
        }
    });
});
</script>

<%@ include file="common/footer.jsp" %> 