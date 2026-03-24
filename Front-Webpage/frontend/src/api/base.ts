import axios, { AxiosHeaders } from 'axios'
import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import router from '../router'
import { createMockAdapter, isMockEnabled } from '../mock/httpMock'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://115.190.101.111:10001/',
  withCredentials: true,
  timeout: 15000
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      const headers =
        config.headers instanceof AxiosHeaders
          ? config.headers
          : new AxiosHeaders(config.headers)
      headers.set('Authorization', `Token ${token}`)
      config.headers = headers
    }

    if (isMockEnabled()) {
      const adapter = createMockAdapter(config)
      if (adapter) {
        config.adapter = adapter
      }
    }

    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')

      if (router.currentRoute.value.path !== '/') {
        alert('Login expired, please sign in again.')
        router.push('/')
      }
    }

    return Promise.reject(error)
  }
)

export default service
