<!-- src/views/warehouse/Stock.vue -->
<template>
  <div class="warehouse-stock-container">
    <h1>配件库存管理</h1>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>库存总价值</span></template>
          <div class="stat-value">¥{{ (stats.totalValue || 0).toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>配件种类</span></template>
          <div class="stat-value">{{ stats.totalItems || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>总库存数量</span></template>
          <div class="stat-value">{{ stats.totalQuantity || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :class="{ 'low-stock-warning': stats.lowStockCount > 0 }">
          <template #header><span>低库存预警</span></template>
          <div class="stat-value">{{ stats.lowStockCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="inventoryList" border style="width: 100%" v-loading="loading" row-key="partId">
      <el-table-column prop="part.partNo" label="配件编号" width="120" />
      <el-table-column prop="part.partName" label="配件名称" width="180" show-overflow-tooltip />
      <el-table-column prop="part.spec" label="规格型号" width="180" show-overflow-tooltip />
      <el-table-column prop="stockQuantity" label="当前库存" width="120" />
      <el-table-column prop="safeStock" label="安全库存" width="120" />
      <el-table-column prop="lastInboundTime" label="最后入库时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button type="success" size="small" @click="openStockDialog(scope.row, 'in')">入库</el-button>
          <el-button type="warning" size="small" @click="openStockDialog(scope.row, 'out')">出库</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 入库/出库对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="40%">
      <el-form ref="formRef" :model="form" label-width="100px" :rules="rules">
        <el-form-item label="配件名称">
          <el-input :value="form.partName" disabled></el-input>
        </el-form-item>
        <el-form-item label="操作数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" controls-position="right"></el-input-number>
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入你的名字"></el-input>
        </el-form-item>
        <el-form-item v-if="dialogType === 'out'" label="出库用途" prop="purpose">
          <el-input v-model="form.purpose" placeholder="例如：维修工单RO12345"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleConfirm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import inventoryApi from '@/api/inventory'

const loading = ref(true)
const inventoryList = ref([])
const stats = ref({})
const formRef = ref(null)

const dialogVisible = ref(false)
const dialogType = ref('') // 'in' for stock-in, 'out' for stock-out
const dialogTitle = ref('')
const form = reactive({
  partId: null,
  partName: '',
  quantity: 1,
  operator: '',
  purpose: ''
})

const rules = reactive({
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }],
  purpose: [{ required: true, message: '请输入出库用途', trigger: 'blur' }],
})

const loadInventory = async () => {
  loading.value = true
  try {
    const [resInventory, resStats] = await Promise.all([
      inventoryApi.getAllInventory(),
      inventoryApi.getInventoryStats()
    ])
    if (resInventory.code === 200) {
      inventoryList.value = resInventory.data
    }
    if (resStats.code === 200) {
      stats.value = resStats.data
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取库存数据失败')
  } finally {
    loading.value = false
  }
}

const openStockDialog = (row, type) => {
  dialogType.value = type
  dialogTitle.value = type === 'in' ? '配件入库' : '配件出库'

  form.partId = row.partId
  form.partName = row.part.partName
  form.quantity = 1
  form.purpose = ''
  // 可以从localStorage获取当前用户名
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  form.operator = userInfo.name || '';

  dialogVisible.value = true
}

const handleConfirm = async () => {
  try {
    await formRef.value.validate()

    let res;
    if (dialogType.value === 'in') {
      res = await inventoryApi.stockIn({
        partId: form.partId,
        quantity: form.quantity,
        operator: form.operator
      })
    } else {
      res = await inventoryApi.stockOut({
        partId: form.partId,
        quantity: form.quantity,
        operator: form.operator,
        purpose: form.purpose
      })
    }

    if (res.code === 200) {
      ElMessage.success(dialogType.value === 'in' ? '入库成功' : '出库成功')
      dialogVisible.value = false
      loadInventory() // 刷新数据
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error(error)
    if (error.name !== 'ValidationError') {
      ElMessage.error('网络错误')
    }
  }
}

onMounted(() => {
  loadInventory()
})
</script>

<style scoped>
.warehouse-stock-container {
  padding: 20px;
}
h1 {
  margin-bottom: 20px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
  padding: 10px 0;
}
.el-card.low-stock-warning .stat-value {
  color: #F56C6C;
}
.el-dialog__body {
  padding-top: 0;
}
</style>