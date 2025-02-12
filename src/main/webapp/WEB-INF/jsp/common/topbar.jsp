
<div id="content-wrapper" class="d-flex flex-column">


    <div id="content">


        <nav class="navbar navbar-expand navbar-light bg-white modern-topbar">
            <div class="container-fluid px-4">
       
                <button id="sidebarToggleTop" class="btn btn-link d-md-none">
                    <i class="fa fa-bars"></i>
                </button>

  
                <ul class="navbar-nav ml-auto">
                    <div class="topbar-divider d-none d-sm-block"></div>

           
                    <li class="nav-item dropdown no-arrow">
                        <a class="nav-link dropdown-toggle modern-profile" href="#" id="userDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <span class="mr-2 d-none d-lg-inline text-gray-600">${user.username}</span>
                            <i class="fas fa-user-circle fa-2x text-gray-300"></i>
                        </a>
                        <!-- Dropdown - User Information -->
                        <div class="dropdown-menu dropdown-menu-right shadow animated--grow-in" aria-labelledby="userDropdown">
                            <a class="dropdown-item" href="/profile">
                                <i class="fas fa-user fa-sm fa-fw mr-2 text-gray-400"></i>
                                Profile
                            </a>
                            <div class="dropdown-divider"></div>
                            <a class="dropdown-item" href="#" data-toggle="modal" data-target="#logoutModal">
                                <i class="fas fa-sign-out-alt fa-sm fa-fw mr-2 text-gray-400"></i>
                                Logout
                            </a>
                        </div>
                    </li>
                </ul>
            </div>
        </nav>
        <div class="container-fluid">

<style>
.modern-topbar {
    box-shadow: 0 2px 10px rgba(0,0,0,0.05);
    height: 70px;
    padding: 0.5rem 1.5rem;
    position: sticky;
    top: 0;
    z-index: 1030;
    transition: all 0.3s ease;
}

.modern-profile {
    padding: 1rem;
    position: relative;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    gap: 10px;
}

.modern-profile:hover {
    background-color: #f8f9fc;
    border-radius: 8px;
}

.dropdown-menu {
    border: none;
    border-radius: 0.5rem;
    box-shadow: 0 0.5rem 1rem rgba(0,0,0,0.1);
}

.dropdown-item {
    padding: 0.75rem 1.5rem;
    transition: all 0.2s ease;
}

.dropdown-item:hover {
    background-color: #f8f9fc;
}

.topbar-divider {
    width: 0;
    border-right: 1px solid #e3e6f0;
    height: 2rem;
    margin: auto 1rem;
}

.animated--grow-in {
    animation-name: growIn;
    animation-duration: 200ms;
    animation-timing-function: transform cubic-bezier(.18,1.25,.4,1), opacity cubic-bezier(0,1,.4,1);
}

@keyframes growIn {
    0% {
        transform: scale(0.9);
        opacity: 0;
    }
    100% {
        transform: scale(1);
        opacity: 1;
    }
}
</style> 