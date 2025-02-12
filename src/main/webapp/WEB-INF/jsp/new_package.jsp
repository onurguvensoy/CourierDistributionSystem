<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Create New Package" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>${pageTitle} - Courier Distribution System</title>


    <link href="/startbootstrap-sb-admin-2-4.1.3/vendor/fontawesome-free/css/all.min.css" rel="stylesheet" type="text/css">
    <link href="https://fonts.googleapis.com/css?family=Nunito:200,200i,300,300i,400,400i,600,600i,700,700i,800,800i,900,900i" rel="stylesheet">


    <link href="/startbootstrap-sb-admin-2-4.1.3/css/sb-admin-2.min.css" rel="stylesheet">
    
   
    <link href="/startbootstrap-sb-admin-2-4.1.3/vendor/datatables/dataTables.bootstrap4.min.css" rel="stylesheet">
    

    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" rel="stylesheet">
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/jquery/jquery.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/jquery-easing/jquery.easing.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/js/sb-admin-2.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/datatables/jquery.dataTables.min.js"></script>
    <script src="/startbootstrap-sb-admin-2-4.1.3/vendor/datatables/dataTables.bootstrap4.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/js/toastr.min.js"></script>
</head>
<body id="page-top">
<div id="wrapper">
    <%@ include file="common/sidebar.jsp" %>
    <div id="content-wrapper" class="d-flex flex-column">
        <div id="content">
            <!-- Topbar -->
            <%@ include file="common/topbar.jsp" %>


<div class="container-fluid">

    <div class="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 class="h3 mb-0 text-gray-800">Create New Package</h1>
    </div>

    <div class="row">
        <div class="col-12">
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Package Details</h6>
                </div>
                <div class="card-body">
                    <form id="createPackageForm" class="needs-validation" novalidate>
                        <input type="hidden" name="username" value="${user.username}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        <input type="hidden" id="csrfHeader" value="${_csrf.headerName}" />
                                    
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

        </div>
     

 
<%@ include file="common/footer.jsp" %> 

    </div>

</div>



<a class="scroll-to-top rounded" href="#page-top">
    <i class="fas fa-angle-up"></i>
</a>

<script>

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

$(document).ready(function() {

    $('#description').on('input', function() {
        const length = $(this).val().length;
        $('#descriptionLength').text(`${length}/500`);
    });

    $('#specialInstructions').on('input', function() {
        const length = $(this).val().length;
        $('#instructionsLength').text(`${length}/200`);
    });

 
    $('#createPackageForm').on('submit', function(e) {
        e.preventDefault();
        
        const submitButton = $('#submitButton');
        submitButton.prop('disabled', true)
                   .html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Creating...');
        
    
        const formData = {
            username: '${user.username}',
            pickupAddress: $('#pickupAddress').val().trim(),
            deliveryAddress: $('#deliveryAddress').val().trim(),
            weight: parseFloat($('#weight').val()),
            description: $('#description').val().trim(),
            specialInstructions: $('#specialInstructions').val().trim() || ''
        };

      
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');


        $.ajax({
            url: '/api/packages/create',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            beforeSend: function(xhr) {
              
                if (csrfHeader && csrfToken && csrfHeader.trim() !== '') {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                }
            },
            success: function(data) {
                if (data.status === 'success' || data.package_id) {
                    toastr.success('Package created successfully!');
                    setTimeout(() => window.location.href = '/customer/dashboard', 1500);
                } else {
                    toastr.error(data.message || 'Failed to create package');
                    submitButton.prop('disabled', false)
                               .html('<i class="fas fa-box"></i> Create Package');
                }
            },
            error: function(xhr, status, error) {
                console.error('Error:', error);
                toastr.error(xhr.responseJSON?.message || 'Failed to create package. Please try again.');
                submitButton.prop('disabled', false)
                           .html('<i class="fas fa-box"></i> Create Package');
            }
        });
    });


    $('[data-toggle="tooltip"]').tooltip();

 
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
</body>
</html> 