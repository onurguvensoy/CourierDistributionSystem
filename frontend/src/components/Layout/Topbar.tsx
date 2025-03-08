import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Dropdown } from 'react-bootstrap';
import { useAuthContext } from '../../contexts/auth.context';

interface TopbarProps {
  onSidebarToggle: () => void;
}

export const Topbar: FC<TopbarProps> = ({ onSidebarToggle }) => {
  const { user, logout } = useAuthContext();

  return (
    <nav className="navbar navbar-expand navbar-light bg-white topbar mb-4 static-top shadow">
      {/* Sidebar Toggle (Topbar) */}
      <button 
        id="sidebarToggleTop" 
        className="btn btn-link d-md-none rounded-circle mr-3"
        onClick={onSidebarToggle}
      >
        <i className="fa fa-bars"></i>
      </button>

      {/* Topbar Search */}
      <form className="d-none d-sm-inline-block form-inline mr-auto ml-md-3 my-2 my-md-0 mw-100 navbar-search">
        <div className="input-group">
          <input 
            type="text" 
            className="form-control bg-light border-0 small" 
            placeholder="Search for..." 
          />
          <div className="input-group-append">
            <button className="btn btn-primary" type="button">
              <i className="fas fa-search fa-sm"></i>
            </button>
          </div>
        </div>
      </form>

      {/* Topbar Navbar */}
      <ul className="navbar-nav ml-auto">
        {/* Nav Item - User Information */}
        <Dropdown as="li" className="nav-item">
          <Dropdown.Toggle as="a" className="nav-link" id="userDropdown">
            <span className="mr-2 d-none d-lg-inline text-gray-600 small">
              {user?.username}
            </span>
            <img 
              className="img-profile rounded-circle" 
              src="/img/undraw_profile.svg"
            />
          </Dropdown.Toggle>

          <Dropdown.Menu className="dropdown-menu-right shadow animated--grow-in">
            <Dropdown.Item as={Link} to="/profile">
              <i className="fas fa-user fa-sm fa-fw mr-2 text-gray-400"></i>
              Profile
            </Dropdown.Item>
            <Dropdown.Item as={Link} to="/settings">
              <i className="fas fa-cogs fa-sm fa-fw mr-2 text-gray-400"></i>
              Settings
            </Dropdown.Item>
            <Dropdown.Divider />
            <Dropdown.Item onClick={logout}>
              <i className="fas fa-sign-out-alt fa-sm fa-fw mr-2 text-gray-400"></i>
              Logout
            </Dropdown.Item>
          </Dropdown.Menu>
        </Dropdown>
      </ul>
    </nav>
  );
}; 