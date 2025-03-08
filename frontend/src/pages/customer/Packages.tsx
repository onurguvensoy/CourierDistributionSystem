import React, { useEffect } from 'react';
import { Table, Tag, Button, Card, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import {
  CheckCircleOutlined,
  SyncOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons';
import { useAuth } from '../../contexts/auth.context';
import {
  useGetCustomerPackagesQuery,
  useCancelPackageMutation,
  type Package
} from '../../store/api/packages.api';

const Packages: React.FC = () => {
  const navigate = useNavigate();
  const { user, checkAuthorization, isLoading, isAuthenticated } = useAuth();
  const { data: packages, isLoading: apiLoading } = useGetCustomerPackagesQuery(undefined, {
    skip: !isAuthenticated || !user?.username
  });
  const [cancelPackage] = useCancelPackageMutation();

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

  const handleCancelPackage = async (packageId: string) => {
    try {
      await cancelPackage(packageId).unwrap();
      message.success('Package cancelled successfully');
    } catch (error) {
      message.error('Failed to cancel package');
    }
  };

  const handleCreatePackage = () => {
    if (!user) {
      message.error('Please login to create a package');
      navigate('/login', { state: { from: '/customer/new-package' } });
      return;
    }
    navigate('/customer/new-package');
  };

  const handleTrackPackage = (trackingNumber: string) => {
    navigate(`/customer/tracking/${trackingNumber}`);
  };

  const statusConfig = {
    PENDING: { color: 'gold', icon: <ClockCircleOutlined /> },
    IN_TRANSIT: { color: 'blue', icon: <SyncOutlined spin /> },
    IN_PROGRESS: { color: 'blue', icon: <SyncOutlined spin /> },
    DELIVERED: { color: 'green', icon: <CheckCircleOutlined /> },
    CANCELLED: { color: 'red', icon: <CloseCircleOutlined /> },
  } as const;

  type PackageStatus = keyof typeof statusConfig;

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
      render: (status: PackageStatus) => (
        <Tag color={statusConfig[status].color} icon={statusConfig[status].icon}>
          {status}
        </Tag>
      ),
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
        <div style={{ display: 'flex', gap: 8 }}>
          <Button
            type="link"
            onClick={() => handleTrackPackage(record.trackingNumber)}
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
        </div>
      ),
    },
  ];

  return (
    <Card 
      title="My Packages" 
      extra={
        <Button 
          type="primary" 
          onClick={handleCreatePackage}
        >
          Create New Package
        </Button>
      }
    >
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
  );
};

export default Packages;