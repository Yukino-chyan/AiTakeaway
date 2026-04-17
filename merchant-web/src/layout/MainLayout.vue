<template>
  <el-container class="layout">
    <el-aside width="200px" class="sidebar">
      <div class="logo">AI外卖 · 商家端</div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/shop">
          <el-icon><Shop /></el-icon>
          <span>店铺信息</span>
        </el-menu-item>
        <el-menu-item index="/dish">
          <el-icon><Food /></el-icon>
          <span>菜品管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="header-title">{{ pageTitle }}</span>
        <el-button type="danger" text @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Shop, Food, List } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => route.path)

const titleMap = { '/shop': '店铺信息', '/dish': '菜品管理', '/order': '订单管理' }
const pageTitle = computed(() => titleMap[route.path] || '')

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }

.sidebar {
  background: #001529;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 15px;
  font-weight: bold;
  border-bottom: 1px solid #ffffff22;
}

.sidebar .el-menu {
  border-right: none;
  background: #001529;
  flex: 1;
  --el-menu-bg-color: #001529;
  --el-menu-text-color: #ffffffa0;
  --el-menu-active-color: #fff;
  --el-menu-hover-bg-color: #ffffff18;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
}

.header-title { font-size: 16px; font-weight: 500; }

.main { background: #f5f5f5; }
</style>
