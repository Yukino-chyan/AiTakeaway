<template>
  <view class="page">
    <view v-if="loading" class="center-tip"><text>加载中...</text></view>

    <view v-else-if="orders.length === 0" class="empty">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无订单</text>
      <view class="btn-link" @click="uni.switchTab({ url: '/pages/home/home' })">去点餐</view>
    </view>

    <scroll-view v-else scroll-y class="list">
      <view
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        @click="goDetail(order.id)"
      >
        <view class="card-header">
          <text class="order-no">订单号：{{ order.orderNo }}</text>
          <text :class="['status-tag', statusClass(order.status)]">{{ statusText(order.status) }}</text>
        </view>
        <view class="card-body">
          <text class="amount">实付 ¥{{ order.payAmount }}</text>
          <text class="time">{{ formatTime(order.createTime) }}</text>
        </view>
        <view class="card-footer">
          <text class="addr">📍 {{ order.deliveryAddress }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyOrders } from '@/api/order'

const orders = ref([])
const loading = ref(false)

onShow(load)

async function load() {
  loading.value = true
  try {
    const res = await getMyOrders()
    orders.value = res.data || []
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const STATUS = ['待确认', '已确认', '配送中', '已完成', '已取消']
const STATUS_CLS = ['pending', 'confirmed', 'delivering', 'completed', 'cancelled']

function statusText(s) { return STATUS[s] ?? '未知' }
function statusClass(s) { return STATUS_CLS[s] ?? '' }

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/order-detail/order-detail?orderId=${id}` })
}
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; }

.center-tip {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  margin-top: 120rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 200rpx;
  gap: 24rpx;
}
.empty-icon { font-size: 100rpx; }
.empty-text { font-size: 30rpx; color: #999; }
.btn-link {
  background: #FF6B35;
  color: #fff;
  padding: 20rpx 60rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
}

.list { height: 100vh; }

.order-card {
  background: #fff;
  margin: 20rpx 24rpx;
  border-radius: 16rpx;
  padding: 28rpx 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.order-no { font-size: 24rpx; color: #999; }

.status-tag {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}
.pending    { background: #fff3e0; color: #FF6B35; }
.confirmed  { background: #e3f2fd; color: #1976d2; }
.delivering { background: #e8f5e9; color: #388e3c; }
.completed  { background: #f5f5f5; color: #999; }
.cancelled  { background: #fce4ec; color: #c62828; }

.card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

.time { font-size: 24rpx; color: #bbb; }

.card-footer { }
.addr { font-size: 24rpx; color: #999; }
</style>
