<template>
  <div class="vehicle-history-container">
    <div class="header">
      <el-button icon="el-icon-arrow-left" @click="goBack">返回</el-button>
      <h2>车辆维修历史 - {{ vehicleInfo.plateNumber || '' }}</h2>
    </div>

    <el-card>
      <div class="vehicle-info">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="车牌号">{{ vehicleInfo.plateNumber }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ vehicleInfo.brand }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ vehicleInfo.model }}</el-descriptions-item>
          <el-descriptions-item label="颜色">{{ vehicleInfo.color }}</el-descriptions-item>
          <el-descriptions-item label="车架号">{{ vehicleInfo.vin }}</el-descriptions-item>
          <el-descriptions-item label="当前里程">{{ vehicleInfo.mileage }} km</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider>维修记录</el-divider>

      <el-table :data="historyList" v-loading="loading">
        <el-table-column prop="id" label="工单号" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="维修描述" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.totalAmount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
                type="text"
                @click="viewWorkOrderDetail(row.id)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVehicleDetail, getVehicleRepairHistory } from '@/api/vehicle'

const route = useRoute()
const router = useRouter()
const vehicleId = route.params.id
const loading = ref(false)
const vehicleInfo = ref({})
const historyList = ref([])

// 获取车辆信息和维修历史
const fetchData = async () => {
  try {
    loading.value = true

    // 获取车辆详情
    const detailRes = await getVehicleDetail(vehicleId)
    if (detailRes.code === 200) {
      vehicleInfo.value = detailRes.data
    }

    // 获取维修历史
    const historyRes = await getVehicleRepairHistory(vehicleId)
    if (historyRes.code === 200) {
      historyList.value = historyRes.data || []
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.go(-1)
}

// 查看工单详情
const viewWorkOrderDetail = (orderId) => {
  // 这里先跳转到占位页，后面实现工单详情页后再修改
  router.push(`/placeholder/workorder/${orderId}`)
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'info',     // 待接车
    1: 'warning',  // 检查中
    2: '',         // 待确认报价
    3: 'primary',  // 维修中
    4: 'success',  // 已完成
    5: 'success'   // 已交车
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '待接车',
    1: '检查中',
    2: '待确认报价',
    3: '维修中',
    4: '已完成',
    5: '已交车'
  }
  return textMap[status] || '未知状态'
}

onMounted(() => {
  if (vehicleId) {
    fetchData()
  }
})
</script>

<style scoped>
.vehicle-history-container {
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.vehicle-info {
  margin-bottom: 20px;
}
</style>