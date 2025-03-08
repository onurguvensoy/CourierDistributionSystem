import React from 'react';
import { message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { signup } from '../../store/slices/auth.slice';
import type { AppDispatch, RootState } from '../../store';
import AuthForm, { AuthFormFields } from '../../components/common/AuthForm';

const Signup: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { loading } = useSelector((state: RootState) => state.auth);

  const onFinish = async (values: AuthFormFields) => {
    try {
      const { confirmPassword, remember, ...signupData } = values;
      if (!signupData.email || !signupData.role) {
        message.error('Email and role are required');
        return;
      }
      await dispatch(signup({
        ...signupData,
        email: signupData.email,
        role: signupData.role
      })).unwrap();
      message.success('Signup successful! Please login.');
      navigate('/login');
    } catch (error) {
      message.error('Failed to signup');
    }
  };

  return (
    <div className="auth-container">
      <AuthForm
        type="signup"
        onSubmit={onFinish}
        isLoading={loading}
      />
    </div>
  );
};

export default Signup; 