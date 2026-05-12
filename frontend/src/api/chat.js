import api from './index'

export const chatApi = {
  async sendMessage(planId, message) {
    const response = await api.post('/chat/send', { planId, message })
    return response.data
  },

  async createNewPlan() {
    const response = await api.post('/chat/new')
    return response.data
  },

  async getHistory(planId) {
    const response = await api.get(`/chat/history/${planId}`)
    return response.data
  },

  async getPlans() {
    const response = await api.get('/plans')
    return response.data
  },

  async getPlan(id) {
    const response = await api.get(`/plans/${id}`)
    return response.data
  },

  async updatePlan(id, data) {
    const response = await api.put(`/plans/${id}`, data)
    return response.data
  },

  async deletePlan(id) {
    const response = await api.delete(`/plans/${id}`)
    return response.data
  }
}