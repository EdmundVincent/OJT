import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const instance = axios.create({
  baseURL: '/api'
})

instance.interceptors.request.use(config => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

instance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/login'
    } else {
      const errorMsg = error.response?.data?.message || error.response?.data?.error || 'An error occurred'
      ElMessage.error(errorMsg)
    }
    return Promise.reject(error)
  }
)

export default instance
