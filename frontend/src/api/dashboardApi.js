
import api from './axiosInstance'

export const getMyPolicies = async () => {
  const response = await api.get('/policies')
  return response.data
}

export const getAvailableProducts = async () => {
  const response = await api.get('/products/available')
  return response.data
}
