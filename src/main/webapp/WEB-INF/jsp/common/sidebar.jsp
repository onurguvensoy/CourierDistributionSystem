<!-- Sidebar -->
<ul class="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">

    <!-- Sidebar - Brand -->
    <a class="sidebar-brand d-flex align-items-center justify-content-center" href="/">
        <div class="sidebar-brand-icon rotate-n-15">
            <i class="fas fa-truck"></i>
        </div>
        <div class="sidebar-brand-text mx-3">CDS</div>
    </a>

    <!-- Divider -->
    <hr class="sidebar-divider my-0">

    <!-- Nav Item - Dashboard -->
    <li class="nav-item">
        <a class="nav-link" href="/">
            <i class="fas fa-fw fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
    </li>

    <!-- Divider -->
    <hr class="sidebar-divider">

    <!-- Heading -->
    <div class="sidebar-heading">
        Interface
    </div>

    <c:if test="${user.role == 'ADMIN'}">
        <!-- Nav Item - Admin Pages -->
        <li class="nav-item">
            <a class="nav-link" href="/admin/dashboard">
                <i class="fas fa-fw fa-cog"></i>
                <span>Admin Dashboard</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="/admin/reports">
                <i class="fas fa-fw fa-chart-area"></i>
                <span>Reports</span>
            </a>
        </li>
    </c:if>

    <c:if test="${user.role == 'CUSTOMER'}">
        <!-- Nav Item - Customer Pages -->
        <li class="nav-item">
            <a class="nav-link" href="/customer/dashboard">
                <i class="fas fa-fw fa-box"></i>
                <span>My Packages</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="/customer/new-package">
                <i class="fas fa-fw fa-plus"></i>
                <span>Create Package</span>
            </a>
        </li>
    </c:if>

    <c:if test="${user.role == 'COURIER'}">
        <!-- Nav Item - Courier Pages -->
        <li class="nav-item">
            <a class="nav-link" href="/courier/dashboard">
                <i class="fas fa-fw fa-truck"></i>
                <span>My Deliveries</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="/courier/available-packages">
                <i class="fas fa-fw fa-box-open"></i>
                <span>Available Packages</span>
            </a>
        </li>
    </c:if>

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