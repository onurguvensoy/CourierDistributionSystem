import React from 'react';
import { 
  Form, 
  Input, 
  InputNumber, 
  Button, 
  Card, 
  Select, 
  Checkbox,
  message
} from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/auth.context';
import { useCreatePackageMutation } from '../../store/api/packages.api';

const { TextArea } = Input;
const { Option } = Select;

interface PackageFormData {
  pickupAddress: string;
  deliveryAddress: string;
  weight: number;
  description?: string;
  specialInstructions?: boolean;
  priority: 'NORMAL' | 'EXPRESS' | 'URGENT';
  fragile: boolean;
}

const NewPackage: React.FC = () => {
  const [form] = Form.useForm<PackageFormData>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [createPackage, { isLoading }] = useCreatePackageMutation();

  React.useEffect(() => {
    if (!user) {
      message.error('Please login to continue');
      navigate('/login');
      return;
    }
    if (user?.role !== 'CUSTOMER') {
      message.error('Unauthorized access');
      navigate('/login');
    }
  }, [user, navigate]);

  const onFinish = async (values: PackageFormData) => {
    try {
      await createPackage(values).unwrap();
      message.success('Package created successfully');
      navigate('/customer/packages');
    } catch (error: any) {
      if (error.response?.status === 401) {
        message.error('Session expired. Please login again');
        navigate('/login');
        return;
      }
      if (error.response?.status === 400) {
        const errorMessage = error.response.data.message || 'Invalid package data';
        message.error(errorMessage);
        return;
      }
      message.error('Failed to create package. Please try again.');
    }
  };

  if (!user) {
    return null;
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
      <Card title="Create New Package" variant="outlined">
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{
            priority: 'NORMAL',
            fragile: false
          }}
        >
          <Form.Item
            label="Pickup Address"
            name="pickupAddress"
            rules={[
              { required: true, message: 'Pickup address is required' },
              { min: 5, message: 'Address must be at least 5 characters' }
            ]}
          >
            <TextArea 
              rows={3} 
              placeholder="Enter the pickup address"
              maxLength={200}
              showCount
            />
          </Form.Item>

          <Form.Item
            label="Delivery Address"
            name="deliveryAddress"
            rules={[
              { required: true, message: 'Delivery address is required' },
              { min: 5, message: 'Address must be at least 5 characters' }
            ]}
          >
            <TextArea 
              rows={3} 
              placeholder="Enter the delivery address"
              maxLength={200}
              showCount
            />
          </Form.Item>

          <Form.Item
            label="Weight (kg)"
            name="weight"
            rules={[
              { required: true, message: 'Weight is required' },
              { type: 'number', min: 0.1, message: 'Weight must be greater than 0' }
            ]}
          >
            <InputNumber 
              min={0.1}
              step={0.1}
              style={{ width: '100%' }}
              placeholder="Enter package weight in kilograms"
            />
          </Form.Item>

          <Form.Item
            label="Package Description"
            name="description"
          >
            <TextArea 
              rows={4}
              placeholder="Enter package description (optional)"
              maxLength={500}
              showCount
            />
          </Form.Item>

          <Form.Item
            label="Special Instructions"
            name="specialInstructions"
          >
            <TextArea 
              rows={3}
              placeholder="Enter any special handling instructions (optional)"
              maxLength={300}
              showCount
            />
          </Form.Item>

          <Form.Item
            label="Priority Level"
            name="priority"
            rules={[{ required: true, message: 'Priority level is required' }]}
          >
            <Select placeholder="Select priority level">
              <Option value="NORMAL">Normal</Option>
              <Option value="EXPRESS">Express</Option>
              <Option value="URGENT">Urgent</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="fragile"
            valuePropName="checked"
          >
            <Checkbox>This is a fragile package</Checkbox>
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary"
              htmlType="submit"
              loading={isLoading}
              style={{ marginRight: 16 }}
            >
              Create Package
            </Button>
            <Button onClick={() => navigate('/customer/packages')}>
              Cancel
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default NewPackage; 