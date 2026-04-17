<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header>
        <span class="card-title">AI外卖 · 商家注册</span>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码"
            :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（选填）" :prefix-icon="Phone" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        已有账号？<router-link to="/login">返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()

const formRef = ref()
const loading = ref(false)
const form = ref({ username: '', password: '', phone: '' })

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register({ ...form.value, role: 'MERCHANT' })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.auth-card { width: 380px; }
.card-title { font-size: 18px; font-weight: bold; }
.auth-footer { text-align: center; margin-top: 8px; font-size: 13px; color: #999; }
.auth-footer a { color: #409eff; text-decoration: none; }
</style>
