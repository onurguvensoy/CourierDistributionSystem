/*!
 * Start Bootstrap - SB Admin v7.0.7 (https://startbootstrap.com/template/sb-admin)
 * Copyright 2013-2024 Start Bootstrap
 * Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-sb-admin/blob/master/LICENSE)
 */

window.addEventListener('DOMContentLoaded', event => {
    // Toggle the side navigation
    const sidebarToggle = document.body.querySelector('#sidebarToggle');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', event => {
            event.preventDefault();
            document.body.classList.toggle('sb-sidenav-toggled');
            localStorage.setItem('sb|sidebar-toggle', document.body.classList.contains('sb-sidenav-toggled'));
        });
    }

    // Add active state to sidbar nav links
    const path = window.location.href;
    const navLinks = document.querySelectorAll('.sb-sidenav-menu .nav-link');
    navLinks.forEach(link => {
        if (link.href === path) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Initialize popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    // Enable Bootstrap validation
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // Initialize DataTables
    const datatablesSimple = document.getElementById('datatablesSimple');
    if (datatablesSimple) {
        new simpleDatatables.DataTable(datatablesSimple);
    }

    // Handle CSRF token for AJAX requests
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    if (token && header) {
        // Add CSRF token to all AJAX requests
        const originalFetch = window.fetch;
        window.fetch = function() {
            let [resource, config] = arguments;
            if (config === undefined) {
                config = {};
            }
            if (config.headers === undefined) {
                config.headers = {};
            }
            config.headers[header] = token;
            return originalFetch(resource, config);
        };

        // Add CSRF token to XMLHttpRequest
        const originalXhr = window.XMLHttpRequest;
        function newXHR() {
            const xhr = new originalXhr();
            const originalOpen = xhr.open;
            xhr.open = function() {
                const result = originalOpen.apply(this, arguments);
                this.setRequestHeader(header, token);
                return result;
            };
            return xhr;
        }
        window.XMLHttpRequest = newXHR;
    }

    // Handle sidebar state on page load
    const sidebarState = localStorage.getItem('sb|sidebar-toggle');
    if (sidebarState === 'true') {
        document.body.classList.add('sb-sidenav-toggled');
    }
});

// Global AJAX error handler
window.addEventListener('error', function(e) {
    console.error('Global error handler:', e.error);
    // You can add custom error handling here
});

// Handle session timeout
let sessionTimeout;
function resetSessionTimeout() {
    clearTimeout(sessionTimeout);
    sessionTimeout = setTimeout(() => {
        // Redirect to login page or show session timeout modal
        window.location.href = '/auth/login';
    }, 30 * 60 * 1000); // 30 minutes
}

// Reset session timeout on user activity
['mousedown', 'keydown', 'scroll', 'touchstart'].forEach(event => {
    document.addEventListener(event, resetSessionTimeout);
});

// Initialize session timeout
resetSessionTimeout(); 