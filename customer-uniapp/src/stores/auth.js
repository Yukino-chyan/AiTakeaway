import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: uni.getStorageSync('token') || null,
    userId: uni.getStorageSync('userId') || null,
    role: uni.getStorageSync('role') || null,
  }),

  getters: {
    isLoggedIn: state => !!state.token,
    isCustomer: state => state.role === 'CUSTOMER',
  },

  actions: {
    async login(username, password) {
      const res = await loginApi({ username, password })
      const { token, userId, role } = res.data
      this.token = token
      this.userId = userId
      this.role = role
      uni.setStorageSync('token', token)
      uni.setStorageSync('userId', String(userId))
      uni.setStorageSync('role', role)
    },

    logout() {
      this.token = null
      this.userId = null
      this.role = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('userId')
      uni.removeStorageSync('role')
    },
  },
})
