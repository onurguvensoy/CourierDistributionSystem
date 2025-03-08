import React, { useState, useEffect } from 'react';
import { Table, Tag, Button, Card, Space, Modal, Form, Input, Select, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { EditOutlined, DeleteOutlined, UserAddOutlined } from '@ant-design/icons';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from '../../store';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/auth.context';

interface User {
  id: string;
  username: string;
  email: string;
  role: 'ADMIN' | 'CUSTOMER' | 'COURIER';
  status: 'ACTIVE' | 'INACTIVE';
  phoneNumber: string;
  createdAt: string;
}

interface UserFormData {
  username: string;
  email: string;
  role: User['role'];
  phoneNumber: string;
  password?: string;
}

const Users: React.FC = () => {
  const [form] = Form.useForm<UserFormData>();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { checkAuthorization, isLoading, isAuthenticated } = useAuth();

  useEffect(() => {
    const checkAuth = async () => {
      if (!isLoading) {
        if (!isAuthenticated) {
          navigate('/login');
          return;
        }
        const isAuthorized = await checkAuthorization('ADMIN');
        if (!isAuthorized) {
          navigate('/login');
        }
      }
    };

    checkAuth();
  }, [isLoading, isAuthenticated, checkAuthorization, navigate]);

  const handleAddUser = () => {
    setEditingUser(null);
    form.resetFields();
    setIsModalOpen(true);
  };

  const handleEditUser = (user: User) => {
    setEditingUser(user);
    form.setFieldsValue({
      username: user.username,
      email: user.email,
      role: user.role,
      phoneNumber: user.phoneNumber,
    });
    setIsModalOpen(true);
  };

  const handleDeleteUser = async (userId: string) => {
    Modal.confirm({
      title: 'Are you sure you want to delete this user?',
      content: 'This action cannot be undone.',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk: async () => {
        try {
          // TODO: Implement delete user API call
          message.success('User deleted successfully');
        } catch (error) {
          message.error('Failed to delete user');
        }
      },
    });
  };

  const handleSubmit = async (values: UserFormData) => {
    setLoading(true);
    try {
      if (editingUser) {
        // TODO: Implement update user API call
        console.log('Updating user:', values);
      } else {
        // TODO: Implement create user API call
        console.log('Creating user:', values);
      }
      message.success(`User ${editingUser ? 'updated' : 'created'} successfully`);
      setIsModalOpen(false);
    } catch (error) {
      message.error(`Failed to ${editingUser ? 'update' : 'create'} user`);
    } finally {
      setLoading(false);
    }
  };

  const columns: ColumnsType<User> = [
    {
      title: 'Username',
      dataIndex: 'username',
      key: 'username',
      sorter: (a, b) => a.username.localeCompare(b.username),
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Role',
      dataIndex: 'role',
      key: 'role',
      render: (role: User['role']) => (
        <Tag color={role === 'ADMIN' ? 'red' : role === 'COURIER' ? 'blue' : 'green'}>
          {role}
        </Tag>
      ),
      filters: [
        { text: 'Admin', value: 'ADMIN' },
        { text: 'Courier', value: 'COURIER' },
        { text: 'Customer', value: 'CUSTOMER' },
      ],
      onFilter: (value, record) => record.role === value,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: User['status']) => (
        <Tag color={status === 'ACTIVE' ? 'success' : 'error'}>
          {status}
        </Tag>
      ),
    },
    {
      title: 'Phone',
      dataIndex: 'phoneNumber',
      key: 'phoneNumber',
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleDateString(),
      sorter: (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    },
    {
      title: 'Action',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEditUser(record)}
          >
            Edit
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDeleteUser(record.id)}
          >
            Delete
          </Button>
        </Space>
      ),
    },
  ];

  // Mock data - replace with API call
  const mockUsers: User[] = [
    {
      id: '1',
      username: 'admin',
      email: 'admin@example.com',
      role: 'ADMIN',
      status: 'ACTIVE',
      phoneNumber: '+90 555 111 2233',
      createdAt: '2024-01-01',
    },
    // Add more mock users...
  ];

  return (
    <>
      <Card
        title="User Management"
        extra={
          <Button
            type="primary"
            icon={<UserAddOutlined />}
            onClick={handleAddUser}
          >
            Add User
          </Button>
        }
      >
        <Table<User>
          columns={columns}
          dataSource={mockUsers}
          rowKey="id"
          pagination={{
            defaultPageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} users`,
          }}
        />
      </Card>

      <Modal
        title={`${editingUser ? 'Edit' : 'Add'} User`}
        open={isModalOpen}
        onCancel={() => setIsModalOpen(false)}
        footer={null}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="username"
            label="Username"
            rules={[{ required: true, message: 'Please input username!' }]}
          >
            <Input disabled={!!editingUser} />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: 'Please input email!' },
              { type: 'email', message: 'Please enter a valid email!' }
            ]}
          >
            <Input />
          </Form.Item>

          {!editingUser && (
            <Form.Item
              name="password"
              label="Password"
              rules={[{ required: true, message: 'Please input password!' }]}
            >
              <Input.Password />
            </Form.Item>
          )}

          <Form.Item
            name="role"
            label="Role"
            rules={[{ required: true, message: 'Please select role!' }]}
          >
            <Select>
              <Select.Option value="ADMIN">Admin</Select.Option>
              <Select.Option value="COURIER">Courier</Select.Option>
              <Select.Option value="CUSTOMER">Customer</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="phoneNumber"
            label="Phone Number"
            rules={[{ required: true, message: 'Please input phone number!' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading}>
                {editingUser ? 'Update' : 'Create'}
              </Button>
              <Button onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default Users; 