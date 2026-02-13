import { defineStore } from 'pinia'
import { login as apiLogin } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token'),
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isAdmin: (state) => ['ADMIN', 'ROLE_ADMIN'].includes(state.user?.role)
  },
  actions: {
    async login(email, password) {
      const response = await apiLogin(email, password)
      const { token, role, email: userEmail } = response.data
      this.token = token
      this.user = { email: userEmail, role }
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
