<template>
  <view class="page">
    <view v-if="loading" class="center-tip"><text>加载中...</text></view>

    <view v-else>
      <!-- 商家名 -->
      <view class="section-card">
        <text class="merchant-name">{{ merchantName }}</text>
      </view>

      <!-- 商品清单 -->
      <view class="section-card">
        <view class="section-title">商品清单</view>
        <view v-for="item in items" :key="item.id" class="order-item">
          <text class="oi-name">{{ item.dishName }}</text>
          <text class="oi-qty">x{{ item.quantity }}</text>
          <text class="oi-price">¥{{ (Number(item.dishPrice) * item.quantity).toFixed(2) }}</text>
        </view>
      </view>

      <!-- 配送信息 -->
      <view class="section-card">
        <view class="section-title">配送信息</view>
        <view class="info-row">
          <text class="info-label">收货地址</text>
          <input class="info-input" v-model="deliveryAddress" placeholder="请输入收货地址（必填）" />
        </view>
        <view class="info-row">
          <text class="info-label">备注</text>
          <input class="info-input" v-model="remark" placeholder="口味、忌口等（选填）" />
        </view>
      </view>

      <!-- 合计 -->
      <view class="total-row">
        <text class="total-label">合计</text>
        <text class="total-price">¥{{ totalPrice }}</text>
      </view>

      <!-- 提交 -->
      <view
        class="submit-btn"
        :class="{ disabled: submitting }"
        @click="submit"
      >{{ submitting ? '提交中...' : '确认下单' }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCartList, createOrderFromCart } from '@/api/cart'
import { getMerchantDetail } from '@/api/merchant'

const merchantId = ref(null)
const merchantName = ref('')
const items = ref([])
const loading = ref(false)
const submitting = ref(false)
const deliveryAddress = ref('')
const remark = ref('')

onLoad((options) => {
  merchantId.value = options?.merchantId
  load()
})

async function load() {
  if (!merchantId.value) return
  loading.value = true
  try {
    const [cartRes, merchantRes] = await Promise.all([
      getCartList(),
      getMerchantDetail(merchantId.value)
    ])
    items.value = (cartRes.data?.items || []).filter(
      i => String(i.merchantId) === String(merchantId.value)
    )
    merchantName.value = merchantRes.data?.name || ''
  } catch (e) {
    uni.showToast({ title: '加载失败，请检查网络', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const totalPrice = computed(() =>
  items.value.reduce((s, i) => s + Number(i.dishPrice) * i.quantity, 0).toFixed(2)
)

async function submit() {
  if (!deliveryAddress.value.trim()) {
    uni.showToast({ title: '请填写收货地址', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    const res = await createOrderFromCart({
      merchantId: Number(merchantId.value),
      deliveryAddress: deliveryAddress.value.trim(),
      remark: remark.value.trim()
    })
    uni.showToast({ title: '下单成功！' })
    setTimeout(() => {
      uni.redirectTo({ url: `/pages/order-detail/order-detail?orderId=${res.data}` })
    }, 800)
  } finally {
    submitting.value = false
  }
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

.section-card {
  background: #fff;
  margin: 20rpx 0;
  padding: 24rpx 32rpx;
}

.merchant-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.section-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #FF6B35;
  margin-bottom: 16rpx;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}

.order-item:last-child { border-bottom: none; }

.oi-name { flex: 1; font-size: 28rpx; color: #333; }
.oi-qty { font-size: 26rpx; color: #999; margin-right: 20rpx; }
.oi-price { font-size: 28rpx; color: #FF6B35; font-weight: bold; }

.info-row {
  display: flex;
  align-items: center;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-row:last-child { border-bottom: none; }

.info-label {
  font-size: 28rpx;
  color: #333;
  width: 140rpx;
  flex-shrink: 0;
}

.info-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 24rpx 32rpx;
  margin-bottom: 40rpx;
}

.total-label { font-size: 28rpx; color: #666; }

.total-price {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF6B35;
}

.submit-btn {
  margin: 0 32rpx;
  background: #FF6B35;
  color: #fff;
  text-align: center;
  font-size: 32rpx;
  padding: 28rpx;
  border-radius: 16rpx;
}

.submit-btn.disabled { background: #ccc; }
</style>
