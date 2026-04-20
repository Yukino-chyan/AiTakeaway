<template>
  <view class="page">
    <view v-if="loading" class="center-tip"><text>加载中...</text></view>

    <view v-else-if="groups.length === 0" class="empty">
      <text class="empty-icon">🛒</text>
      <text class="empty-text">暂无待结算商品</text>
      <view class="btn-link" @click="uni.switchTab({ url: '/pages/home/home' })">去点餐</view>
    </view>

    <scroll-view v-else scroll-y class="list">
      <view v-for="group in groups" :key="group.merchantId" class="merchant-group">
        <!-- 商家标题 -->
        <view class="group-header">
          <text class="group-name">{{ group.merchantName }}</text>
        </view>

        <!-- 菜品行 -->
        <view v-for="item in group.items" :key="item.cartId" class="cart-item">
          <view class="item-info">
            <text class="item-name">{{ item.dishName }}</text>
            <text class="item-price">¥{{ item.dishPrice }}</text>
          </view>
          <view class="item-actions">
            <text class="qty-btn minus" @click="decrease(item)">−</text>
            <text class="qty-num">{{ item.quantity }}</text>
            <text class="qty-btn plus" @click="increase(item)">+</text>
            <text class="del-btn" @click="remove(item)">🗑</text>
          </view>
        </view>

        <!-- 小计 + 结算 -->
        <view class="group-footer">
          <text class="subtotal">合计 <text class="subtotal-price">¥{{ groupTotal(group) }}</text></text>
          <view class="checkout-btn" @click="goCheckout(group.merchantId)">去结算</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCartList, updateCartQuantity, removeCartItem } from '@/api/cart'

const groups = ref([])
const loading = ref(false)

onShow(load)

async function load() {
  loading.value = true
  try {
    const res = await getCartList()
    // 后端返回 { merchants: [{merchantId, merchantName, items, merchantTotal}], totalAmount }
    groups.value = res.data?.merchants || []
  } finally {
    loading.value = false
  }
}

function groupTotal(group) {
  return group.items.reduce((s, i) => s + Number(i.dishPrice) * i.quantity, 0).toFixed(2)
}

async function increase(item) {
  item.quantity++
  try {
    await updateCartQuantity({ cartId: item.cartId, quantity: item.quantity })
  } catch {
    await load()
  }
}

async function decrease(item) {
  if (item.quantity <= 1) { remove(item); return }
  item.quantity--
  try {
    await updateCartQuantity({ cartId: item.cartId, quantity: item.quantity })
  } catch {
    await load()
  }
}

async function remove(item) {
  for (const group of groups.value) {
    group.items = group.items.filter(i => i.cartId !== item.cartId)
  }
  groups.value = groups.value.filter(g => g.items.length > 0)
  try {
    await removeCartItem(item.cartId)
  } catch {
    await load()
  }
}

function goCheckout(merchantId) {
  uni.navigateTo({ url: `/pages/checkout/checkout?merchantId=${merchantId}` })
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
  margin-top: 16rpx;
}

.list { height: 100vh; }

.merchant-group {
  background: #fff;
  margin: 20rpx 0;
}

.group-header {
  padding: 24rpx 32rpx 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.group-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20rpx 32rpx;
  border-bottom: 1rpx solid #f8f8f8;
}

.item-info { flex: 1; }

.item-name { font-size: 28rpx; color: #333; display: block; margin-bottom: 6rpx; }
.item-price { font-size: 26rpx; color: #FF6B35; font-weight: bold; }

.item-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.qty-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  font-size: 34rpx;
  font-weight: bold;
  line-height: 48rpx;
  text-align: center;
}

.plus { background: #FF6B35; color: #fff; }
.minus { background: #fff; color: #FF6B35; border: 2rpx solid #FF6B35; }

.qty-num {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  min-width: 30rpx;
  text-align: center;
}

.del-btn { font-size: 34rpx; margin-left: 8rpx; }

.group-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
}

.subtotal { font-size: 26rpx; color: #666; }
.subtotal-price { color: #FF6B35; font-weight: bold; font-size: 30rpx; }

.checkout-btn {
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  padding: 16rpx 40rpx;
  border-radius: 40rpx;
}
</style>
