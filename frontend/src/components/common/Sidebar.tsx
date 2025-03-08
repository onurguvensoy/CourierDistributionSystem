import { FC } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../contexts/auth.context';

const Sidebar: FC = () => {
    const { user, isAuthenticated, logout } = useAuth();
    const role = user?.role?.toLowerCase();

    return (
        <ul className="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">
            <Link className="sidebar-brand d-flex align-items-center justify-content-center" to="/">
                <div className="sidebar-brand-icon rotate-n-15">
                    <i className="fas fa-truck"></i>
                </div>
                <div className="sidebar-brand-text mx-3">Courier System</div>
            </Link>

            <hr className="sidebar-divider my-0" />

            <li className="nav-item">
                <Link className="nav-link" to={`/${role}/dashboard`}>
                    <i className="fas fa-fw fa-tachometer-alt"></i>
                    <span>Dashboard</span>
                </Link>
            </li>

            <hr className="sidebar-divider" />

            {role === 'admin' && (
                <>
                    <div className="sidebar-heading">Admin</div>
                    <li className="nav-item">
                        <Link className="nav-link" to="/admin/users">
                            <i className="fas fa-fw fa-users"></i>
                            <span>Users</span>
                        </Link>
                    </li>
                    <li className="nav-item">
                        <Link className="nav-link" to="/admin/reports">
                            <i className="fas fa-fw fa-chart-area"></i>
                            <span>Reports</span>
                        </Link>
                    </li>
                </>
            )}

            {role === 'courier' && (
                <>
                    <div className="sidebar-heading">Courier</div>
                    <li className="nav-item">
                        <Link className="nav-link" to="/courier/deliveries">
                            <i className="fas fa-fw fa-box"></i>
                            <span>Deliveries</span>
                        </Link>
                    </li>
                    <li className="nav-item">
                        <Link className="nav-link" to="/courier/history">
                            <i className="fas fa-fw fa-history"></i>
                            <span>History</span>
                        </Link>
                    </li>
                </>
            )}

            {role === 'customer' && (
                <>
                    <div className="sidebar-heading">Customer</div>
                    <li className="nav-item">
                        <Link className="nav-link" to="/customer/packages">
                            <i className="fas fa-fw fa-box"></i>
                            <span>My Packages</span>
                        </Link>
                    </li>
                    <li className="nav-item">
                        <Link className="nav-link" to="/customer/new-package">
                            <i className="fas fa-fw fa-plus"></i>
                            <span>New Package</span>
                        </Link>
                    </li>
                    <li className="nav-item">
                        <Link className="nav-link" to="/customer/tracking">
                            <i className="fas fa-fw fa-search"></i>
                            <span>Track Package</span>
                        </Link>
                    </li>
                </>
            )}

            <hr className="sidebar-divider d-none d-md-block" />

            <div className="text-center d-none d-md-inline">
                <button className="rounded-circle border-0" id="sidebarToggle">
                    <i className="fas fa-angle-left"></i>
                </button>
            </div>
        </ul>
    );
};

export default Sidebar;