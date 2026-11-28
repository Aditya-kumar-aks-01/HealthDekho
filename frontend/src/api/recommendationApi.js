
import api from './axiosInstance'

export const generateRecommendations = async (requestData) => {
  const response = await api.post('/recommendations', requestData)
  return response.data
}
