import { FC } from 'react';
import { Link, useLocation } from 'react-router-dom';
import classNames from 'classnames';
import { useAuthContext } from '../../contexts/auth.context';
import type { User } from '../../types/auth';

interface SidebarProps {
  toggled: boolean;
}

interface AuthContextType {
  user: User | null;
}

export const Sidebar: FC<SidebarProps> = ({ toggled }) => {
  const location = useLocation();
  const { user } = useAuthContext() as AuthContextType;

  return (
    <ul className={classNames("navbar-nav bg-gradient-primary sidebar sidebar-dark accordion", {
      'toggled': toggled
    })} id="accordionSidebar">
      {/* Sidebar - Brand */}
      <Link className="sidebar-brand d-flex align-items-center justify-content-center" to="/">
        <div className="sidebar-brand-icon rotate-n-15">
          <i className="fas fa-truck"></i>
        </div>
        <div className="sidebar-brand-text mx-3">Courier App</div>
      </Link>

      <hr className="sidebar-divider my-0" />

      {/* Dashboard */}
      <li className="nav-item">
        <Link className={classNames("nav-link", { 'active': location.pathname.includes('/dashboard') })} 
              to={`/${user?.role.toLowerCase()}/dashboard`}>
          <i className="fas fa-fw fa-tachometer-alt"></i>
          <span>Dashboard</span>
        </Link>
      </li>

      {/* Add role-specific menu items */}
      {user?.role === 'CUSTOMER' && (
        <>
          <li className="nav-item">
            <Link className={classNames("nav-link", { 'active': location.pathname.includes('/packages') })} 
                  to="/customer/packages">
              <i className="fas fa-fw fa-box"></i>
              <span>My Packages</span>
            </Link>
          </li>
          {/* Add more customer menu items */}
        </>
      )}

      {/* Add more role-specific sections */}
    </ul>
  );
}; 