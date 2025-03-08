import React, { useEffect, useCallback, useState } from 'react';
import { Card, Row, Col, Switch, Table, Tag, Button, Space, message } from 'antd';
import { EnvironmentOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/auth.context';
import { useStompClient } from '../../contexts/stomp.context';
import ErrorBoundary from '../../components/common/ErrorBoundary';
import { 
  useGetCourierActiveDeliveriesQuery,
  useGetAvailablePackagesQuery,
  useUpdateCourierAvailabilityMutation,
  useAssignDeliveryMutation,
  useUnassignDeliveryMutation,
  useUpdatePackageStatusMutation,
  useUpdateCourierLocationMutation,
  type Package
} from '../../store/api/packages.api';
import { startMockLocationUpdates } from '../../utils/mockLocationLoader';
import type { GPSPoint } from '../../utils/mockLocationLoader';

const statusConfig = {
  PENDING: { color: 'gold' },
  IN_TRANSIT: { color: 'blue' },
  IN_PROGRESS: { color: 'gold' },
  DELIVERED: { color: 'green' },
  CANCELLED: { color: 'red' },
} as const;

type PackageStatus = keyof typeof statusConfig;

const getStatusConfig = (status: string): { color: string } => {
  return (statusConfig[status as PackageStatus] || { color: 'default' });
};

const CourierDashboard: React.FC = () => {
  const [isAvailable, setIsAvailable] = React.useState(true);
  const navigate = useNavigate();
  const { user, checkAuthorization, isLoading, isAuthenticated } = useAuth();
  const { client, connected } = useStompClient();
  const [updateCourierLocation] = useUpdateCourierLocationMutation();
  const [locationCleanup, setLocationCleanup] = useState<(() => void) | null>(null);
  const [assignDelivery] = useAssignDeliveryMutation();

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


  const { data: activeDeliveries = [], isLoading: activeLoading } = useGetCourierActiveDeliveriesQuery(undefined, {
    skip: !isAuthenticated || !user?.username
  });
  const { data: availablePackages = [], isLoading: availableLoading } = useGetAvailablePackagesQuery(undefined, {
    skip: !isAuthenticated || !user?.username
  });
  
  const [updateAvailability] = useUpdateCourierAvailabilityMutation();
  const [unassignDelivery] = useUnassignDeliveryMutation();
  const [updatePackageStatus] = useUpdatePackageStatusMutation();

  const handleAvailabilityChange = async (checked: boolean) => {
    try {
      if (!user?.username) {
        message.error('User not authenticated');
        return;
      }

      await updateAvailability({ 
        username: user.username, 
        isAvailable: checked 
      }).unwrap();
      setIsAvailable(checked);
      message.success(`You are now ${checked ? 'available' : 'unavailable'} for deliveries`);
    } catch (error) {
      message.error('Failed to update availability');
      setIsAvailable(!checked); // Revert the switch if the API call fails
    }
  };

  const handleTakeDelivery = useCallback(async (packageId: number) => {
    try {
      if (!user?.username) {
        message.error('User not authenticated');
        return;
      }

      await assignDelivery({ username: user.username, deliveryId: packageId }).unwrap();
      message.success('Successfully took delivery');

      // Start mock location updates
      const cleanup = startMockLocationUpdates(
        {
          nodeId: Math.floor(Math.random() * 8) + 1,
          intervalMs: 5000
        },
        (location: GPSPoint) => {
          if (client && connected) {
            const trackingNumber = availablePackages.find(p => p.id === packageId)?.trackingNumber;
            if (!trackingNumber) {
              console.error('No tracking number found for package:', packageId);
              return;
            }
            
            client.publish({
              destination: `/app/package/${trackingNumber}/location`,
              body: JSON.stringify({
                lat: location.lat,
                lng: location.lon,
                timestamp: new Date().toISOString(),
                zone: 'UTC',
                trackingNumber: trackingNumber
              })
            });
          }
        }
      );
      
      setLocationCleanup(() => cleanup);
    } catch (error) {
      message.error('Failed to take delivery');
      console.error('Error taking delivery:', error);
    }
  }, [assignDelivery, client, connected, availablePackages, user?.username]);

  const handleUnassignDelivery = async (deliveryId: number) => {
    try {
      if (!user?.username) {
        message.error('User not authenticated');
        return;
      }


      await updatePackageStatus({
        packageId: deliveryId,
        status: 'PENDING'
      }).unwrap();


      await unassignDelivery({
        username: user.username,
        deliveryId
      }).unwrap();

      message.success('Delivery unassigned successfully');
    } catch (error) {
      message.error('Failed to unassign delivery');
    }
  };

  const handleStatusUpdate = async (packageId: number, status: string) => {
    try {
      await updatePackageStatus({ packageId, status }).unwrap();
      message.success('Status updated successfully');
    } catch (error) {
      message.error('Failed to update status');
    }
  };

  const availablePackagesColumns: ColumnsType<Package> = [
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
          <Tag color={config.color}>
            {status}
          </Tag>
        );
      },
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
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Button 
          type="primary"
          onClick={() => handleTakeDelivery(record.id)}
          disabled={!isAvailable}
        >
          Take Delivery
        </Button>
      ),
    },
  ];

  const activeDeliveriesColumns: ColumnsType<Package> = [
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
          <Tag color={config.color}>
            {status}
          </Tag>
        );
      },
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
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button 
            danger 
            onClick={() => handleUnassignDelivery(record.id)}
          >
            Unassign
          </Button>
          <Button
            type="primary"
            onClick={() => handleStatusUpdate(record.id, 'DELIVERED')}
            disabled={record.status === 'DELIVERED'}
          >
            Mark as Delivered
          </Button>
        </Space>
      ),
    },
  ];

  // Add cleanup effect
  useEffect(() => {
    return () => {
      if (locationCleanup) {
        locationCleanup();
      }
    };
  }, [locationCleanup]);

  return (
    <ErrorBoundary>
      <div style={{ padding: 24 }}>
        <Row gutter={[16, 16]}>
          <Col span={24}>
            <Card>
              <Space size="large" align="center">
                <span>Availability Status:</span>
                <Switch
                  checked={isAvailable}
                  onChange={handleAvailabilityChange}
                  checkedChildren="Available"
                  unCheckedChildren="Unavailable"
                />
                <Tag icon={<EnvironmentOutlined />} color="blue">
                  Location Tracking Active
                </Tag>
              </Space>
            </Card>
          </Col>
        </Row>

        <Card title="Active Deliveries" style={{ marginTop: 16 }}>
          <Table<Package>
            columns={activeDeliveriesColumns}
            dataSource={activeDeliveries}
            rowKey="id"
            loading={activeLoading}
            pagination={false}
          />
        </Card>

        <Card title="Available Packages" style={{ marginTop: 16 }}>
          <Table<Package>
            columns={availablePackagesColumns}
            dataSource={availablePackages}
            rowKey="id"
            loading={availableLoading}
            pagination={{
              defaultPageSize: 5,
              showSizeChanger: true,
            }}
          />
        </Card>
      </div>
    </ErrorBoundary>
  );
};

export default CourierDashboard; 