<template>
  <div class="appointment-list-container">
    <!-- 页面标题和操作 -->
    <div class="page-header-actions">
      <div class="page-header">
        <h1><el-icon><Calendar /></el-icon> 我的预约</h1>
        <p class="page-desc">查看和管理您的所有预约记录</p>
      </div>
      <div class="page-actions">
        <el-button
            type="primary"
            icon="Plus"
            @click="goToCreateAppointment"
            :disabled="!hasVehicles"
        >
          新预约
        </el-button>
      </div>
    </div>

    <!-- 如果没有车辆，显示引导 -->
    <div v-if="!hasVehicles" class="no-vehicles-guide">
      <el-empty description="您还没有添加任何车辆，无法进行预约。">
        <el-button type="primary" @click="goToAddVehicle">去添加车辆</el-button>
      </el-empty>
    </div>

    <!-- 如果有车辆，显示预约列表 -->
    <div v-else>
      <!-- 搜索和筛选 -->
      <div class="filter-container">
        <el-form :inline="true" :model="queryParams" class="filter-form">
          <el-form-item label="预约号">
            <el-input
                v-model="queryParams.appointmentNo"
                placeholder="请输入预约号"
                clearable
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="车牌号码">
            <el-select
                v-model="queryParams.vehicleId"
                placeholder="请选择车辆"
                clearable
                style="width: 180px"
            >
              <el-option
                  v-for="vehicle in vehicleList"
                  :key="vehicle.id"
                  :label="vehicle.plateNumber"
                  :value="vehicle.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="预约状态">
            <el-select
                v-model="queryParams.status"
                placeholder="请选择状态"
                clearable
                style="width: 180px"
            >
              <el-option label="待确认" value="pending" />
              <el-option label="已确认" value="confirmed" />
              <el-option label="已完成" value="completed" />
              <el-option label="已取消" value="canceled" />
            </el-select>
          </el-form-item>
          <el-form-item label="预约时间">
            <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 250px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格 -->
      <div class="table-container">
        <el-table
            v-loading="loading"
            :data="appointmentList"
            row-key="id"
            border
            style="width: 100%"
        >
          <el-table-column label="预约号" prop="appointmentNo" :show-overflow-tooltip="true" />
          <el-table-column label="车牌号码" prop="vehicle.plateNumber" :show-overflow-tooltip="true" />
          <el-table-column label="车辆型号" prop="vehicle.brand" :show-overflow-tooltip="true">
            <template #default="scope">
              {{ scope.row.vehicle.brand }} {{ scope.row.vehicle.model }}
            </template>
          </el-table-column>
          <el-table-column label="服务类型" prop="serviceType" :show-overflow-tooltip="true" />
          <el-table-column label="预约时间" prop="startTime" width="240">
            <template #default="scope">
              <div>{{ formatDate(scope.row.startTime) }}</div>
              <div class="text-gray-500 text-sm">{{ formatTime(scope.row.startTime) }} - {{ formatTime(scope.row.endTime) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="120">
            <template #default="scope">
              <el-tag :type="statusTagType[scope.row.status]">{{ statusLabel[scope.row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="服务顾问" prop="serviceAdvisor.name" :show-overflow-tooltip="true" />
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button
                  size="small"
                  type="success"
                  icon="Edit"
                  @click="viewAppointment(scope.row)"
              >
                详情
              </el-button>
              <el-button
                  v-if="canCancel(scope.row)"
                  size="small"
                  type="warning"
                  icon="Delete"
                  @click="cancelAppointment(scope.row)"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
              v-model:current-page="queryParams.pageNum"
              v-model:page-size="queryParams.pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              background
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { Calendar } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import appointmentApi from '@/api/appointment'
import vehicleApi from '@/api/vehicle'

const router = useRouter()
const store = useStore()
const loading = ref(false)

const vehicleList = ref([])
const hasVehicles = computed(() => vehicleList.value.length > 0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  ownerId: store.getters['user/userId'],
  appointmentNo: '',
  vehicleId: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined
})

const dateRange = ref([])

const appointmentList = ref([])
const total = ref(0)

const statusLabel = {
  pending: '待确认',
  confirmed: '已确认',
  completed: '已完成',
  canceled: '已取消'
}

const statusTagType = {
  pending: 'info',
  confirmed: 'success',
  completed: 'success',
  canceled: 'danger'
}

// ✅ 修改：加载数据的核心方法
const loadData = async () => {
  loading.value = true
  try {
    // 1. 先获取车辆列表
    const vehicleRes = await vehicleApi.getVehiclesByOwner(queryParams.value.ownerId)
    if (vehicleRes.code === 200) {
      vehicleList.value = vehicleRes.data || []
    }

    // 2. 如果有车辆，再获取预约列表
    if (hasVehicles.value) {
      // ✅ 修改：API调用参数错误，应该只传递 ownerId，而不是整个 queryParams 对象
      const response = await appointmentApi.getAppointmentsByOwner(queryParams.value.ownerId)

      if (response.code === 200) {
        appointmentList.value = response.data || []

        // ✅ 修改：你的后端目前返回所有数据，total 应该是数据的总长度
        total.value = response.data.length || 0
      }
    } else {
      appointmentList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const handleSearch = () => {
  queryParams.value.pageNum = 1
  loadData()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    ownerId: store.getters['user/userId'],
    appointmentNo: '',
    vehicleId: undefined,
    status: undefined
  }
  dateRange.value = []
  loadData()
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  queryParams.value.pageNum = 1
  loadData()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  loadData()
}

const viewAppointment = (row) => {
  router.push({ name: 'AppointmentDetail', query: { id: row.id } })
}

const goToCreateAppointment = () => {
  router.push('/appointment/create')
}

const goToAddVehicle = () => {
  router.push('/vehicle/create')
}

const cancelAppointment = async (row) => {
  try {
    await ElMessageBox.confirm(
        '确定要取消该预约吗？',
        '温馨提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
    const response = await appointmentApi.cancelAppointment(row.id, store.getters['user/userId'])
    if (response.code === 200) {
      ElMessage.success('取消成功')
      loadData()
    } else {
      ElMessage.error(response.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const canCancel = (row) => {
  if (row.status !== 'pending' && row.status !== 'confirmed') {
    return false
  }
  const startTime = new Date(row.startTime)
  const twoHoursLater = new Date(Date.now() + 2 * 60 * 60 * 1000)
  return startTime > twoHoursLater
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>
<style scoped lang="scss">
.appointment-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

// 页面标题区域
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;

  .header-content {
    h1 {
      color: #303133;
      font-size: 28px;
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .page-desc {
      color: #909399;
      font-size: 16px;
      line-height: 1.6;
    }
  }
  .header-actions {
    display: flex;
    gap: 10px;
  }
}

// 筛选区域
.filter-section {
  margin-bottom: 30px;

  .filter-card {
    border-radius: 8px;
  }
  .filter-content {
    display: flex;
    flex-wrap: wrap;
    gap: 30px;
    align-items: center;
  }

  .filter-group {
    display: flex;
    align-items: center;
    gap: 10px;

    .filter-label {
      color: #606266;
      font-size: 14px;
      white-space: nowrap;
    }
  }
}

// 统计卡片
.stats-section {
  margin-bottom: 30px;

  .stat-card {
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-5px);
    }
  }

  .stat-content {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: white;
  }

  .stat-info {
    flex: 1;

    .stat-count {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 5px;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }

  // 不同统计卡片的颜色
  .stat-total .stat-icon {
    background-color: #409eff;
  }

  .stat-pending .stat-icon {
    background-color: #e6a23c;
  }

  .stat-confirmed .stat-icon {
    background-color: #67c23a;
  }

  .stat-canceled .stat-icon {
    background-color: #909399;
  }
}

// 列表卡片
.list-card {
  border-radius: 8px;

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;

    h3 {
      margin: 0;
      color: #303133;
    }

    .list-summary {
      color: #909399;
      font-size: 14px;
    }
  }
}

// 加载状态
.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;

  .loading-icon {
    font-size: 40px;
    color: #409eff;
    margin-bottom: 20px;
    animation: rotate 2s linear infinite;
  }

  span {
    color: #606266;
    font-size: 16px;
  }
}

// 空状态
.empty-content {
  padding: 60px 0;
}

// 预约项目
.appointment-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.appointment-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
  background: white;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  // 不同状态的边框颜色
  &.status-pending {
    border-left: 4px solid #e6a23c;
  }

  &.status-confirmed {
    border-left: 4px solid #67c23a;
  }

  &.status-canceled {
    border-left: 4px solid #909399;
  }

  &.status-completed {
    border-left: 4px solid #409eff;
  }
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;

  .item-title {
    display: flex;
    align-items: center;
    gap: 15px;

    .appointment-no {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

  .item-actions {
    display: flex;
    gap: 10px;
  }
}

.item-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-bottom: 20px;
}

.item-section {
  h4 {
    font-size: 16px;
    color: #303133;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.info-grid {
  .info-item {
    margin-bottom: 8px;
    display: flex;

    .label {
      color: #909399;
      min-width: 80px;
    }

    .value {
      color: #303133;
      font-weight: 500;
      flex: 1;
    }
  }
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  color: #909399;
  font-size: 13px;

  .create-time,
  .update-time {
    display: flex;
    align-items: center;
    gap: 5px;
  }
}

// 分页
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

// 详情弹窗
.appointment-detail {
  .detail-section {
    margin-bottom: 30px;

    h3 {
      font-size: 18px;
      color: #303133;
      margin-bottom: 15px;
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }

  .vehicle-info {
    display: flex;
    align-items: flex-start;
    gap: 20px;

    .vehicle-avatar {
      flex-shrink: 0;
    }

    .vehicle-details {
      flex: 1;

      .detail-item {
        margin-bottom: 8px;
        display: flex;

        .label {
          color: #909399;
          min-width: 80px;
        }

        .value {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }

  .detail-actions {
    display: flex;
    justify-content: flex-end;
    gap: 15px;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
  }
}

// 旋转动画
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .appointment-list-container {
    padding: 15px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;

    .header-actions {
      justify-content: flex-start;
    }
  }

  .filter-content {
    flex-direction: column;
    align-items: stretch !important;
    gap: 20px !important;
  }

  .filter-group {
    flex-direction: column;
    align-items: flex-start !important;

    .filter-label {
      margin-bottom: 5px;
    }
  }

  .item-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .item-content {
    grid-template-columns: 1fr;
  }

  .item-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .vehicle-info {
    flex-direction: column;
  }
}
</style>