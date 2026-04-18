<template>
  <view class="container">
    <view class="title">AI外卖</view>
    <view class="subtitle">顾客注册</view>

    <view class="form">
      <input class="input" v-model="form.username" placeholder="用户名（至少3位）" />
      <input class="input" v-model="form.password" placeholder="密码（至少6位）" password />
      <input class="input" v-model="form.phone" placeholder="手机号（选填）" />
      <button class="btn-primary" :disabled="loading" @click="handleRegister">
        {{ loading ? '注册中...' : '注册' }}
      </button>
      <view class="link" @click="uni.navigateBack()">已有账号？返回登录</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { register } from '@/api/auth'

const loading = ref(false)
const form = ref({ username: '', password: '', phone: '' })

async function handleRegister() {
  if (form.value.username.length < 3) {
    uni.showToast({ title: '用户名至少3位', icon: 'none' }); return
  }
  if (form.value.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' }); return
  }
  loading.value = true
  try {
    await register({ ...form.value, role: 'CUSTOMER' })
    uni.showToast({ title: '注册成功，请登录' })
    setTimeout(() => uni.navigateBack(), 1500)
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
