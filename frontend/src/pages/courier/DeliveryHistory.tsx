import React, { useEffect } from 'react';
import { Card, Table, Tag, Space, message } from 'antd';
import { CheckCircleOutlined, ClockCircleOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/auth.context';
import { useGetCourierDeliveryHistoryQuery, type Package } from '../../store/api/packages.api';

const DeliveryHistory: React.FC = () => {
  const navigate = useNavigate();
  const { user, checkAuthorization, isLoading, isAuthenticated } = useAuth();

  useEffect(() => {
    const checkAuth = async () => {
      if (!isLoading) {
        if (!isAuthenticated) {
          navigate('/login');
          return;
        }
        const isAuthorized = await checkAuthorization('COURIER');
        if (!isAuthorized) {
          navigate('/login');
        }
      }
    };

    checkAuth();
  }, [isLoading, isAuthenticated, checkAuthorization, navigate]);

  const { data: deliveryHistory = [], isLoading: apiLoading } = useGetCourierDeliveryHistoryQuery(
    user?.username || '',
    { skip: !isAuthenticated || !user?.username }
  );

  const columns: ColumnsType<Package> = [
    {
      title: 'Tracking Number',
      dataIndex: 'trackingNumber',
      key: 'trackingNumber',
      sorter: (a, b) => a.trackingNumber.localeCompare(b.trackingNumber),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'DELIVERED' ? 'success' : 'processing'} icon={status === 'DELIVERED' ? <CheckCircleOutlined /> : <ClockCircleOutlined />}>
          {status}
        </Tag>
      ),
      filters: [
        { text: 'Delivered', value: 'DELIVERED' },
        { text: 'In Transit', value: 'IN_TRANSIT' },
      ],
      onFilter: (value, record) => record.status === value,
    },
    {
      title: 'Pickup Address',
      dataIndex: 'pickupLocation',
      key: 'pickupAddress',
      ellipsis: true,
    },
    {
      title: 'Delivery Address',
      dataIndex: 'deliveryLocation',
      key: 'deliveryAddress',
      ellipsis: true,
    },
    {
      title: 'Completed At',
      dataIndex: 'completedAt',
      key: 'completedAt',
      render: (date: string) => date ? new Date(date).toLocaleString() : '-',
      sorter: (a, b) => {
        if (!a.completedAt) return 1;
        if (!b.completedAt) return -1;
        return new Date(b.completedAt).getTime() - new Date(a.completedAt).getTime();
      },
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card title="Delivery History">
        <Table<Package>
          columns={columns}
          dataSource={deliveryHistory}
          rowKey="id"
          loading={apiLoading}
          pagination={{
            defaultPageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} deliveries`,
          }}
        />
      </Card>
    </div>
  );
};

export default DeliveryHistory; 