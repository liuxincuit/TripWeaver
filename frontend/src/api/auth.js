import api from './index'

export const authApi = {
  async register(data) {
    const response = await api.post('/auth/register', data)
    return response.data
  },

  async login(data) {
    const response = await api.post('/auth/login', data)
    return response.data
  },

  async getCurrentUser() {
    const response = await api.get('/auth/me')
    return response.data
  }
}