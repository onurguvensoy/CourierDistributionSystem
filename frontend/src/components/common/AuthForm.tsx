import React from 'react';
import type { FormProps } from 'antd';
import { Button, Checkbox, Form, Input, Card, Select, Space, Row, Col } from 'antd';
import { Link } from 'react-router-dom';
import '../../styles/auth.css';

const { Option } = Select;

export interface AuthFormFields {
  username: string;
  password: string;
  email?: string;
  confirmPassword?: string;
  remember?: boolean;
  role?: 'CUSTOMER' | 'COURIER';
  phoneNumber?: string;
  vehicleType?: 'MOTORCYCLE' | 'CAR' | 'VAN';
}

interface AuthFormProps {
  onSubmit: (values: AuthFormFields) => void;
  isLoading?: boolean;
  error?: string | null;
  type: 'login' | 'signup';
}

const AuthForm: React.FC<AuthFormProps> = ({
  onSubmit,
  isLoading = false,
  error = null,
  type
}) => {
  const [form] = Form.useForm();
  const [selectedRole, setSelectedRole] = React.useState<string>('CUSTOMER');

  const onFinish: FormProps<AuthFormFields>['onFinish'] = (values) => {
    const formattedValues = {
      ...values,
      role: values.role?.toUpperCase(),
      vehicleType: values.vehicleType?.toUpperCase(),
    };
    onSubmit(formattedValues);
  };

  const handleRoleChange = (value: string) => {
    setSelectedRole(value);
    if (value === 'CUSTOMER') {
      form.setFieldValue('vehicleType', undefined);
    }
  };

  return (
    <div className="auth-container">
      <Card className="auth-card">
        <h2 className="auth-title">
          {type === 'login' ? 'Welcome Back!' : 'Create Account'}
        </h2>
        {error && (
          <div className="auth-error">
            {error}
          </div>
        )}
        <Form
          form={form}
          name="auth"
          layout="vertical"
          onFinish={onFinish}
          autoComplete="off"
          disabled={isLoading}
          className="auth-form"
        >
          {type === 'signup' ? (
            <>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item
                    label={<span className="required-label">Username</span>}
                    name="username"
                    rules={[
                      { required: true, message: 'Username is required' },
                      { min: 3, message: 'Username must be at least 3 characters' }
                    ]}
                  >
                    <Input placeholder="Enter your username" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    label={<span className="required-label">Email</span>}
                    name="email"
                    rules={[
                      { required: true, message: 'Email is required' },
                      { type: 'email', message: 'Invalid email format' }
                    ]}
                  >
                    <Input placeholder="Enter your email" />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item
                    label={<span className="required-label">Password</span>}
                    name="password"
                    rules={[
                      { required: true, message: 'Password is required' },
                      { min: 6, message: 'Password must be at least 6 characters' }
                    ]}
                  >
                    <Input.Password placeholder="Enter your password" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    label={<span className="required-label">Confirm Password</span>}
                    name="confirmPassword"
                    dependencies={['password']}
                    rules={[
                      { required: true, message: 'Please confirm your password' },
                      ({ getFieldValue }) => ({
                        validator(_, value) {
                          if (!value || getFieldValue('password') === value) {
                            return Promise.resolve();
                          }
                          return Promise.reject(new Error('Passwords do not match'));
                        },
                      }),
                    ]}
                  >
                    <Input.Password placeholder="Confirm your password" />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item
                    label={<span className="required-label">Role</span>}
                    name="role"
                    initialValue="CUSTOMER"
                    rules={[
                      { required: true, message: 'Role is required' },
                      {
                        pattern: /^(CUSTOMER|COURIER)$/i,
                        message: 'Role must be either CUSTOMER or COURIER'
                      }
                    ]}
                  >
                    <Select onChange={handleRoleChange}>
                      <Option value="CUSTOMER">Customer</Option>
                      <Option value="COURIER">Courier</Option>
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    label={<span className="required-label">Phone Number</span>}
                    name="phoneNumber"
                    rules={[
                      { required: true, message: 'Phone number is required' },
                      {
                        pattern: /^\+?[1-9]\d{1,14}$/,
                        message: 'Invalid phone number format'
                      }
                    ]}
                  >
                    <Input placeholder="+1234567890" />
                  </Form.Item>
                </Col>
              </Row>

              {selectedRole === 'COURIER' && (
                <Row>
                  <Col span={24}>
                    <Form.Item
                      label={<span className="required-label">Vehicle Type</span>}
                      name="vehicleType"
                      rules={[
                        { required: true, message: 'Vehicle type is required for couriers' },
                        {
                          pattern: /^(MOTORCYCLE|CAR|VAN)$/i,
                          message: 'Vehicle type must be MOTORCYCLE, CAR, or VAN'
                        }
                      ]}
                    >
                      <Select placeholder="Select your vehicle type">
                        <Option value="MOTORCYCLE">Motorcycle</Option>
                        <Option value="CAR">Car</Option>
                        <Option value="VAN">Van</Option>
                      </Select>
                    </Form.Item>
                  </Col>
                </Row>
              )}
            </>
          ) : (
            <>
              <Form.Item
                label={<span className="required-label">Username</span>}
                name="username"
                rules={[{ required: true, message: 'Username is required' }]}
              >
                <Input placeholder="Enter your username" />
              </Form.Item>

              <Form.Item
                label={<span className="required-label">Password</span>}
                name="password"
                rules={[{ required: true, message: 'Password is required' }]}
              >
                <Input.Password placeholder="Enter your password" />
              </Form.Item>
            </>
          )}

          {type === 'login' && (
            <div className="auth-options">
              <Form.Item name="remember" valuePropName="checked" noStyle>
                <Checkbox>Remember me</Checkbox>
              </Form.Item>
              <Link to="/forgot-password" className="forgot-link">
                Forgot password?
              </Link>
            </div>
          )}

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={isLoading}
              className="auth-submit"
            >
              {type === 'login' ? 'Login' : 'Sign Up'}
            </Button>
          </Form.Item>

          <div className="auth-divider">
            <span>Or</span>
          </div>

          <div className="auth-footer">
            {type === 'login' ? (
              <Link to="/signup">Create new account</Link>
            ) : (
              <Link to="/login">Already have an account? Login</Link>
            )}
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default AuthForm; 