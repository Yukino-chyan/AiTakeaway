<template>
  <div class="shop-page">

    <!-- 未创建店铺 -->
    <el-card v-if="!shop" class="empty-card">
      <el-empty description="您还没有创建店铺">
        <el-button type="primary" @click="openDialog(null)">立即创建</el-button>
      </el-empty>
    </el-card>

    <!-- 已有店铺 -->
    <template v-else>
      <!-- 顶部状态栏 -->
      <el-card class="status-card">
        <div class="status-row">
          <div class="shop-name-row">
            <el-avatar :size="48" :src="shop.avatar" icon="Shop" />
            <div class="shop-name-info">
              <span class="shop-name">{{ shop.name }}</span>
              <el-tag :type="shop.status === 1 ? 'success' : 'info'" size="small">
                {{ shop.status === 1 ? '营业中' : '休息中' }}
              </el-tag>
            </div>
          </div>
          <div class="status-actions">
            <el-switch
              v-model="isOpen"
              active-text="营业中"
              inactive-text="休息中"
              :loading="statusLoading"
              @change="handleStatusChange"
            />
            <el-button type="primary" plain @click="openDialog(shop)">编辑信息</el-button>
          </div>
        </div>
      </el-card>

      <!-- 详情卡片 -->
      <el-card class="detail-card">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="店铺名称">{{ shop.name }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ shop.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="店铺地址" :span="2">{{ shop.address || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="营业时间">{{ shop.businessHours || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="配送费">¥{{ shop.deliveryFee }}</el-descriptions-item>
          <el-descriptions-item label="配送范围">{{ shop.deliveryRange }} km</el-descriptions-item>
          <el-descriptions-item label="店铺描述" :span="2">{{ shop.description || '未设置' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>

    <!-- 创建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑店铺信息' : '创建店铺'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="店铺名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="店铺地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入店铺地址" />
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="form.businessHours" placeholder="如：09:00-21:00" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="配送费" prop="deliveryFee">
              <el-input-number v-model="form.deliveryFee" :min="0" :precision="2" :step="0.5"
                style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配送范围" prop="deliveryRange">
              <el-input-number v-model="form.deliveryRange" :min="1" :max="50"
                style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="店铺描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简单介绍一下您的店铺" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="form.avatar" placeholder="图片链接（选填）" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyShop, createShop, updateShop, updateStatus } from '@/api/shop'

const shop = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const statusLoading = ref(false)
const formRef = ref()

const form = ref({
  id: null,
  name: '',
  phone: '',
  address: '',
  businessHours: '09:00-21:00',
  deliveryFee: 3.00,
  deliveryRange: 3,
  description: '',
  avatar: '',
})

const rules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入店铺地址', trigger: 'blur' }],
  deliveryFee: [{ required: true, message: '请输入配送费', trigger: 'blur' }],
  deliveryRange: [{ required: true, message: '请输入配送范围', trigger: 'blur' }],
}

const isOpen = computed({
  get: () => shop.value?.status === 1,
  set: () => {},
})

async function loadShop() {
  try {
    const res = await getMyShop()
    shop.value = res.data
  } catch {
    shop.value = null
  }
}

function openDialog(data) {
  isEdit.value = !!data
  if (data) {
    Object.assign(form.value, data)
  } else {
    form.value = {
      id: null, name: '', phone: '', address: '',
      businessHours: '09:00-21:00', deliveryFee: 3.00,
      deliveryRange: 3, description: '', avatar: '',
    }
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateShop(form.value)
      ElMessage.success('保存成功')
    } else {
      await createShop(form.value)
      ElMessage.success('店铺创建成功')
    }
    dialogVisible.value = false
    await loadShop()
  } finally {
    submitLoading.value = false
  }
}

async function handleStatusChange(val) {
  statusLoading.value = true
  try {
    await updateStatus(shop.value.id, val ? 1 : 0)
    shop.value.status = val ? 1 : 0
    ElMessage.success(val ? '已开启营业' : '已切换为休息中')
  } catch {
    // 失败时还原 switch
    shop.value.status = val ? 0 : 1
  } finally {
    statusLoading.value = false
  }
}

onMounted(loadShop)
</script>

<style scoped>
.shop-page { display: flex; flex-direction: column; gap: 16px; }

.status-card .status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.shop-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-name-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.shop-name { font-size: 18px; font-weight: bold; }

.status-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>
