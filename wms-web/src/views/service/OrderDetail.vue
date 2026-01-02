<!-- src/views/service/OrderDetail.vue -->
<template>
  <div class="order-detail-container">
    <div class="page-header">
      <h1><el-icon :icon="Ticket" /> 工单详情</h1>
      <el-button type="default" icon="ArrowLeft" @click="goBack">返回列表</el-button>
    </div>

    <el-card shadow="hover" class="detail-card" v-loading="loading">
      <!-- 工单基本信息 -->
      <div class="base-info-section">
        <div class="info-row">
          <div class="info-label">工单编号</div>
          <div class="info-value">{{ order?.orderNo }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">车辆信息</div>
          <div class="info-value">
            {{ order?.vehicle?.brand }} {{ order?.vehicle?.model }}（{{ order?.vehicle?.plateNumber }}）
          </div>
        </div>
        <div class="info-row">
          <div class="info-label">车主信息</div>
          <div class="info-value">
            {{ order?.owner?.name }}（{{ order?.owner?.phone }}）
          </div>
        </div>
        <div class="info-row">
          <div class="info-label">当前状态</div>
          <div class="info-value">
            <el-tag :type="statusTagType[order?.status]">
              {{ statusLabel[order?.status] }}
            </el-tag>
          </div>
        </div>
        <div class="info-row">
          <div class="info-label">创建时间</div>
          <div class="info-value">{{ formatDateTime(order?.createTime) }}</div>
        </div>
        <div class="info-row">
          <div class="info-label">问题描述</div>
          <div class="info-value">{{ order?.problemDesc || '暂无' }}</div>
        </div>
      </div>

      <!-- 工单项列表 -->
      <div class="items-section">
        <div class="section-header">
          <h3>工单项</h3>
          <el-button
              type="primary"
              size="small"
              icon="Plus"
              @click="openAddItemDialog"
              :disabled="!canAddItem"
          >
            添加工单项
          </el-button>
        </div>

        <el-table
            v-if="order?.items?.length"
            :data="order.items"
            border
            style="width: 100%"
        >
          <el-table-column prop="itemType" label="类型" width="100">
            <template #default="scope">
              {{ scope.row.itemType === 'part' ? '配件' : '工时' }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="unitPrice" label="单价" width="100" />
          <el-table-column prop="totalPrice" label="总价" width="100" />
        </el-table>
        <div v-else class="empty-items">
          <el-empty description="暂无工单项" />
        </div>
      </div>

      <!-- 金额信息 -->
      <div class="amount-section">
        <div class="amount-item">
          <span>预估金额：</span>
          <span class="amount-value">{{ order?.estimatedAmount || 0 }} 元</span>
        </div>
        <div class="amount-item">
          <span>实际金额：</span>
          <span class="amount-value">{{ order?.actualAmount || 0 }} 元</span>
        </div>
      </div>

      <!-- 操作区 -->
      <div class="action-section">
        <el-button
            type="primary"
            icon="Edit"
            @click="submitQuote"
            :disabled="!canSubmitQuote"
        >
          提交报价
        </el-button>
        <el-button
            type="success"
            icon="Check"
            @click="completeOrder"
            :disabled="!canCompleteOrder"
        >
          完成工单
        </el-button>
      </div>
    </el-card>

    <!-- 添加工单项弹窗 -->
    <el-dialog
        title="添加工单项"
        v-model="addItemDialogVisible"
        width="500px"
    >
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="类型" prop="itemType">
          <el-select v-model="itemForm.itemType" placeholder="请选择类型">
            <el-option label="配件" value="part" />
            <el-option label="工时" value="labor" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="itemForm.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="itemForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="itemForm.unitPrice" :min="0" step="0.01" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addItemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitItem">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Ticket } from '@element-plus/icons-vue'
import repairOrderApi from '@/api/repairOrder'

const router = useRouter()
const route = useRoute()
// ✅ 核心修正：从路由参数 (params) 中获取工单ID，而不是查询参数 (query)
const orderId = route.params.id

const loading = ref(true)
const order = ref({})
const addItemDialogVisible = ref(false)
const itemForm = ref({
  itemType: 'part',
  description: '',
  quantity: 1,
  unitPrice: 0
})

const statusLabel = {
  pending: '待接车',
  inspecting: '检查中',
  quote_pending: '待确认报价',
  repairing: '维修中',
  completed: '已完成',
  delivered: '已交车'
}
const statusTagType = {
  pending: 'info',
  inspecting: 'warning',
  quote_pending: 'primary',
  repairing: 'success',
  completed: 'success',
  delivered: 'success'
}

const loadOrderDetail = async () => {
  loading.value = true
  try {
    const res = await repairOrderApi.getOrderDetail(orderId)
    if (res.code === 200) {
      order.value = res.data.order
      order.value.items = res.data.items
    } else {
      ElMessage.error(res.msg || '获取工单详情失败')
    }
  } catch (err) {
    ElMessage.error('网络错误，请重试')
  } finally {
    loading.value = false
  }
}

const canAddItem = computed(() => {
  const status = order.value.status
  return status === 'pending' || status === 'inspecting'
})

const canSubmitQuote = computed(() => {
  const status = order.value.status
  return status === 'inspecting'
})

const canCompleteOrder = computed(() => {
  const status = order.value.status
  return status === 'repairing'
})

const openAddItemDialog = () => {
  itemForm.value = { itemType: 'part', description: '', quantity: 1, unitPrice: 0 }
  addItemDialogVisible.value = true
}

const submitItem = async () => {
  try {
    const res = await repairOrderApi.addOrderItem(orderId, itemForm.value)
    if (res.code === 200) {
      ElMessage.success('工单项添加成功')
      addItemDialogVisible.value = false
      loadOrderDetail()
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (err) {
    ElMessage.error('网络错误，请重试')
  }
}

const submitQuote = async () => {
  try {
    const calcRes = await repairOrderApi.calculateOrderAmount(orderId)
    if (calcRes.code !== 200) {
      ElMessage.error('计算金额失败')
      return
    }
    const estimatedAmount = calcRes.data

    await ElMessageBox.confirm(
        `确认提交报价（预估金额：${estimatedAmount} 元）？`,
        '提示',
        { type: 'warning' }
    )

    const res = await repairOrderApi.submitQuote(orderId, estimatedAmount, '系统自动计算')
    if (res.code === 200) {
      ElMessage.success('报价提交成功')
      loadOrderDetail()
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (err) {
    if (err.type !== 'cancel') ElMessage.error('操作失败')
  }
}

const completeOrder = async () => {
  try {
    await ElMessageBox.confirm(
        '确认完成此工单？',
        '提示',
        { type: 'warning' }
    )

    const actualAmount = order.value.estimatedAmount
    const res = await repairOrderApi.completeOrder(orderId, actualAmount)
    if (res.code === 200) {
      ElMessage.success('工单已完成')
      loadOrderDetail()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (err) {
    if (err.type !== 'cancel') ElMessage.error('操作失败')
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.order-detail-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.detail-card {
  padding: 20px;
}
.base-info-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px 20px;
  margin-bottom: 20px;
}
.info-row {
  display: flex;
  flex-direction: column;
}
.info-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}
.info-value {
  font-size: 16px;
  font-weight: 500;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.items-section {
  margin-bottom: 20px;
}
.empty-items {
  padding: 30px 0;
  text-align: center;
}
.amount-section {
  display: flex;
  justify-content: flex-end;
  gap: 30px;
  font-size: 16px;
  margin-bottom: 20px;
}
.amount-value {
  font-weight: 600;
  color: #ff4d4f;
}
.action-section {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>