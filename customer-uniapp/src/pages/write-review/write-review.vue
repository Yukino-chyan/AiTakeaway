<template>
  <view class="page">
    <!-- 商家信息 -->
    <view class="merchant-bar">
      <text class="merchant-name">{{ merchantName }}</text>
      <text class="order-no">订单号：{{ orderNo }}</text>
    </view>

    <!-- 评分区 -->
    <view class="section">
      <view class="section-title">您的评分</view>
      <view class="stars-wrap">
        <view
          v-for="i in 5" :key="i"
          class="star-item"
          @click="selectRating(i)"
        >
          <text class="star-icon" :class="i <= rating ? 'filled' : ''">★</text>
        </view>
      </view>
      <text class="rating-label">{{ ratingLabel }}</text>
    </view>

    <!-- 文字评价 -->
    <view class="section">
      <view class="section-title">写点评 <text class="optional">（选填）</text></view>
      <textarea
        class="review-textarea"
        v-model="content"
        placeholder="说说这家店的菜品口感、服务速度、包装等，帮助其他用户做选择..."
        maxlength="300"
        auto-height
      />
      <text class="char-count">{{ content.length }}/300</text>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-wrap">
      <view
        class="submit-btn"
        :class="submitting ? 'disabled' : ''"
        @click="submit"
      >
        {{ submitting ? '提交中...' : '发布评价' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { submitReview } from '@/api/review'

const orderId = ref(null)
const merchantName = ref('')
const orderNo = ref('')
const rating = ref(5)
const content = ref('')
const submitting = ref(false)

const LABELS = ['', '非常不满意', '不满意', '一般', '满意', '非常满意']
const ratingLabel = computed(() => LABELS[rating.value] || '')

onLoad((options) => {
  orderId.value = options?.orderId
  merchantName.value = decodeURIComponent(options?.merchantName || '该商家')
  orderNo.value = decodeURIComponent(options?.orderNo || '')
})

function selectRating(val) {
  rating.value = val
}

async function submit() {
  if (rating.value < 1) {
    uni.showToast({ title: '请先选择评分', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await submitReview({
      orderId: orderId.value,
      rating: rating.value,
      content: content.value.trim() || null
    })
    uni.showToast({ title: '评价发布成功！', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1200)
  } catch (e) {
    uni.showToast({ title: e?.data?.message || '提交失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.merchant-bar {
  background: #fff;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 16rpx;
}
.merchant-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.order-no {
  font-size: 22rpx;
  color: #999;
}

.section {
  background: #fff;
  padding: 32rpx;
  margin-bottom: 16rpx;
}
.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}
.optional {
  font-size: 24rpx;
  color: #bbb;
  font-weight: normal;
}

.stars-wrap {
  display: flex;
  justify-content: center;
  gap: 24rpx;
  margin-bottom: 16rpx;
}
.star-item {
  padding: 8rpx;
}
.star-icon {
  font-size: 72rpx;
  color: #e0e0e0;
  transition: color 0.15s;
}
.star-icon.filled {
  color: #FFB300;
}
.rating-label {
  display: block;
  text-align: center;
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
  min-height: 40rpx;
}

.review-textarea {
  width: 100%;
  min-height: 200rpx;
  background: #f9f9f9;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  box-sizing: border-box;
}
.char-count {
  display: block;
  text-align: right;
  font-size: 22rpx;
  color: #bbb;
  margin-top: 10rpx;
}

.submit-wrap {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06);
}
.submit-btn {
  background: #FF6B35;
  color: #fff;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  padding: 26rpx;
  border-radius: 16rpx;
}
.submit-btn.disabled {
  background: #ffb39a;
}
</style>
