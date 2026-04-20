<template>
  <view class="page">
    <view v-if="loading" class="center-tip"><text>加载中...</text></view>

    <view v-else-if="order">
      <!-- 状态栏 -->
      <view class="status-banner" :class="statusClass(order.status)">
        <text class="status-text">{{ statusText(order.status) }}</text>
        <text class="status-sub">{{ statusDesc(order.status) }}</text>
      </view>

      <!-- 配送信息 -->
      <view class="section-card">
        <view class="section-title">配送信息</view>
        <view class="info-row">
          <text class="info-label">收货地址</text>
          <text class="info-value">{{ order.deliveryAddress }}</text>
        </view>
        <view class="info-row" v-if="order.remark">
          <text class="info-label">备注</text>
          <text class="info-value">{{ order.remark }}</text>
        </view>
      </view>

      <!-- 商品清单 -->
      <view class="section-card">
        <view class="section-title">商品清单</view>
        <view v-for="item in items" :key="item.id" class="order-item">
          <text class="oi-name">{{ item.dishName }}</text>
          <text class="oi-qty">x{{ item.quantity }}</text>
          <text class="oi-price">¥{{ item.subtotal }}</text>
        </view>
      </view>

      <!-- 费用明细 -->
      <view class="section-card">
        <view class="fee-row">
          <text class="fee-label">商品金额</text>
          <text class="fee-value">¥{{ order.totalAmount }}</text>
        </view>
        <view class="fee-row">
          <text class="fee-label">配送费</text>
          <text class="fee-value">¥{{ order.deliveryFee }}</text>
        </view>
        <view class="fee-row total">
          <text class="fee-label">实付金额</text>
          <text class="fee-value pay">¥{{ order.payAmount }}</text>
        </view>
      </view>

      <!-- 评价区（仅已完成订单） -->
      <view v-if="order.status === 3" class="section-card">
        <view class="section-title">订单评价</view>

        <!-- 已评价：展示评价摘要 -->
        <view v-if="existingReview" class="review-done">
          <view class="rv-stars">
            <text v-for="i in 5" :key="i" class="rv-star" :class="i <= existingReview.rating ? 'filled' : ''">★</text>
            <text class="rv-score-text">{{ LABELS[existingReview.rating] }}</text>
          </view>
          <text v-if="existingReview.content" class="rv-content">{{ existingReview.content }}</text>
          <text class="rv-time">{{ formatTime(existingReview.createTime) }}</text>
        </view>

        <!-- 未评价：跳转写评价页 -->
        <view v-else class="go-review-btn" @click="goWriteReview">
          <text class="go-review-text">✏️ 去写评价</text>
          <text class="go-review-tip">分享您的用餐体验</text>
          <text class="go-review-arrow">›</text>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="section-card">
        <view class="section-title">订单信息</view>
        <view class="info-row">
          <text class="info-label">订单号</text>
          <text class="info-value small">{{ order.orderNo }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">下单时间</text>
          <text class="info-value">{{ formatTime(order.createTime) }}</text>
        </view>
      </view>

      <!-- 取消按钮（仅待确认状态） -->
      <view v-if="order.status === 0" class="cancel-btn" @click="cancel">
        {{ cancelling ? '取消中...' : '取消订单' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getOrderDetail, cancelOrder } from '@/api/order'
import { getOrderReview } from '@/api/review'

const order = ref(null)
const items = ref([])
const loading = ref(false)
const cancelling = ref(false)
const orderId = ref(null)
const existingReview = ref(null)

const LABELS = ['', '非常不满意', '不满意', '一般', '满意', '非常满意']

onLoad((options) => {
  orderId.value = options?.orderId
  load()
})

// 从写评价页返回时刷新评价状态
onShow(() => {
  if (order.value?.status === 3) {
    loadReview()
  }
})

async function load() {
  if (!orderId.value) return
  loading.value = true
  try {
    const res = await getOrderDetail(orderId.value)
    order.value = res.data?.order
    items.value = res.data?.items || []
    if (order.value?.status === 3) {
      await loadReview()
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function loadReview() {
  try {
    const rv = await getOrderReview(orderId.value)
    existingReview.value = rv.data || null
  } catch {
    existingReview.value = null
  }
}

function goWriteReview() {
  const name = encodeURIComponent(order.value?.merchantName || '该商家')
  const no = encodeURIComponent(order.value?.orderNo || '')
  uni.navigateTo({
    url: `/pages/write-review/write-review?orderId=${orderId.value}&merchantName=${name}&orderNo=${no}`
  })
}

async function cancel() {
  if (cancelling.value) return
  uni.showModal({
    title: '确认取消',
    content: '确定要取消这个订单吗？',
    success: async ({ confirm }) => {
      if (!confirm) return
      cancelling.value = true
      try {
        await cancelOrder(orderId.value)
        uni.showToast({ title: '订单已取消' })
        await load()
      } finally {
        cancelling.value = false
      }
    }
  })
}

const STATUS_TEXT = ['待确认', '已确认', '配送中', '已完成', '已取消']
const STATUS_DESC = ['等待商家接单', '商家已接单，准备中', '外卖正在配送中', '订单已完成', '订单已取消']
const STATUS_CLS  = ['pending', 'confirmed', 'delivering', 'completed', 'cancelled']

function statusText(s) { return STATUS_TEXT[s] ?? '未知' }
function statusDesc(s) { return STATUS_DESC[s] ?? '' }
function statusClass(s) { return STATUS_CLS[s] ?? '' }

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 60rpx;
}
.center-tip {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  margin-top: 120rpx;
}

.status-banner {
  padding: 48rpx 32rpx 36rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}
.pending    { background: #FF6B35; }
.confirmed  { background: #1976d2; }
.delivering { background: #388e3c; }
.completed  { background: #9e9e9e; }
.cancelled  { background: #bdbdbd; }
.status-text { font-size: 40rpx; font-weight: bold; color: #fff; }
.status-sub  { font-size: 26rpx; color: rgba(255,255,255,0.85); }

.section-card {
  background: #fff;
  margin: 16rpx 0;
  padding: 24rpx 32rpx;
}
.section-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #FF6B35;
  margin-bottom: 16rpx;
}

.info-row {
  display: flex;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 26rpx; color: #999; width: 140rpx; flex-shrink: 0; }
.info-value { flex: 1; font-size: 26rpx; color: #333; }
.info-value.small { font-size: 22rpx; color: #666; }

.order-item {
  display: flex;
  align-items: center;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}
.order-item:last-child { border-bottom: none; }
.oi-name  { flex: 1; font-size: 28rpx; color: #333; }
.oi-qty   { font-size: 26rpx; color: #999; margin-right: 20rpx; }
.oi-price { font-size: 28rpx; color: #333; font-weight: bold; }

.fee-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}
.fee-row:last-child { border-bottom: none; }
.fee-row.total { padding-top: 20rpx; }
.fee-label { font-size: 26rpx; color: #666; }
.fee-value { font-size: 26rpx; color: #333; }
.fee-value.pay { font-size: 34rpx; font-weight: bold; color: #FF6B35; }

/* 已评价展示 */
.review-done { display: flex; flex-direction: column; gap: 10rpx; }
.rv-stars { display: flex; align-items: center; gap: 6rpx; }
.rv-star { font-size: 34rpx; color: #e0e0e0; }
.rv-star.filled { color: #FFB300; }
.rv-score-text { font-size: 26rpx; color: #FF6B35; margin-left: 8rpx; }
.rv-content { font-size: 28rpx; color: #333; line-height: 1.6; }
.rv-time { font-size: 22rpx; color: #bbb; }

/* 去写评价按钮 */
.go-review-btn {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  gap: 12rpx;
}
.go-review-text { font-size: 30rpx; font-weight: bold; color: #FF6B35; flex: 1; }
.go-review-tip  { font-size: 24rpx; color: #bbb; }
.go-review-arrow { font-size: 36rpx; color: #ccc; }

.cancel-btn {
  margin: 32rpx;
  border: 2rpx solid #e53935;
  color: #e53935;
  text-align: center;
  font-size: 30rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  background: #fff;
}
</style>
