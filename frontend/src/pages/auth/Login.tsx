import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { message } from 'antd';
import AuthForm, { AuthFormFields } from '../../components/common/AuthForm';
import { login } from '../../store/slices/auth.slice';
import type { RootState, AppDispatch } from '../../store';
import { getDashboardPath } from '../../utils/navigation';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch<AppDispatch>();
  const { loading } = useSelector((state: RootState) => state.auth);

  const onFinish = async (values: AuthFormFields) => {
    try {
      const result = await dispatch(login(values)).unwrap();
      if (result.user?.role) {

        const from = (location.state as any)?.from?.pathname || getDashboardPath(result.user.role);
        message.success('Login successful!');
        navigate(from);
      } else {
        message.error('Invalid user role received');
      }
    } catch (error) {
      message.error('Login failed. Please check your credentials.');
    }
  };

  return (
    <div className="auth-container">
      <AuthForm
        type="login"
        onSubmit={onFinish}
        isLoading={loading}
      />
    </div>
  );
};

export default Login;
