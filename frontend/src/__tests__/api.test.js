import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'

// Mock window.location
const originalLocation = window.location
let mockLocation

beforeEach(() => {
  localStorage.clear()
  mockLocation = { href: '', pathname: '/home' }
  Object.defineProperty(window, 'location', {
    value: mockLocation,
    writable: true
  })
})

afterEach(() => {
  Object.defineProperty(window, 'location', {
    value: originalLocation,
    writable: true
  })
})

describe('API interceptors', () => {
  it('should clear localStorage on 401 response', async () => {
    localStorage.setItem('token', 'expired-token')
    localStorage.setItem('username', 'testuser')
    localStorage.setItem('email', 'test@example.com')

    // Import api after setting up mocks
    const { default: api } = await import('../api/index.js')
    const mock = new MockAdapter(api)

    mock.onGet('/test').reply(401)

    try {
      await api.get('/test')
    } catch (e) {
      // Expected to fail
    }

    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('username')).toBeNull()
    expect(localStorage.getItem('email')).toBeNull()
  })

  it('should redirect to /login on 401 when not on login/register page', async () => {
    mockLocation.pathname = '/home'
    localStorage.setItem('token', 'expired-token')

    const { default: api } = await import('../api/index.js')
    const mock = new MockAdapter(api)

    mock.onGet('/test').reply(401)

    try {
      await api.get('/test')
    } catch (e) {
      // Expected to fail
    }

    expect(mockLocation.href).toBe('/login')
  })

  it('should not redirect on 401 when already on /login page', async () => {
    mockLocation.pathname = '/login'
    localStorage.setItem('token', 'expired-token')

    const { default: api } = await import('../api/index.js')
    const mock = new MockAdapter(api)

    mock.onGet('/test').reply(401)

    try {
      await api.get('/test')
    } catch (e) {
      // Expected to fail
    }

    expect(mockLocation.href).toBe('')
  })

  it('should not redirect on 401 when already on /register page', async () => {
    mockLocation.pathname = '/register'
    localStorage.setItem('token', 'expired-token')

    const { default: api } = await import('../api/index.js')
    const mock = new MockAdapter(api)

    mock.onGet('/test').reply(401)

    try {
      await api.get('/test')
    } catch (e) {
      // Expected to fail
    }

    expect(mockLocation.href).toBe('')
  })

  it('should inject Authorization header with token from localStorage', async () => {
    localStorage.setItem('token', 'test-token')

    const { default: api } = await import('../api/index.js')
    const mock = new MockAdapter(api)

    mock.onGet('/test').reply(200, { success: true })

    await api.get('/test')

    const request = mock.history.get[0]
    expect(request.headers.Authorization).toBe('Bearer test-token')
  })

  it('should pass through non-401 errors without redirecting', async () => {
    mockLocation.pathname = '/home'
    localStorage.setItem('token', 'test-token')

    const { default: api } = await import('../api/index.js')
    const mock = new MockAdapter(api)

    mock.onGet('/test').reply(500)

    let errorThrown = null
    try {
      await api.get('/test')
    } catch (e) {
      errorThrown = e
    }

    expect(errorThrown).not.toBeNull()
    expect(errorThrown.response.status).toBe(500)
    expect(mockLocation.href).toBe('')
    expect(localStorage.getItem('token')).toBe('test-token')
  })
})
