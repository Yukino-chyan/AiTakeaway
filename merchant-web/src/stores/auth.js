import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    userId: localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null,
    role: localStorage.getItem('role') || null,
  }),

  getters: {
    isLoggedIn: state => !!state.token,
    isMerchant: state => state.role === 'MERCHANT',
  },

  actions: {
    async login(username, password) {
      const res = await loginApi({ username, password })
      const { token, userId, role } = res.data
      this.token = token
      this.userId = userId
      this.role = role
      localStorage.setItem('token', token)
      localStorage.setItem('userId', userId)
      localStorage.setItem('role', role)
    },

    logout() {
      this.token = null
      this.userId = null
      this.role = null
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
    },
  },
})
