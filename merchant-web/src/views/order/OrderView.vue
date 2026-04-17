<template>
  <div class="order-page">
    <!-- 状态 Tab -->
    <el-card class="toolbar">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane
          v-for="tab in tabs"
          :key="tab.value"
          :label="tab.label"
          :name="tab.value"
        />
      </el-tabs>
    </el-card>

    <!-- 订单列表 -->
    <el-card v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="暂无订单" />

      <div v-for="order in list" :key="order.id" class="order-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-meta">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
          </div>
          <el-tag :type="statusTag(order.status).type" size="small">
            {{ statusTag(order.status).label }}
          </el-tag>
        </div>

        <!-- 订单内容 -->
        <div class="order-body">
          <div class="order-info">
            <div><el-icon><Location /></el-icon> {{ order.deliveryAddress }}</div>
            <div v-if="order.remark"><el-icon><ChatDotRound /></el-icon> {{ order.remark }}</div>
          </div>
          <div class="order-amount">
            <div class="amount-detail">商品：¥{{ order.totalAmount }}</div>
            <div class="amount-detail">配送费：¥{{ order.deliveryFee }}</div>
            <div class="amount-total">实付：¥{{ order.payAmount }}</div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="order-footer">
          <el-button size="small" plain @click="openDetail(order.id)">查看详情</el-button>
          <el-button
            v-if="order.status === 0"
            type="primary" size="small"
            :loading="actionLoading === order.id"
            @click="handleAction(order, 'confirm')"
          >确认接单</el-button>
          <el-button
            v-if="order.status === 1"
            type="warning" size="small"
            :loading="actionLoading === order.id"
            @click="handleAction(order, 'deliver')"
          >开始配送</el-button>
          <el-button
            v-if="order.status === 2"
            type="success" size="small"
            :loading="actionLoading === order.id"
            @click="handleAction(order, 'complete')"
          >确认完成</el-button>
        </div>
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="500px">
      <div v-if="detail" class="detail-content">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="订单号">{{ detail.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(detail.order.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ detail.order.deliveryAddress }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.order.remark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusTag(detail.order.status).type" size="small">
              {{ statusTag(detail.order.status).label }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="item-title">菜品明细</div>
        <el-table :data="detail.items" size="small">
          <el-table-column prop="dishName" label="菜品" />
          <el-table-column prop="dishPrice" label="单价" width="80">
            <template #default="{ row }">¥{{ row.dishPrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="60" />
          <el-table-column prop="subtotal" label="小计" width="80">
            <template #default="{ row }">¥{{ row.subtotal }}</template>
          </el-table-column>
        </el-table>

        <div class="detail-amount">
          <span>商品合计：¥{{ detail.order.totalAmount }}</span>
          <span>配送费：¥{{ detail.order.deliveryFee }}</span>
          <span class="total">实付：¥{{ detail.order.payAmount }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, ChatDotRound } from '@element-plus/icons-vue'
import { getMerchantOrders, getOrderDetail, confirmOrder, deliverOrder, completeOrder } from '@/api/order'

const tabs = [
  { label: '全部',   value: 'all' },
  { label: '待确认', value: '0' },
  { label: '已确认', value: '1' },
  { label: '配送中', value: '2' },
  { label: '已完成', value: '3' },
  { label: '已取消', value: '4' },
]

const STATUS_MAP = {
  0: { label: '待确认', type: 'warning' },
  1: { label: '已确认', type: 'primary' },
  2: { label: '配送中', type: '' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'info' },
}

const list = ref([])
const loading = ref(false)
const activeTab = ref('all')
const actionLoading = ref(null)
const detailVisible = ref(false)
const detail = ref(null)

const statusTag = status => STATUS_MAP[status] ?? { label: '未知', type: 'info' }

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

async function loadList(status) {
  loading.value = true
  try {
    const res = await getMerchantOrders(status)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleTabChange(tab) {
  const status = tab === 'all' ? null : Number(tab)
  loadList(status)
}

async function openDetail(id) {
  const res = await getOrderDetail(id)
  detail.value = res.data
  detailVisible.value = true
}

async function handleAction(order, action) {
  actionLoading.value = order.id
  try {
    const actionMap = { confirm: confirmOrder, deliver: deliverOrder, complete: completeOrder }
    const msgMap = { confirm: '接单成功', deliver: '已开始配送', complete: '订单已完成' }
    await actionMap[action](order.id)
    ElMessage.success(msgMap[action])
    // 更新当前订单状态，不重新请求整个列表
    const next = { confirm: 1, deliver: 2, complete: 3 }
    order.status = next[action]
  } finally {
    actionLoading.value = null
  }
}

onMounted(() => loadList(null))
</script>

<style scoped>
.order-page { display: flex; flex-direction: column; gap: 16px; }

.toolbar :deep(.el-tabs__header) { margin: 0; }
.toolbar :deep(.el-tabs__nav-wrap::after) { display: none; }

.order-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.order-card:last-child { margin-bottom: 0; }

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-meta { display: flex; gap: 16px; align-items: center; }
.order-no { font-weight: 500; font-size: 13px; }
.order-time { color: #999; font-size: 12px; }

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 12px;
}
.order-info { color: #555; font-size: 13px; display: flex; flex-direction: column; gap: 4px; }
.order-info .el-icon { vertical-align: middle; margin-right: 4px; }

.order-amount { text-align: right; font-size: 13px; color: #666; }
.amount-detail { margin-bottom: 2px; }
.amount-total { color: #f56c6c; font-weight: bold; font-size: 15px; margin-top: 4px; }

.order-footer { display: flex; justify-content: flex-end; gap: 8px; border-top: 1px solid #f0f0f0; padding-top: 12px; }

.item-title { font-weight: 500; margin: 16px 0 8px; }
.detail-amount {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  margin-top: 12px;
  font-size: 13px;
  color: #666;
}
.detail-amount .total { color: #f56c6c; font-weight: bold; }
</style>
