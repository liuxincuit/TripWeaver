import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../stores/user'

describe('UserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('should start with empty state', () => {
    const store = useUserStore()

    expect(store.token).toBe('')
    expect(store.username).toBe('')
    expect(store.isLoggedIn).toBe(false)
  })

  it('should set auth data', () => {
    const store = useUserStore()

    store.setAuth('test-token', 'testuser', 'test@example.com')

    expect(store.token).toBe('test-token')
    expect(store.username).toBe('testuser')
    expect(store.isLoggedIn).toBe(true)
    expect(localStorage.getItem('token')).toBe('test-token')
  })

  it('should clear auth data on logout', () => {
    const store = useUserStore()

    store.setAuth('test-token', 'testuser', 'test@example.com')
    store.clearAuth()

    expect(store.token).toBe('')
    expect(store.username).toBe('')
    expect(store.isLoggedIn).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
  })
})