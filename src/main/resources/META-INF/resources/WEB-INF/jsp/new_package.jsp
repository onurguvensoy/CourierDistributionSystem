<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Create New Package" />

<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<%@ include file="common/topbar.jsp" %>

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
                                   required minlength="5" maxlength="200"
                                   pattern="^[a-zA-Z0-9\s,.-]+$">
                            <div class="invalid-feedback">
                                Please provide a valid pickup address (5-200 characters, alphanumeric and basic punctuation only).
                            </div>
                            <small class="form-text text-muted">Enter the complete address where the package should be picked up.</small>
                        </div>

                        <div class="form-group">
                            <label for="deliveryAddress">Delivery Address <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="deliveryAddress" name="deliveryAddress" 
                                   required minlength="5" maxlength="200"
                                   pattern="^[a-zA-Z0-9\s,.-]+$">
                            <div class="invalid-feedback">
                                Please provide a valid delivery address (5-200 characters, alphanumeric and basic punctuation only).
                            </div>
                            <small class="form-text text-muted">Enter the complete address where the package should be delivered.</small>
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
        const formElement = form[0];
        
        // Clear previous validation state
        form.removeClass('was-validated');
        
        // Validate all fields
        if (!formElement.checkValidity()) {
            e.stopPropagation();
            form.addClass('was-validated');
            toastr.error('Please fill in all required fields correctly.');
            return;
        }

        // Additional validation
        const weight = parseFloat($('#weight').val());
        if (isNaN(weight) || weight <= 0 || weight > 1000) {
            toastr.error('Weight must be between 0.1 and 1000 kg');
            return;
        }

        // Show confirmation dialog
        if (!confirm('Please confirm the package details are correct. Would you like to create this package?')) {
            return;
        }

        const submitButton = $('#submitButton');
        submitButton.prop('disabled', true)
                   .html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Creating...');

        // Get form data
        const formData = new FormData(formElement);
        const requestData = {
            username: formData.get('username'),
            pickupAddress: formData.get('pickupAddress'),
            deliveryAddress: formData.get('deliveryAddress'),
            weight: parseFloat(formData.get('weight')),
            description: formData.get('description'),
            specialInstructions: formData.get('specialInstructions') || ''
        };

        console.log('Sending request with data:', requestData);

        // Send the request to create package using fetch
        fetch('/api/packages/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
        .then(async response => {
            console.log('Response status:', response.status);
            const contentType = response.headers.get('content-type');
            const responseText = await response.text();
            console.log('Response body:', responseText);
            
            if (!response.ok) {
                let errorMessage;
                try {
                    const errorData = JSON.parse(responseText);
                    errorMessage = errorData.message || `Server error: ${response.status}`;
                } catch (e) {
                    errorMessage = `Server error: ${response.status} - ${responseText}`;
                }
                throw new Error(errorMessage);
            }
            
            try {
                return JSON.parse(responseText);
            } catch (e) {
                throw new Error('Invalid JSON response from server');
            }
        })
        .then(data => {
            console.log('Success:', data);
            if (data.status === 'success') {
                toastr.success('Package created successfully! Redirecting to dashboard...');
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