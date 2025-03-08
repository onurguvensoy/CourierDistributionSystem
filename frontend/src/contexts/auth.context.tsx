import React, { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { message } from 'antd'
import { useDispatch } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { AppDispatch } from '../store'
import { logout as logoutAction } from '../store/slices/auth.slice'
import { decodeToken, isTokenExpired } from '../utils/tokenUtils'

interface AuthContextType {
  isAuthenticated: boolean
  isLoading: boolean
  user: {
    username: string | null
    role: string | null
    userId: string | null
  } | null
  checkAuthorization: (requiredRole: string) => Promise<boolean>
  validateToken: () => Promise<boolean>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const dispatch = useDispatch<AppDispatch>()
  const navigate = useNavigate()
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false)
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [user, setUser] = useState<AuthContextType['user']>(null)
  const [lastValidation, setLastValidation] = useState<number>(0)

  const clearAuthState = useCallback(async () => {
    localStorage.removeItem('token')
    await dispatch(logoutAction()).unwrap()
    setIsAuthenticated(false)
    setUser(null)
    navigate('/login')
  }, [dispatch, navigate])

  const validateToken = useCallback(async (): Promise<boolean> => {
    try {
      const token = localStorage.getItem('token')
      
      if (!token || isTokenExpired(token)) {
        await clearAuthState()
        return false
      }

      const decoded = decodeToken(token)
      if (!decoded) {
        await clearAuthState()
        return false
      }

      const newUser = {
        username: decoded.sub || null,
        role: decoded.role?.toUpperCase() || null,
        userId: String(decoded.userId) || null
      }

      setUser(newUser)
      setIsAuthenticated(true)
      setLastValidation(Date.now())
      return true
    } catch (error) {
      console.error('Token validation error:', error)
      await clearAuthState()
      return false
    }
  }, [clearAuthState])

  const checkAuthorization = useCallback(async (requiredRole: string): Promise<boolean> => {

    const now = Date.now()
    if (now - lastValidation < 5000 && isAuthenticated && user?.role === requiredRole) {
      return true
    }

    const isValid = await validateToken()
    if (!isValid) {
      message.error('Your session has expired. Please login again.')
      await clearAuthState()
      return false
    }

    if (!user?.role || user.role !== requiredRole) {
      message.error('You do not have permission to access this page')
      await clearAuthState()
      return false
    }

    return true
  }, [isAuthenticated, user, validateToken, clearAuthState, lastValidation])

  const handleLogout = async () => {
    try {
      await clearAuthState()
    } catch (error) {
      console.error('Logout failed:', error)
      navigate('/login')
    }
  }


  useEffect(() => {
    const initializeAuth = async () => {
      await validateToken()
      setIsLoading(false)
    }
    initializeAuth()
  }, [validateToken])


  useEffect(() => {
    const validateAndUpdate = async () => {
      await validateToken()
    }

    const interval = setInterval(validateAndUpdate, 60000)
    return () => clearInterval(interval)
  }, [validateToken])

  const value = {
    isAuthenticated,
    isLoading,
    user,
    checkAuthorization,
    validateToken,
    logout: handleLogout
  }

  if (isLoading) {
    return null
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export default AuthContext