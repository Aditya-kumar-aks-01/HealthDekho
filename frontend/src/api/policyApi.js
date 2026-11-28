
import api from './axiosInstance'

export const registerPolicy = async (policyData) => {
  const response = await api.post('/policies/register', policyData)
  return response.data
}
