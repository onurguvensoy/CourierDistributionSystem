import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import authService from '../../services/authService';

const Sidebar = () => {
    const location = useLocation();
    const userRole = authService.getRole()?.toLowerCase();

    const isActive = (path) => {
        return location.pathname.startsWith(path) ? 'active' : '';
    };

    const renderAdminLinks = () => (
        <>
            <li className={`nav-item ${isActive('/admin/dashboard')}`}>
                <Link className="nav-link" to="/admin/dashboard">
                    <i className="fas fa-fw fa-tachometer-alt"></i>
                    <span>Dashboard</span>
                </Link>
            </li>
            <li className={`nav-item ${isActive('/admin/reports')}`}>
                <Link className="nav-link" to="/admin/reports">
                    <i className="fas fa-fw fa-chart-area"></i>
                    <span>Reports</span>
                </Link>
            </li>
        </>
    );

    const renderCourierLinks = () => (
        <>
            <li className={`nav-item ${isActive('/courier/dashboard')}`}>
                <Link className="nav-link" to="/courier/dashboard">
                    <i className="fas fa-fw fa-tachometer-alt"></i>
                    <span>Dashboard</span>
                </Link>
            </li>
            <li className={`nav-item ${isActive('/courier/deliveries')}`}>
                <Link className="nav-link" to="/courier/deliveries">
                    <i className="fas fa-fw fa-truck"></i>
                    <span>Deliveries</span>
                </Link>
            </li>
            <li className={`nav-item ${isActive('/courier/history')}`}>
                <Link className="nav-link" to="/courier/history">
                    <i className="fas fa-fw fa-history"></i>
                    <span>History</span>
                </Link>
            </li>
        </>
    );

    const renderCustomerLinks = () => (
        <>
            <li className={`nav-item ${isActive('/customer/dashboard')}`}>
                <Link className="nav-link" to="/customer/dashboard">
                    <i className="fas fa-fw fa-tachometer-alt"></i>
                    <span>Dashboard</span>
                </Link>
            </li>
            <li className={`nav-item ${isActive('/customer/packages')}`}>
                <Link className="nav-link" to="/customer/packages">
                    <i className="fas fa-fw fa-box"></i>
                    <span>My Packages</span>
                </Link>
            </li>
            <li className={`nav-item ${isActive('/customer/new-package')}`}>
                <Link className="nav-link" to="/customer/new-package">
                    <i className="fas fa-fw fa-plus"></i>
                    <span>New Package</span>
                </Link>
            </li>
        </>
    );

    return (
        <ul className="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">
            {/* Sidebar - Brand */}
            <Link className="sidebar-brand d-flex align-items-center justify-content-center" to="/">
                <div className="sidebar-brand-icon rotate-n-15">
                    <i className="fas fa-truck"></i>
                </div>
                <div className="sidebar-brand-text mx-3">Courier System</div>
            </Link>

            {/* Divider */}
            <hr className="sidebar-divider my-0" />

            {/* Nav Items */}
            {userRole === 'admin' && renderAdminLinks()}
            {userRole === 'courier' && renderCourierLinks()}
            {userRole === 'customer' && renderCustomerLinks()}

            {/* Divider */}
            <hr className="sidebar-divider" />

            {/* Common Links */}
            <li className={`nav-item ${isActive('/profile')}`}>
                <Link className="nav-link" to="/profile">
                    <i className="fas fa-fw fa-user"></i>
                    <span>Profile</span>
                </Link>
            </li>
            <li className={`nav-item ${isActive('/settings')}`}>
                <Link className="nav-link" to="/settings">
                    <i className="fas fa-fw fa-cog"></i>
                    <span>Settings</span>
                </Link>
            </li>
        </ul>
    );
};

export default Sidebar; 