<template>
  <view class="page">
    <!-- 顶部搜索栏 -->
    <view class="header">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索商家"
          confirm-type="search"
          @input="onSearchInput"
          @confirm="doSearch"
        />
        <text v-if="keyword" class="clear-btn" @click="clearSearch">✕</text>
      </view>
    </view>

    <!-- AI助手浮动入口 -->
    <view class="ai-fab" @click="openAiChat">
      <text class="ai-fab-icon">🤖</text>
      <text class="ai-fab-text">AI点餐</text>
    </view>

    <!-- 商家列表 -->
    <scroll-view
      class="list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view v-if="loading && merchants.length === 0" class="center-tip">
        <text>加载中...</text>
      </view>

      <view v-else-if="merchants.length === 0" class="center-tip">
        <text>暂无商家</text>
      </view>

      <view
        v-for="m in merchants"
        :key="m.merchantId"
        class="merchant-card"
        @click="goMerchant(m.id)"
      >
        <view class="card-body">
          <view class="merchant-name">{{ m.name }}</view>
          <view v-if="m.address" class="merchant-addr">📍 {{ m.address }}</view>
          <view v-if="m.description" class="merchant-desc">{{ m.description }}</view>
        </view>
        <text class="arrow">›</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getMerchantList, searchMerchant } from '@/api/merchant'

const auth = useAuthStore()
const merchants = ref([])
const loading = ref(false)
const refreshing = ref(false)
const keyword = ref('')

let searchTimer = null

onMounted(() => {
  if (!auth.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  loadMerchants()
})

async function loadMerchants() {
  loading.value = true
  try {
    const res = await getMerchantList()
    merchants.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

async function doSearch() {
  if (!keyword.value.trim()) {
    loadMerchants()
    return
  }
  loading.value = true
  try {
    const res = await searchMerchant(keyword.value.trim())
    merchants.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(doSearch, 500)
}

function clearSearch() {
  keyword.value = ''
  loadMerchants()
}

async function onRefresh() {
  refreshing.value = true
  try {
    await (keyword.value.trim() ? doSearch() : loadMerchants())
  } finally {
    refreshing.value = false
  }
}

function goMerchant(id) {
  uni.navigateTo({ url: `/pages/merchant/merchant?merchantId=${id}` })
}

function openAiChat() {
  uni.navigateTo({ url: '/pages/ai-chat/ai-chat' })
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.header {
  background: #fff;
  padding: 20rpx 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
}

.search-bar {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 16rpx 24rpx;
  gap: 16rpx;
}

.search-icon { font-size: 28rpx; color: #999; }

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.clear-btn {
  font-size: 24rpx;
  color: #999;
  padding: 4rpx 8rpx;
}

.list {
  flex: 1;
  padding: 20rpx 24rpx;
}

.center-tip {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  margin-top: 120rpx;
}

.merchant-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05);
}

.card-body { flex: 1; }

.merchant-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.merchant-addr {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.merchant-desc {
  font-size: 24rpx;
  color: #999;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.arrow {
  font-size: 40rpx;
  color: #ccc;
  margin-left: 16rpx;
}

.ai-fab {
  position: fixed;
  right: 40rpx;
  bottom: 180rpx;
  z-index: 999;
  background: #1890ff;
  border-radius: 60rpx;
  padding: 18rpx 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(24,144,255,0.4);
}

.ai-fab-icon { font-size: 44rpx; }
.ai-fab-text { font-size: 22rpx; color: #fff; margin-top: 4rpx; }
</style>
