import React, { useEffect } from 'react';
import { Card, Row, Col, Statistic, Table, Tag, Button, Space, message } from 'antd';
import { 
  InboxOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  SyncOutlined,
  CloseCircleOutlined
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/auth.context';
import ErrorBoundary from '../../components/common/ErrorBoundary';
import { 
  useGetCustomerPackagesQuery,
  useCancelPackageMutation,
  type Package 
} from '../../store/api/packages.api';

const statusConfig = {
  PENDING: { color: 'gold', icon: <ClockCircleOutlined /> },
  IN_TRANSIT: { color: 'processing', icon: <SyncOutlined spin /> },
  IN_PROGRESS: { color: 'blue', icon: <SyncOutlined spin /> },
  DELIVERED: { color: 'success', icon: <CheckCircleOutlined /> },
  CANCELLED: { color: 'error', icon: <CloseCircleOutlined /> },
} as const;

type PackageStatus = keyof typeof statusConfig;

const CustomerDashboard: React.FC = () => {
  const { user, checkAuthorization, isLoading, isAuthenticated } = useAuth();
  const navigate = useNavigate();


  useEffect(() => {
    const checkAuth = async () => {
      if (!isLoading) {
        if (!isAuthenticated) {
          navigate('/login');
          return;
        }
        const isAuthorized = await checkAuthorization('CUSTOMER');
        if (!isAuthorized) {
          navigate('/login');
        }
      }
    };


    checkAuth();
  }, [isLoading, isAuthenticated, checkAuthorization, navigate]);


  const { data: packages, isLoading: apiLoading } = useGetCustomerPackagesQuery(undefined, {
    skip: !isAuthenticated || !user?.username
  });
  const [cancelPackage] = useCancelPackageMutation();

  const handleCancelPackage = async (packageId: string) => {
    try {
      await cancelPackage(packageId).unwrap();
      message.success('Package cancelled successfully');
    } catch (error) {
      message.error('Failed to cancel package');
    }
  };

  const getStatusConfig = (status: string): { color: string; icon: React.ReactNode } => {
    return (statusConfig[status as PackageStatus] || { color: 'default', icon: null });
  };

  const columns: ColumnsType<Package> = [
    {
      title: 'Tracking Number',
      dataIndex: 'trackingNumber',
      key: 'trackingNumber',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const config = getStatusConfig(status);
        return (
          <Tag color={config.color} icon={config.icon}>
            {status}
          </Tag>
        );
      },
      filters: [
        { text: 'Pending', value: 'PENDING' },
        { text: 'In Transit', value: 'IN_TRANSIT' },
        { text: 'Delivered', value: 'DELIVERED' },
        { text: 'Cancelled', value: 'CANCELLED' },
      ],
      onFilter: (value, record) => record.status === value,
    },
    {
      title: 'Pickup Address',
      dataIndex: 'pickupAddress',
      key: 'pickupAddress',
      ellipsis: true,
    },
    {
      title: 'Delivery Address',
      dataIndex: 'deliveryAddress',
      key: 'deliveryAddress',
      ellipsis: true,
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleDateString(),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            onClick={() => navigate(`/customer/tracking/${record.trackingNumber}`)}
          >
            Track
          </Button>
          {record.status === 'PENDING' && (
            <Button
              type="link"
              danger
              onClick={() => handleCancelPackage(String(record.id))}
            >
              Cancel
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const getPackageStats = () => {
    if (!packages) return { total: 0, active: 0, delivered: 0 };
    
    return {
      total: packages.length,
      active: packages.filter(p => p.status === 'IN_TRANSIT').length,
      delivered: packages.filter(p => p.status === 'DELIVERED').length,
    };
  };

  const stats = getPackageStats();

  return (
    <ErrorBoundary>
      <div style={{ padding: 24 }}>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={8}>
            <Card>
              <Statistic
                title="Total Packages"
                value={stats.total}
                prefix={<InboxOutlined />}
              />
            </Card>
          </Col>
          <Col xs={24} sm={8}>
            <Card>
              <Statistic
                title="Active Deliveries"
                value={stats.active}
                prefix={<SyncOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={8}>
            <Card>
              <Statistic
                title="Delivered Packages"
                value={stats.delivered}
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
        </Row>

        <Card title="My Packages" style={{ marginTop: 16 }}>
          <Table<Package>
            columns={columns}
            dataSource={packages}
            rowKey="id"
            loading={apiLoading}
            pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `Total ${total} packages`,
            }}
          />
        </Card>
      </div>
    </ErrorBoundary>
  );
};

export default CustomerDashboard; 
