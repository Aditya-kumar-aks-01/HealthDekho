
import api from './axiosInstance'

export const loginCustomer = async (credentials) => {
  const response = await api.post('/customers/login', credentials)
  return response.data
}

export const getCurrentCustomer = async () => {
  const response = await api.get('/customers/me')
  return response.data
}
