import React from 'react';
import { Table, Tag, Card } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  CheckCircleOutlined,
  SyncOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons';
import { useGetDeliveryHistoryQuery, type Package } from '../../store/api/packages.api';

const DeliveryHistory: React.FC = () => {
  const { data: deliveryHistory, isLoading } = useGetDeliveryHistoryQuery();

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
      sorter: (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    },
    {
      title: 'Completed At',
      dataIndex: 'completedAt',
      key: 'completedAt',
      render: (date: string | undefined) => date ? new Date(date).toLocaleDateString() : '-',
      sorter: (a, b) => {
        if (!a.completedAt) return 1;
        if (!b.completedAt) return -1;
        return new Date(a.completedAt).getTime() - new Date(b.completedAt).getTime();
      },
    },
    {
      title: 'Courier',
      dataIndex: 'courierUsername',
      key: 'courierUsername',
      render: (courier: string | undefined) => courier || '-',
    },
  ];

  return (
    <Card title="Delivery History">
      <Table<Package>
        columns={columns}
        dataSource={deliveryHistory}
        rowKey="id"
        loading={isLoading}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: true,
          showTotal: (total) => `Total ${total} deliveries`,
        }}
      />
    </Card>
  );
};

export default DeliveryHistory; 