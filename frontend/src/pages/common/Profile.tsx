import React, { useState } from 'react';
import { Card, Form, Input, Button, Row, Col, Avatar, Upload, message, Descriptions, Space, Typography } from 'antd';
import { UserOutlined, EditOutlined, SaveOutlined, UploadOutlined } from '@ant-design/icons';
import { useAuth } from '../../contexts/auth.context';
import type { UploadProps } from 'antd';
import type { RcFile } from 'antd/es/upload/interface';
import { Navigate } from 'react-router-dom';

const { Title } = Typography;

interface ProfileFormData {
  username: string;
  email: string;
  phoneNumber: string;
  fullName: string;
  address?: string;
}

const ProfileContent: React.FC = () => {
  const { user } = useAuth();
  const [form] = Form.useForm<ProfileFormData>();
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(false);

  // Mock initial data - replace with actual API call
  const initialData: ProfileFormData = {
    username: user?.username || '',
    email: 'user@example.com',
    phoneNumber: '+90 555 123 4567',
    fullName: 'John Doe',
    address: 'Istanbul, Turkey'
  };

  const handleSubmit = async (values: ProfileFormData) => {
    setLoading(true);
    try {
      // TODO: Implement API call to update profile
      console.log('Updating profile:', values);
      message.success('Profile updated successfully');
      setIsEditing(false);
    } catch (error) {
      message.error('Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const uploadProps: UploadProps = {
    name: 'avatar',
    showUploadList: false,
    beforeUpload: (file: RcFile) => {
      const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
      if (!isJpgOrPng) {
        message.error('You can only upload JPG/PNG files!');
        return false;
      }
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        message.error('Image must be smaller than 2MB!');
        return false;
      }
      return true;
    },
    onChange: (info) => {
      if (info.file.status === 'done') {
        message.success('Avatar uploaded successfully');
      } else if (info.file.status === 'error') {
        message.error('Avatar upload failed');
      }
    },
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <Row gutter={[24, 24]}>
        <Col xs={24} md={8}>
          <Card>
            <div style={{ textAlign: 'center' }}>
              <Avatar 
                size={120} 
                icon={<UserOutlined />} 
                style={{ marginBottom: 24 }}
              />
              <div style={{ marginBottom: 24 }}>
                <Upload {...uploadProps}>
                  <Button icon={<UploadOutlined />}>Change Avatar</Button>
                </Upload>
              </div>
              <Descriptions 
                column={1} 
                title={<Title level={5}>Account Information</Title>}
                bordered
                size="small"
              >
                <Descriptions.Item label="Role">{user?.role}</Descriptions.Item>
                <Descriptions.Item label="Member Since">January 2024</Descriptions.Item>
                <Descriptions.Item label="Status">
                  <span style={{ color: '#52c41a' }}>Active</span>
                </Descriptions.Item>
              </Descriptions>
            </div>
          </Card>
        </Col>
        <Col xs={24} md={16}>
          <Card
            title={<Title level={4}>Profile Information</Title>}
            extra={
              !isEditing ? (
                <Button 
                  type="primary" 
                  icon={<EditOutlined />}
                  onClick={() => setIsEditing(true)}
                >
                  Edit Profile
                </Button>
              ) : null
            }
          >
            {!isEditing ? (
              <Descriptions bordered column={2}>
                <Descriptions.Item label="Username">{initialData.username}</Descriptions.Item>
                <Descriptions.Item label="Full Name">{initialData.fullName}</Descriptions.Item>
                <Descriptions.Item label="Email">{initialData.email}</Descriptions.Item>
                <Descriptions.Item label="Phone">{initialData.phoneNumber}</Descriptions.Item>
                <Descriptions.Item label="Address" span={2}>{initialData.address}</Descriptions.Item>
              </Descriptions>
            ) : (
              <Form
                form={form}
                layout="vertical"
                initialValues={initialData}
                onFinish={handleSubmit}
              >
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      name="username"
                      label="Username"
                      rules={[{ required: true, message: 'Username is required' }]}
                    >
                      <Input disabled />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      name="fullName"
                      label="Full Name"
                      rules={[{ required: true, message: 'Full name is required' }]}
                    >
                      <Input />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      name="email"
                      label="Email"
                      rules={[
                        { required: true, message: 'Email is required' },
                        { type: 'email', message: 'Please enter a valid email' }
                      ]}
                    >
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      name="phoneNumber"
                      label="Phone Number"
                      rules={[{ required: true, message: 'Phone number is required' }]}
                    >
                      <Input />
                    </Form.Item>
                  </Col>
                </Row>
                <Form.Item
                  name="address"
                  label="Address"
                >
                  <Input.TextArea rows={3} />
                </Form.Item>
                <Form.Item>
                  <Space>
                    <Button 
                      type="primary" 
                      htmlType="submit" 
                      icon={<SaveOutlined />}
                      loading={loading}
                    >
                      Save Changes
                    </Button>
                    <Button onClick={() => setIsEditing(false)}>
                      Cancel
                    </Button>
                  </Space>
                </Form.Item>
              </Form>
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
};

const Profile: React.FC = () => {
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <ProfileContent />;
};

export default Profile;