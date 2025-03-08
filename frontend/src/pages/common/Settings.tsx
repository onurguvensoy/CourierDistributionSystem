import React, { useEffect } from 'react';
import { Card, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/auth.context';

const { Title } = Typography;

const Settings: React.FC = () => {
    const navigate = useNavigate();
    const { user, isLoading, isAuthenticated } = useAuth();

    useEffect(() => {
        if (!isLoading && !isAuthenticated) {
            navigate('/login');
        }
    }, [isLoading, isAuthenticated, navigate]);

    if (!user) {
        return null;
    }

    return (
        <div className="container-fluid">
            <Title level={3} style={{ marginBottom: 24 }}>Settings</Title>
            <Card 
                title={<span className="font-weight-bold">User Settings</span>}
                className="shadow"
            >
                <p>Settings functionality coming soon...</p>
                <p>Current user: {user.username}</p>
            </Card>
        </div>
    );
};

export default Settings;