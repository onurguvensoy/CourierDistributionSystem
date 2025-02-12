<!-- Sidebar -->
<ul class="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">

    <!-- Sidebar - Brand -->
    <a class="sidebar-brand d-flex align-items-center justify-content-center" href="/">
        <div class="sidebar-brand-icon">
            <i class="fas fa-truck"></i>
        </div>
        <div class="sidebar-brand-text mx-3">CDS</div>
    </a>

    <!-- Divider -->
    <hr class="sidebar-divider my-0">

    <!-- Nav Item - Dashboard -->
    <li class="nav-item">
        <a class="nav-link" href="/">
            <i class="fas fa-fw fa-home"></i>
            <span>Dashboard</span>
        </a>
    </li>

    <!-- Divider -->
    <hr class="sidebar-divider">

    <c:choose>
        <%-- Admin Navigation Items --%>
        <c:when test="${user.role eq 'ADMIN'}">
            <!-- Heading -->
            <div class="sidebar-heading">
                Administration
            </div>

            <!-- Nav Item - User Management -->
            <li class="nav-item">
                <a class="nav-link" href="/admin/users">
                    <i class="fas fa-fw fa-users"></i>
                    <span>User Management</span>
                </a>
            </li>

            <!-- Nav Item - Package Management -->
            <li class="nav-item">
                <a class="nav-link" href="/admin/packages">
                    <i class="fas fa-fw fa-boxes"></i>
                    <span>Package Management</span>
                </a>
            </li>

            <!-- Nav Item - Reports -->
            <li class="nav-item">
                <a class="nav-link" href="/admin/reports">
                    <i class="fas fa-fw fa-chart-bar"></i>
                    <span>Reports</span>
                </a>
            </li>
        </c:when>

        <%-- Customer Navigation Items --%>
        <c:when test="${user.role eq 'CUSTOMER'}">
            <!-- Heading -->
            <div class="sidebar-heading">
                Package Management
            </div>

        

            <!-- Nav Item - Create Package -->
            <li class="nav-item">
                <a class="nav-link" href="/customer/new-package">
                    <i class="fas fa-fw fa-plus"></i>
                    <span>Create Package</span>
                </a>
            </li>

            <!-- Nav Item - Delivery History -->
            <li class="nav-item">
                <a class="nav-link" href="/customer/delivery-history">
                    <i class="fas fa-fw fa-history"></i>
                    <span>Delivery History</span>
                </a>
            </li>
        </c:when>

        <%-- Courier Navigation Items --%>
        <c:when test="${user.role eq 'COURIER'}">
            <!-- Heading -->
            <div class="sidebar-heading">
                Deliveries
            </div>


            <!-- Nav Item - Delivery History -->
            <li class="nav-item">
                <a class="nav-link" href="/courier/delivery-history">
                    <i class="fas fa-fw fa-history"></i>
                    <span>Delivery History</span>
                </a>
            </li>
        </c:when>
    </c:choose>

    <!-- Divider -->
    <hr class="sidebar-divider">

    <!-- Heading -->
    <div class="sidebar-heading">
        Account
    </div>

    <!-- Nav Item - Profile -->
    <li class="nav-item">
        <a class="nav-link" href="/profile">
            <i class="fas fa-fw fa-user"></i>
            <span>Profile</span>
        </a>
    </li>

    <!-- Nav Item - Logout -->
    <li class="nav-item">
        <a class="nav-link" href="#" data-toggle="modal" data-target="#logoutModal">
            <i class="fas fa-fw fa-sign-out-alt"></i>
            <span>Logout</span>
        </a>
    </li>

    <!-- Divider -->
    <hr class="sidebar-divider d-none d-md-block">

    <!-- Sidebar Toggler (Sidebar) -->
    <div class="text-center d-none d-md-inline">
        <button class="rounded-circle border-0" id="sidebarToggle"></button>
    </div>

</ul>
<!-- End of Sidebar -->

<!-- Logout Modal-->
<div class="modal fade" id="logoutModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="exampleModalLabel">Ready to Leave?</h5>
                <button class="close" type="button" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">×</span>
                </button>
            </div>
            <div class="modal-body">Select "Logout" below if you are ready to end your current session.</div>
            <div class="modal-footer">
                <button class="btn btn-secondary" type="button" data-dismiss="modal">Cancel</button>
                <form action="/api/auth/logout" method="post" style="display: inline;">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <button type="submit" class="btn btn-primary">Logout</button>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
document.querySelector('form[action="/api/auth/logout"]').addEventListener('submit', function(e) {
    e.preventDefault();
    
    fetch('/api/auth/logout', {
        method: 'POST',
        headers: {
            [document.querySelector("meta[name='_csrf_header']").content]: document.querySelector("meta[name='_csrf']").content
        },
        credentials: 'same-origin'
    })
    .then(() => {
        window.location.href = '/auth/login';
    })
    .catch(error => {
        console.error('Logout failed:', error);
        window.location.href = '/auth/login';
    });
});
</script> 