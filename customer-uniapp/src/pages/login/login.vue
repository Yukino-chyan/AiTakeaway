<template>
  <view class="container">
    <view class="title">AI外卖</view>
    <view class="subtitle">顾客登录</view>

    <view class="form">
      <input class="input" v-model="form.username" placeholder="用户名" />
      <input class="input" v-model="form.password" placeholder="密码" password />
      <button class="btn-primary" :disabled="loading" @click="handleLogin">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <view class="link" @click="$uni.navigateTo({ url: '/pages/register/register' })">
        还没有账号？立即注册
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const form = ref({ username: '', password: '' })

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    uni.showToast({ title: '请填写用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await auth.login(form.value.username, form.value.password)
    if (!auth.isCustomer) {
      auth.logout()
      uni.showToast({ title: '该账号不是顾客账号', icon: 'none' })
      return
    }
    uni.switchTab({ url: '/pages/home/home' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.container { padding: 60rpx 40rpx; }
.title { font-size: 56rpx; font-weight: bold; color: #FF6B35; text-align: center; margin-bottom: 16rpx; }
.subtitle { font-size: 32rpx; color: #999; text-align: center; margin-bottom: 80rpx; }
.form { display: flex; flex-direction: column; gap: 24rpx; }
.input { border: 1rpx solid #ddd; border-radius: 12rpx; padding: 24rpx; font-size: 28rpx; background: #fff; }
.btn-primary { background: #FF6B35; color: #fff; border-radius: 12rpx; padding: 28rpx; font-size: 32rpx; border: none; }
.link { text-align: center; color: #FF6B35; font-size: 28rpx; margin-top: 16rpx; }
</style>
