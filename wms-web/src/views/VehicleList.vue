<template>
  <div class="vehicle-list-container">
    <div class="page-header">
      <h1><el-icon :icon="Truck" /> 我的车辆</h1>
    </div>

    <div class="filter-area">
      <!-- 可以添加搜索框等 -->
    </div>

    <el-table :data="vehicleList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="plateNumber" label="车牌号码" min-width="120" />
      <el-table-column prop="brand" label="品牌" min-width="100" />
      <el-table-column prop="model" label="型号" min-width="100" />
      <el-table-column prop="vin" label="车架号" min-width="200" show-overflow-tooltip />
      <el-table-column prop="mileage" label="当前里程(km)" width="120" />
      <el-table-column prop="purchaseDate" label="购买日期" width="120">
        <template #default="scope">
          {{ scope.row.purchaseDate ? new Date(scope.row.purchaseDate).toLocaleDateString() : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button
              type="primary"
              size="small"
              @click="handleViewHistory(scope.row)"
          >
            查看历史
          </el-button>
          <el-button
              type="success"
              size="small"
              @click="handleCreateAppointment(scope.row)"
          >
            预约保养
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="empty-state" v-if="!loading && vehicleList.length === 0">
      <el-empty description="您还没有添加任何车辆">
        <el-button type="primary" @click="goToAddVehicle">去添加</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Truck } from '@element-plus/icons-vue'
import vehicleApi from '@/api/vehicle'

const router = useRouter()
const route = useRoute()
const store = useStore()

const loading = ref(true)
const vehicleList = ref([])

const currentUserId = store.getters['user/userId']

const loadVehicleList = async () => {
  if (!currentUserId) {
    ElMessage.error('无法获取用户信息，请重新登录')
    loading.value = false
    return
  }

  loading.value = true
  try {
    // 调用API获取数据
    const response = await vehicleApi.getVehiclesByOwner(currentUserId)

    // ================ 关键修改点 ================
    // 1. 后端返回的是 { code: 200, msg: '成功', data: [...] } 结构
    // 2. 我们需要将 response.data 赋值给 vehicleList
    if (response.code === 200) {
      vehicleList.value = response.data // <--- 就是这里！
    } else {
      ElMessage.error(response.msg || '获取车辆列表失败')
    }
    // ==========================================

  } catch (error) {
    console.error('加载车辆列表失败:', error)
    ElMessage.error('网络错误或服务器内部错误')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadVehicleList()
})

watch([() => route.query.t], () => {
  loadVehicleList()
})

const handleViewHistory = (row) => {
  router.push(`/vehicle/history/${row.id}`)
}

const handleCreateAppointment = (row) => {
  router.push({
    name: 'AppointmentCreate',
    query: { vehicleId: row.id }
  })
}

const goToAddVehicle = () => {
  router.push('/vehicle/create')
}
</script>

<style scoped>
.vehicle-list-container {
  padding: 20px;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.filter-area {
  margin-bottom: 20px;
}
.empty-state {
  margin-top: 40px;
}
</style>