import { Navigate, useLocation } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/auth.context'
import { Spin } from 'antd'

interface ProtectedRouteProps {
  children: React.ReactNode
  allowedRoles?: string[]
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const location = useLocation()
  const { user, isAuthenticated } = useAuth()
  const [isChecking, setIsChecking] = useState(true)

  useEffect(() => {

    setIsChecking(false);
  }, []);

  if (isChecking) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to={`/${user.role.toLowerCase()}/dashboard`} replace />
  }

  return <>{children}</>
} 