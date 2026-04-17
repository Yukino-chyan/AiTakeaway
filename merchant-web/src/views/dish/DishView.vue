<template>
  <div class="dish-page">
    <!-- 顶部操作栏 -->
    <el-card class="toolbar">
      <div class="toolbar-inner">
        <div class="filters">
          <el-input
            v-model="searchText"
            placeholder="搜索菜品名称"
            clearable
            style="width: 220px"
            :prefix-icon="Search"
          />
          <el-select v-model="filterCategory" placeholder="全部分类" clearable style="width: 140px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
        </div>
        <el-button type="primary" :icon="Plus" @click="openDialog(null)">新增菜品</el-button>
      </div>
    </el-card>

    <!-- 菜品表格 -->
    <el-card>
      <el-table :data="filteredList" v-loading="loading" stripe>
        <el-table-column label="菜品图片" width="90">
          <template #default="{ row }">
            <el-avatar shape="square" :size="50" :src="row.image" :icon="Picture" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜品名称" min-width="120" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.category || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-color="#67c23a"
              @change="val => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该菜品？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜品' : '新增菜品'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1"
            style="width:100%" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="如：主食、饮料、小吃" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="菜品描述（选填）" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.image" placeholder="图片链接（选填）" />
          <el-avatar v-if="form.image" shape="square" :size="60"
            :src="form.image" style="margin-top:8px" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存' : '新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Picture } from '@element-plus/icons-vue'
import { getMyDishList, createDish, updateDish, updateDishStatus, deleteDish } from '@/api/dish'

const list = ref([])
const loading = ref(false)
const searchText = ref('')
const filterCategory = ref('')
const filterStatus = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()

const form = ref({
  id: null, name: '', price: null, category: '', description: '', image: '',
})

const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

// 从列表中提取所有分类（去重）
const categories = computed(() => {
  const cats = list.value.map(d => d.category).filter(Boolean)
  return [...new Set(cats)]
})

const filteredList = computed(() => {
  return list.value.filter(d => {
    const matchName = !searchText.value || d.name.includes(searchText.value)
    const matchCat = !filterCategory.value || d.category === filterCategory.value
    const matchStatus = filterStatus.value === null || filterStatus.value === undefined
      ? true : d.status === filterStatus.value
    return matchName && matchCat && matchStatus
  })
})

async function loadList() {
  loading.value = true
  try {
    const res = await getMyDishList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(data) {
  isEdit.value = !!data
  form.value = data
    ? { id: data.id, name: data.name, price: data.price, category: data.category,
        description: data.description, image: data.image }
    : { id: null, name: '', price: null, category: '', description: '', image: '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateDish(form.value)
      ElMessage.success('保存成功')
    } else {
      await createDish(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadList()
  } finally {
    submitLoading.value = false
  }
}

async function handleStatusChange(row, val) {
  try {
    await updateDishStatus(row.id, val ? 1 : 0)
    row.status = val ? 1 : 0
    ElMessage.success(val ? '已上架' : '已下架')
  } catch {
    // 失败不更新状态，el-switch 自动还原
  }
}

async function handleDelete(id) {
  try {
    await deleteDish(id)
    ElMessage.success('删除成功')
    await loadList()
  } catch {}
}

onMounted(loadList)
</script>

<style scoped>
.dish-page { display: flex; flex-direction: column; gap: 16px; }

.toolbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filters { display: flex; gap: 10px; }

.price { color: #f56c6c; font-weight: bold; }
</style>
