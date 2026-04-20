<template>
  <view class="page">
    <view v-if="loading" class="center-tip"><text>加载中...</text></view>

    <template v-else>
      <!-- 总体评分概览 -->
      <view class="summary-card">
        <view class="summary-left">
          <text class="big-score">{{ avgRating ?? '-' }}</text>
          <text class="score-label">综合评分</text>
        </view>
        <view class="summary-right">
          <view class="stars-row">
            <text class="stars-display">{{ summaryStars }}</text>
          </view>
          <text class="review-total">共 {{ reviews.length }} 条评价</text>
          <!-- 评分分布 -->
          <view class="dist-row" v-for="n in [5,4,3,2,1]" :key="n">
            <text class="dist-label">{{ n }}星</text>
            <view class="dist-bar-bg">
              <view class="dist-bar-fill" :style="{ width: barWidth(n) }"></view>
            </view>
            <text class="dist-count">{{ countOfRating(n) }}</text>
          </view>
        </view>
      </view>

      <!-- 评价列表 -->
      <view v-if="reviews.length === 0" class="center-tip">
        <text>暂无评价，快来抢先评价吧～</text>
      </view>

      <view v-for="rv in reviews" :key="rv.id" class="review-card">
        <view class="rv-header">
          <view class="avatar">{{ rv.username?.charAt(0) ?? '?' }}</view>
          <view class="rv-meta">
            <text class="rv-username">{{ rv.username }}</text>
            <text class="rv-time">{{ formatTime(rv.createTime) }}</text>
          </view>
          <view class="rv-stars">
            <text
              v-for="i in 5" :key="i"
              class="rv-star"
              :class="i <= rv.rating ? 'filled' : ''"
            >★</text>
          </view>
        </view>
        <text v-if="rv.content" class="rv-content">{{ rv.content }}</text>
        <text v-else class="rv-no-content">该用户没有填写评价内容</text>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMerchantReviews, getMerchantRating } from '@/api/review'

const loading = ref(false)
const reviews = ref([])
const avgRating = ref(null)
const merchantId = ref(null)

onLoad(async (options) => {
  merchantId.value = options?.merchantId
  loading.value = true
  try {
    const [rvRes, ratingRes] = await Promise.all([
      getMerchantReviews(merchantId.value),
      getMerchantRating(merchantId.value)
    ])
    reviews.value = rvRes.data || []
    avgRating.value = ratingRes.data?.avgRating ?? null
  } finally {
    loading.value = false
  }
})

const summaryStars = computed(() => {
  const n = Math.round(avgRating.value || 0)
  return '★'.repeat(n) + '☆'.repeat(5 - n)
})

function countOfRating(n) {
  return reviews.value.filter(r => r.rating === n).length
}

function barWidth(n) {
  if (reviews.value.length === 0) return '0%'
  return (countOfRating(n) / reviews.value.length * 100).toFixed(0) + '%'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 40rpx;
}

.center-tip {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  margin-top: 120rpx;
}

/* 总体概览 */
.summary-card {
  background: #fff;
  margin-bottom: 16rpx;
  padding: 36rpx 32rpx;
  display: flex;
  gap: 32rpx;
  align-items: flex-start;
}
.summary-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 150rpx;
  flex-shrink: 0;
}
.big-score {
  font-size: 80rpx;
  font-weight: bold;
  color: #FF6B35;
  line-height: 1;
}
.score-label {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}
.summary-right {
  flex: 1;
}
.stars-row {
  margin-bottom: 6rpx;
}
.stars-display {
  font-size: 34rpx;
  color: #FFB300;
  letter-spacing: 4rpx;
}
.review-total {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 16rpx;
  display: block;
}

/* 评分分布 */
.dist-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 8rpx;
}
.dist-label {
  font-size: 22rpx;
  color: #666;
  width: 52rpx;
  flex-shrink: 0;
}
.dist-bar-bg {
  flex: 1;
  height: 12rpx;
  background: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
}
.dist-bar-fill {
  height: 100%;
  background: #FFB300;
  border-radius: 6rpx;
  transition: width 0.3s;
}
.dist-count {
  font-size: 22rpx;
  color: #999;
  width: 36rpx;
  text-align: right;
  flex-shrink: 0;
}

/* 单条评价 */
.review-card {
  background: #fff;
  margin-bottom: 16rpx;
  padding: 28rpx 32rpx;
}
.rv-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #FF6B35;
  color: #fff;
  font-size: 30rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.rv-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.rv-username {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}
.rv-time {
  font-size: 22rpx;
  color: #bbb;
}
.rv-stars {
  display: flex;
  gap: 4rpx;
}
.rv-star {
  font-size: 28rpx;
  color: #e0e0e0;
}
.rv-star.filled {
  color: #FFB300;
}
.rv-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.7;
}
.rv-no-content {
  font-size: 26rpx;
  color: #bbb;
  font-style: italic;
}
</style>
