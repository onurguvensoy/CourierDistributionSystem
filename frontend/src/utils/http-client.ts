import { useCallback, useEffect, useRef } from 'react'
import { isTokenExpired, getStoredToken, removeStoredToken, setStoredToken } from './tokenUtils'
import type { ApiError } from '../types'

interface RequestOptions extends RequestInit {
  requiresAuth?: boolean
}

export function useHttpClient() {
  const API_URL = import.meta.env.VITE_API_URL
  const refreshPromise = useRef<Promise<void> | null>(null)

  const request = useCallback(async <T>(
    endpoint: string,
    options: RequestOptions = {}
  ): Promise<T> => {
    const { requiresAuth = true, ...fetchOptions } = options
    const headers = new Headers(fetchOptions.headers)

    if (requiresAuth) {
      const token = getStoredToken()
      if (!token) {
        throw new Error('No authentication token found')
      }

      if (isTokenExpired(token)) {
        await refreshToken()
      }

      headers.set('Authorization', `Bearer ${getStoredToken()}`)
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
      ...fetchOptions,
      headers,
    })

    if (!response.ok) {
      const error: ApiError = {
        message: 'An error occurred',
        status: response.status,
      }
      
      try {
        const data = await response.json()
        error.message = data.message
      } catch {
        // Use default error message
      }
      
      throw error
    }

    return response.json()
  }, [])

  const get = useCallback(<T>(endpoint: string, options?: RequestOptions) => 
    request<T>(endpoint, { ...options, method: 'GET' }), [request])

  const post = useCallback(<T>(endpoint: string, data: unknown, options?: RequestOptions) =>
    request<T>(endpoint, { 
      ...options, 
      method: 'POST',
      body: JSON.stringify(data),
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    }), [request])

  const refreshToken = useCallback(async () => {
    if (!refreshPromise.current) {
      refreshPromise.current = post<{ token: string }>('/auth/refresh', {})
        .then(response => {
          setStoredToken(response.token)
        })
        .finally(() => {
          refreshPromise.current = null
        })
    }
    return refreshPromise.current
  }, [post])


  useEffect(() => {
    const checkTokenInterval = setInterval(async () => {
      const token = getStoredToken()
      if (token && isTokenExpired(token)) {
        try {
          await refreshToken()
        } catch (error) {
          // Token refresh failed - user needs to login again
          removeStoredToken()
          window.location.href = '/login'
        }
      }
    }, 300000) // Check every 5 minutes

    return () => clearInterval(checkTokenInterval)
  }, [refreshToken])

  return {
    get,
    post
  }
} 