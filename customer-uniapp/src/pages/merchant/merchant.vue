<template>
  <view class="page">
    <!-- 商家信息头部 -->
    <view class="merchant-header" v-if="merchant">
      <view class="merchant-name">{{ merchant.name }}</view>
      <view class="merchant-meta">
        <text v-if="merchant.address">📍 {{ merchant.address }}</text>
        <text v-if="merchant.businessHours">🕐 {{ merchant.businessHours }}</text>
        <text v-if="merchant.deliveryFee != null">🛵 配送费 ¥{{ merchant.deliveryFee }}</text>
      </view>
      <view v-if="merchant.description" class="merchant-desc">{{ merchant.description }}</view>
    </view>

    <!-- 菜品列表 -->
    <scroll-view class="dish-list" scroll-y>
      <view v-if="loading" class="center-tip"><text>加载中...</text></view>
      <view v-else-if="dishes.length === 0" class="center-tip"><text>暂无菜品</text></view>

      <template v-for="(group, category) in groupedDishes" :key="category">
        <view class="category-label">{{ category || '其他' }}</view>
        <view
          v-for="dish in group"
          :key="dish.id"
          class="dish-card"
        >
          <view class="dish-info">
            <view class="dish-name">{{ dish.name }}</view>
            <view v-if="dish.description" class="dish-desc">{{ dish.description }}</view>
            <view class="dish-price">¥{{ dish.price }}</view>
          </view>
          <view class="dish-actions">
            <view v-if="getQty(dish.id) > 0" class="qty-control">
              <text class="qty-btn minus" @click="removeDish(dish)">−</text>
              <text class="qty-num">{{ getQty(dish.id) }}</text>
            </view>
            <text class="qty-btn plus" @click="addDish(dish)">+</text>
          </view>
        </view>
      </template>
    </scroll-view>

    <!-- 底部结算栏 -->
    <view class="cart-bar" v-if="totalCount > 0" @click="goCart">
      <view class="cart-badge-wrap">
        <text class="cart-icon">🛒</text>
        <text class="cart-badge">{{ totalCount }}</text>
      </view>
      <text class="cart-total">¥{{ totalPrice }}</text>
      <view class="cart-btn">去结算</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMerchantDetail } from '@/api/merchant'
import { getDishList } from '@/api/dish'
import { getCartList, addToCart, updateCartQuantity } from '@/api/cart'

const merchantId = ref(null)
const merchant = ref(null)
const dishes = ref([])
const cartMap = ref({})  // dishId -> { cartId, quantity }
const loading = ref(false)

onMounted(() => {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  merchantId.value = current?.options?.merchantId
  if (merchantId.value) {
    loading.value = true
    Promise.all([loadMerchant(), loadDishes(), loadCart()]).finally(() => {
      loading.value = false
    })
  }
})

async function loadMerchant() {
  const res = await getMerchantDetail(merchantId.value)
  merchant.value = res.data
}

async function loadDishes() {
  const res = await getDishList(merchantId.value)
  dishes.value = res.data || []
}

async function loadCart() {
  const res = await getCartList()
  const map = {}
  const merchants = res.data?.merchants || []
  const group = merchants.find(m => String(m.merchantId) === String(merchantId.value))
  for (const item of group?.items || []) {
    map[item.dishId] = { cartId: item.cartId, quantity: item.quantity }
  }
  cartMap.value = map
}

const groupedDishes = computed(() => {
  const groups = {}
  for (const dish of dishes.value) {
    const cat = dish.category || '其他'
    if (!groups[cat]) groups[cat] = []
    groups[cat].push(dish)
  }
  return groups
})

const totalCount = computed(() =>
  Object.values(cartMap.value).reduce((sum, v) => sum + v.quantity, 0)
)

const totalPrice = computed(() => {
  let total = 0
  for (const dish of dishes.value) {
    const entry = cartMap.value[dish.id]
    if (entry) total += Number(dish.price) * entry.quantity
  }
  return total.toFixed(2)
})

function getQty(dishId) {
  return cartMap.value[dishId]?.quantity || 0
}

async function addDish(dish) {
  const current = cartMap.value[dish.id]
  if (current) {
    current.quantity++
  } else {
    cartMap.value[dish.id] = { cartId: null, quantity: 1 }
  }
  try {
    await addToCart({ dishId: dish.id, quantity: 1 })
    if (!cartMap.value[dish.id]?.cartId) {
      await loadCart()
    }
  } catch {
    await loadCart()
  }
}

async function removeDish(dish) {
  const current = cartMap.value[dish.id]
  if (!current || current.quantity === 0) return
  const newQty = current.quantity - 1
  if (newQty === 0) {
    delete cartMap.value[dish.id]
  } else {
    current.quantity = newQty
  }
  try {
    await updateCartQuantity({ cartId: current.cartId, quantity: newQty })
  } catch {
    await loadCart()
  }
}

function goCart() {
  uni.navigateTo({ url: `/pages/checkout/checkout?merchantId=${merchantId.value}` })
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.merchant-header {
  background: #fff;
  padding: 30rpx 32rpx 24rpx;
  margin-bottom: 16rpx;
}

.merchant-name {
  font-size: 38rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
}

.merchant-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.merchant-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.dish-list {
  flex: 1;
}

.center-tip {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  margin-top: 120rpx;
}

.category-label {
  padding: 16rpx 32rpx 10rpx;
  font-size: 26rpx;
  font-weight: bold;
  color: #FF6B35;
  background: #f5f5f5;
}

.dish-card {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 24rpx 32rpx;
  margin-bottom: 2rpx;
}

.dish-info { flex: 1; }

.dish-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 6rpx;
}

.dish-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.dish-price {
  font-size: 30rpx;
  color: #FF6B35;
  font-weight: bold;
}

.dish-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.qty-control {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.qty-btn {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: bold;
  line-height: 52rpx;
  text-align: center;
}

.plus {
  background: #FF6B35;
  color: #fff;
}

.minus {
  background: #fff;
  color: #FF6B35;
  border: 2rpx solid #FF6B35;
}

.qty-num {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  min-width: 32rpx;
  text-align: center;
}

.cart-bar {
  height: 100rpx;
  background: #333;
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  flex-shrink: 0;
}

.cart-badge-wrap {
  position: relative;
  margin-right: 24rpx;
}

.cart-icon { font-size: 44rpx; }

.cart-badge {
  position: absolute;
  top: -10rpx;
  right: -16rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 20rpx;
  border-radius: 20rpx;
  padding: 2rpx 10rpx;
  min-width: 30rpx;
  text-align: center;
}

.cart-total {
  flex: 1;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
}

.cart-btn {
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  padding: 18rpx 36rpx;
  border-radius: 40rpx;
}

.cart-bar-placeholder { height: 100rpx; }
</style>
