import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const email = ref(localStorage.getItem('email') || '')

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(newToken, newUsername, newEmail) {
    token.value = newToken
    username.value = newUsername
    email.value = newEmail
    localStorage.setItem('token', newToken)
    localStorage.setItem('username', newUsername)
    localStorage.setItem('email', newEmail)
  }

  function clearAuth() {
    token.value = ''
    username.value = ''
    email.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
  }

  async function login(loginData) {
    const { authApi } = await import('../api/auth')
    const response = await authApi.login(loginData)
    setAuth(response.token, response.username, response.email)
    return response
  }

  async function register(registerData) {
    const { authApi } = await import('../api/auth')
    const response = await authApi.register(registerData)
    setAuth(response.token, response.username, response.email)
    return response
  }

  function logout() {
    clearAuth()
  }

  return {
    token,
    username,
    email,
    isLoggedIn,
    setAuth,
    clearAuth,
    login,
    register,
    logout
  }
})