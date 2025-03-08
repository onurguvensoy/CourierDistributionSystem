import React, { useState, useEffect } from 'react';
import { Card, Input, Button, Form, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { SearchOutlined } from '@ant-design/icons';
import { useAuth } from '../../contexts/auth.context';

const TrackPackageSearch: React.FC = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const { isLoading, isAuthenticated, checkAuthorization } = useAuth();

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

  const handleSearch = async (values: { trackingNumber: string }) => {
    setLoading(true);
    try {
      // Navigate to the tracking page with the tracking number
      navigate(`/customer/tracking/${values.trackingNumber.trim()}`);
    } catch (error) {
      message.error('Invalid tracking number');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card title="Track Package">
      <Form
        form={form}
        onFinish={handleSearch}
        layout="vertical"
      >
        <Form.Item
          name="trackingNumber"
          label="Tracking Number"
          rules={[{ required: true, message: 'Please enter tracking number' }]}
        >
          <Input
            placeholder="Enter tracking number"
            prefix={<SearchOutlined />}
            size="large"
          />
        </Form.Item>
        <Form.Item>
          <Button
            type="primary"
            htmlType="submit"
            loading={loading}
            icon={<SearchOutlined />}
            size="large"
            block
          >
            Track Package
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default TrackPackageSearch; 