import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Navbar, Nav, Container, Button, Dropdown } from 'react-bootstrap';
import axios from 'axios';

const MainLayout = ({ children }) => {
    const [sidebarToggled, setSidebarToggled] = useState(false);
    const navigate = useNavigate();
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    const handleLogout = async () => {
        try {
            const token = localStorage.getItem('token');
            await axios.post(`${process.env.REACT_APP_API_URL}/auth/logout`, {}, {
                headers: { Authorization: `Bearer ${token}` }
            });
            localStorage.clear();
            navigate('/login');
            toast.success('Logged out successfully');
        } catch (error) {
            console.error('Logout error:', error);
            localStorage.clear();
            navigate('/login');
        }
    };

    return (
        <div id="wrapper">
            {/* Sidebar */}
            <Nav className={`navbar-nav bg-gradient-primary sidebar sidebar-dark accordion ${sidebarToggled ? 'toggled' : ''}`} 
                id="accordionSidebar">
                
                {/* Sidebar - Brand */}
                <Link className="sidebar-brand d-flex align-items-center justify-content-center" to="/">
                    <div className="sidebar-brand-icon rotate-n-15">
                        <i className="fas fa-truck"></i>
                    </div>
                    <div className="sidebar-brand-text mx-3">Courier System</div>
                </Link>

                {/* Divider */}
                <hr className="sidebar-divider my-0" />

                {/* Nav Item - Dashboard */}
                <Nav.Item>
                    <Link className="nav-link" to={`/${user.role?.toLowerCase()}/dashboard`}>
                        <i className="fas fa-fw fa-tachometer-alt"></i>
                        <span>Dashboard</span>
                    </Link>
                </Nav.Item>

                {/* Divider */}
                <hr className="sidebar-divider" />

                {/* Nav Items based on role */}
                {user.role === 'CUSTOMER' && (
                    <>
                        <Nav.Item>
                            <Link className="nav-link" to="/customer/new-package">
                                <i className="fas fa-fw fa-box"></i>
                                <span>New Package</span>
                            </Link>
                        </Nav.Item>
                        <Nav.Item>
                            <Link className="nav-link" to="/customer/packages">
                                <i className="fas fa-fw fa-boxes"></i>
                                <span>My Packages</span>
                            </Link>
                        </Nav.Item>
                    </>
                )}

                {user.role === 'COURIER' && (
                    <>
                        <Nav.Item>
                            <Link className="nav-link" to="/courier/deliveries">
                                <i className="fas fa-fw fa-truck-loading"></i>
                                <span>Deliveries</span>
                            </Link>
                        </Nav.Item>
                        <Nav.Item>
                            <Link className="nav-link" to="/courier/history">
                                <i className="fas fa-fw fa-history"></i>
                                <span>History</span>
                            </Link>
                        </Nav.Item>
                    </>
                )}

                {/* Sidebar Toggler (Sidebar) */}
                <div className="text-center d-none d-md-inline">
                    <Button 
                        variant="link"
                        className="rounded-circle border-0" 
                        id="sidebarToggle"
                        onClick={() => setSidebarToggled(!sidebarToggled)}
                    >
                        <i className="fas fa-angle-left"></i>
                    </Button>
                </div>
            </Nav>

            {/* Content Wrapper */}
            <div id="content-wrapper" className="d-flex flex-column">
                {/* Main Content */}
                <div id="content">
                    {/* Topbar */}
                    <Navbar bg="white" expand="lg" className="mb-4 static-top shadow">
                        <Container fluid>
                            {/* Sidebar Toggle (Topbar) */}
                            <Button 
                                variant="link"
                                className="d-md-none rounded-circle mr-3"
                                onClick={() => setSidebarToggled(!sidebarToggled)}
                            >
                                <i className="fa fa-bars"></i>
                            </Button>

                            {/* Topbar Navbar */}
                            <Nav className="ms-auto">
                                {/* Nav Item - User Information */}
                                <Dropdown align="end">
                                    <Dropdown.Toggle 
                                        variant="link" 
                                        className="nav-link"
                                        id="userDropdown"
                                    >
                                        <span className="me-2 d-none d-lg-inline text-gray-600 small">
                                            {user.username}
                                        </span>
                                        <i className="fas fa-user-circle fa-fw"></i>
                                    </Dropdown.Toggle>
                                    <Dropdown.Menu>
                                        <Dropdown.Item onClick={handleLogout}>
                                            <i className="fas fa-sign-out-alt fa-sm fa-fw me-2 text-gray-400"></i>
                                            Logout
                                        </Dropdown.Item>
                                    </Dropdown.Menu>
                                </Dropdown>
                            </Nav>
                        </Container>
                    </Navbar>

                    {/* Begin Page Content */}
                    <Container fluid>
                        {children}
                    </Container>
                </div>

                {/* Footer */}
                <footer className="sticky-footer bg-white">
                    <Container>
                        <div className="copyright text-center my-auto">
                            <span>Copyright © Courier Distribution System 2024</span>
                        </div>
                    </Container>
                </footer>
            </div>
        </div>
    );
};

export default MainLayout; 