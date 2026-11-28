
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from 'react'

import {
  getCurrentCustomer,
  loginCustomer,
} from '../api/authApi'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [customer, setCustomer] = useState(null)
  const [loading, setLoading] = useState(true)

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('tokenType')
    setCustomer(null)
  }, [])

  const login = async (credentials) => {
    const authResponse = await loginCustomer(credentials)

    localStorage.setItem('accessToken', authResponse.accessToken)
    localStorage.setItem('tokenType', authResponse.tokenType || 'Bearer')

    try {
      const customerData = await getCurrentCustomer()
      setCustomer(customerData)
      return customerData
    } catch (error) {
      logout()
      throw error
    }
  }

  useEffect(() => {
    let active = true

    const restoreSession = async () => {
      const token = localStorage.getItem('accessToken')

      if (!token) {
        setLoading(false)
        return
      }

      try {
        const customerData = await getCurrentCustomer()

        if (active) {
          setCustomer(customerData)
        }
      } catch {
        if (active) {
          logout()
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    restoreSession()

    return () => {
      active = false
    }
  }, [logout])

  return (
    <AuthContext.Provider
      value={{
        customer,
        loading,
        login,
        logout,
        isAuthenticated: Boolean(customer),
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}
