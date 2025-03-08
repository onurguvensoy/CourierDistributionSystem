import React from 'react';
import { Layout, Menu, Avatar, Dropdown, Button, theme } from 'antd';
import { 
  MenuFoldOutlined, 
  MenuUnfoldOutlined, 
  UserOutlined,
  LogoutOutlined,
  PlusOutlined,
  HistoryOutlined,
  DashboardOutlined,
  SearchOutlined,
  InboxOutlined,
  TeamOutlined
} from '@ant-design/icons';
import { useLocation, useNavigate, Outlet, Link } from 'react-router-dom';
import { useAuth } from '../../contexts/auth.context';

const { Header, Sider, Content } = Layout;

interface DashboardLayoutProps {
  children?: React.ReactNode;
}

const DashboardLayout: React.FC<DashboardLayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = React.useState(false);
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const getMenuItems = () => {
    const role = user?.role?.toLowerCase();

    const menuConfigs = {
      customer: [
        {
          key: 'customer/dashboard',
          icon: <DashboardOutlined />,
          label: <Link to="/customer/dashboard">Dashboard</Link>,
        },
        {
          key: 'customer/packages',
          icon: <InboxOutlined />,
          label: <Link to="/customer/packages">My Packages</Link>,
        },
        {
          key: 'customer/new-package',
          icon: <PlusOutlined />,
          label: <Link to="/customer/new-package">New Package</Link>,
        },
        {
          key: 'customer/history',
          icon: <HistoryOutlined />,
          label: <Link to="/customer/history">History</Link>,
        },
        {
          key: 'track-package',
          icon: <SearchOutlined />,
          label: <Link to="/customer/tracking">Track Package</Link>,
        },
      ],
      courier: [
        {
          key: 'courier/dashboard',
          icon: <DashboardOutlined />,
          label: <Link to="/courier/dashboard">Dashboard</Link>,
        },
        {
          key: 'courier/history',
          icon: <HistoryOutlined />,
          label: <Link to="/courier/history">Delivery History</Link>,
        },
      ],
      admin: [
        {
          key: 'admin/dashboard',
          icon: <DashboardOutlined />,
          label: <Link to="/admin/dashboard">Dashboard</Link>,
        },
        {
          key: 'admin/users',
          icon: <TeamOutlined />,
          label: <Link to="/admin/users">Users</Link>,
        },
      ],
    };

    return menuConfigs[role as keyof typeof menuConfigs] || [];
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: <Link to="/profile">Profile</Link>,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      onClick: handleLogout,
    },
  ];

  const handleMenuClick = ({ key }: { key: string }) => {
    if (key !== 'track-package') {
      navigate(key);
    }
  };

  const getDefaultRoute = () => {
    const role = user?.role?.toLowerCase();
    switch (role) {
      case 'customer':
        return '/customer/dashboard';
      case 'courier':
        return '/courier/dashboard';
      case 'admin':
        return '/admin/dashboard';
      default:
        return '/login';
    }
  };

  const handleLogoClick = () => {
    navigate(getDefaultRoute());
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div 
          className="logo" 
          style={{ 
            height: 32, 
            margin: 16, 
            background: 'rgba(255, 255, 255, 0.2)',
            color: 'white',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '18px',
            fontWeight: 'bold',
            cursor: 'pointer'
          }}
          onClick={handleLogoClick}
        >
          {!collapsed ? 'Courier App' : 'CA'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={getMenuItems()}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header style={{ padding: 0, background: colorBgContainer }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingRight: 24 }}>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{ fontSize: '16px', width: 64, height: 64 }}
            />
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
                <span>{user?.username}</span>
                <Avatar icon={<UserOutlined />} />
              </div>
            </Dropdown>
          </div>
        </Header>
        <Content style={{ margin: '24px 16px', padding: 24, background: colorBgContainer, borderRadius: borderRadiusLG }}>
          {children || <Outlet />}
        </Content>
      </Layout>
    </Layout>
  );
};

export default DashboardLayout; 