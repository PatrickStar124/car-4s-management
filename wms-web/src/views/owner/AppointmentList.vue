<template>
  <div class="appointment-list-container">
    <!-- 页面标题和操作 -->
    <div class="page-header">
      <div class="header-content">
        <h1><el-icon><List /></el-icon> 我的预约</h1>
        <p class="page-desc">查看和管理您的所有维修保养预约记录</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="goToCreateAppointment">
          <el-icon><Plus /></el-icon> 新建预约
        </el-button>
        <el-button @click="refreshList">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选和搜索区域 -->
    <div class="filter-section">
      <el-card shadow="never" class="filter-card">
        <div class="filter-content">
          <!-- 状态筛选 -->
          <div class="filter-group">
            <span class="filter-label">状态筛选：</span>
            <el-radio-group v-model="filter.status" @change="handleFilterChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button v-for="status in statusOptions"
                               :key="status.value"
                               :label="status.value">
                {{ status.label }}
              </el-radio-button>
            </el-radio-group>
          </div>

          <!-- 时间筛选 -->
          <div class="filter-group">
            <span class="filter-label">时间范围：</span>
            <el-date-picker
                v-model="filter.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :shortcuts="dateShortcuts"
                @change="handleFilterChange"
                style="width: 300px"
            />
          </div>

          <!-- 搜索框 -->
          <div class="filter-group">
            <el-input
                v-model="filter.search"
                placeholder="搜索预约号、车牌号、服务类型"
                clearable
                @clear="handleFilterChange"
                @keyup.enter="handleFilterChange"
                style="width: 300px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section" v-if="appointmentStats">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" v-for="stat in appointmentStats" :key="stat.type">
          <el-card shadow="hover" class="stat-card" :class="`stat-${stat.type}`">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-count">{{ stat.count }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 预约列表 -->
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="list-header">
          <h3>预约记录</h3>
          <div class="list-summary">
            <span>共 {{ pagination.total }} 条记录</span>
            <span v-if="filter.status !== 'all'">（筛选：{{ getStatusLabel(filter.status) }}）</span>
          </div>
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-content">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <!-- 空状态 -->
      <div v-else-if="appointmentList.length === 0" class="empty-content">
        <el-empty description="暂无预约记录">
          <el-button type="primary" @click="goToCreateAppointment">立即预约</el-button>
        </el-empty>
      </div>

      <!-- 预约列表表格 -->
      <div v-else class="appointment-table">
        <div class="appointment-items">
          <div
              v-for="appointment in appointmentList"
              :key="appointment.id"
              class="appointment-item"
              :class="`status-${appointment.status}`"
          >
            <div class="item-header">
              <div class="item-title">
                <span class="appointment-no">{{ appointment.appointmentNo }}</span>
                <el-tag
                    size="small"
                    :type="getStatusTagType(appointment.status)"
                    effect="light"
                >
                  {{ getStatusLabel(appointment.status) }}
                </el-tag>
              </div>
              <div class="item-actions">
                <el-button
                    v-if="canCancel(appointment)"
                    type="danger"
                    size="small"
                    plain
                    @click="handleCancel(appointment)"
                >
                  取消预约
                </el-button>
                <el-button
                    type="primary"
                    size="small"
                    plain
                    @click="showDetail(appointment)"
                >
                  查看详情
                </el-button>
              </div>
            </div>

            <div class="item-content">
              <!-- 车辆信息 -->
              <div class="item-section">
                <h4><el-icon><Truck /></el-icon> 车辆信息</h4>
                <div class="info-grid">
                  <div class="info-item">
                    <span class="label">车牌号：</span>
                    <span class="value">{{ appointment.vehicle?.plateNumber || '未知' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">车型：</span>
                    <span class="value">{{ appointment.vehicle?.brand }} {{ appointment.vehicle?.model }}</span>
                  </div>
                </div>
              </div>

              <!-- 服务信息 -->
              <div class="item-section">
                <h4><el-icon><Setting /></el-icon> 服务信息</h4>
                <div class="info-grid">
                  <div class="info-item">
                    <span class="label">服务类型：</span>
                    <span class="value">{{ getServiceTypeLabel(appointment.serviceType) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">问题描述：</span>
                    <span class="value">{{ appointment.problemDescription || '无' }}</span>
                  </div>
                </div>
              </div>

              <!-- 时间信息 -->
              <div class="item-section">
                <h4><el-icon><Clock /></el-icon> 时间信息</h4>
                <div class="info-grid">
                  <div class="info-item">
                    <span class="label">预约时间：</span>
                    <span class="value">{{ formatDateTime(appointment.startTime) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">持续时间：</span>
                    <span class="value">{{ calculateDuration(appointment.startTime, appointment.endTime) }}</span>
                  </div>
                  <div class="info-item" v-if="appointment.serviceAdvisor">
                    <span class="label">服务顾问：</span>
                    <span class="value">{{ appointment.serviceAdvisor.name || appointment.serviceAdvisor.no }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部信息 -->
            <div class="item-footer">
              <div class="create-time">
                <el-icon><Time /></el-icon>
                <span>创建时间：{{ formatDateTime(appointment.createTime) }}</span>
              </div>
              <div class="update-time" v-if="appointment.updateTime">
                <el-icon><Refresh /></el-icon>
                <span>更新时间：{{ formatDateTime(appointment.updateTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 预约详情弹窗 -->
    <el-dialog
        v-model="detailDialogVisible"
        :title="`预约详情 - ${selectedAppointment?.appointmentNo || ''}`"
        width="700px"
        top="5vh"
        destroy-on-close
    >
      <div v-if="selectedAppointment" class="appointment-detail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3><el-icon><InfoFilled /></el-icon> 基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="预约号">
              <el-tag>{{ selectedAppointment.appointmentNo }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(selectedAppointment.status)">
                {{ getStatusLabel(selectedAppointment.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(selectedAppointment.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间" v-if="selectedAppointment.updateTime">
              {{ formatDateTime(selectedAppointment.updateTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 车辆信息 -->
        <div class="detail-section">
          <h3><el-icon><Truck /></el-icon> 车辆信息</h3>
          <div class="vehicle-info">
            <div class="vehicle-avatar">
              <el-avatar :size="60" :icon="Truck" />
            </div>
            <div class="vehicle-details">
              <div class="detail-item">
                <span class="label">车牌号：</span>
                <span class="value">{{ selectedAppointment.vehicle?.plateNumber || '未知' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">品牌型号：</span>
                <span class="value">{{ selectedAppointment.vehicle?.brand }} {{ selectedAppointment.vehicle?.model }}</span>
              </div>
              <div class="detail-item">
                <span class="label">车架号：</span>
                <span class="value">{{ selectedAppointment.vehicle?.vin || '未记录' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">车主：</span>
                <span class="value">{{ selectedAppointment.owner?.name || selectedAppointment.owner?.no }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 服务信息 -->
        <div class="detail-section">
          <h3><el-icon><Setting /></el-icon> 服务信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="服务类型">
              {{ getServiceTypeLabel(selectedAppointment.serviceType) }}
            </el-descriptions-item>
            <el-descriptions-item label="问题描述">
              {{ selectedAppointment.problemDescription || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="服务顾问" v-if="selectedAppointment.serviceAdvisor">
              {{ selectedAppointment.serviceAdvisor.name || selectedAppointment.serviceAdvisor.no }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 时间信息 -->
        <div class="detail-section">
          <h3><el-icon><Clock /></el-icon> 时间信息</h3>
          <el-timeline>
            <el-timeline-item
                timestamp="预约开始"
                placement="top"
                :icon="Calendar"
                type="primary"
            >
              {{ formatDateTime(selectedAppointment.startTime) }}
            </el-timeline-item>
            <el-timeline-item
                timestamp="预约结束"
                placement="top"
                :icon="Clock"
            >
              {{ formatDateTime(selectedAppointment.endTime) }}
            </el-timeline-item>
            <el-timeline-item
                v-if="selectedAppointment.updateTime"
                timestamp="最近更新"
                placement="top"
                :icon="Refresh"
                type="info"
            >
              {{ formatDateTime(selectedAppointment.updateTime) }}
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 操作按钮 -->
        <div class="detail-actions" v-if="canCancel(selectedAppointment)">
          <el-button
              type="danger"
              :loading="canceling"
              @click="confirmCancel(selectedAppointment)"
          >
            取消预约
          </el-button>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import {
  List, Plus, Refresh, Search, Loading, Truck,
  Setting, Clock, Time, Calendar, InfoFilled,
  Timer, Check, Close, Warning, CircleCheck
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import appointmentApi from '@/api/appointment'

const router = useRouter()
const store = useStore()

// 数据状态
const loading = ref(false)
const canceling = ref(false)
const appointmentList = ref([])

// 筛选条件
const filter = ref({
  status: 'all',
  dateRange: [],
  search: ''
})

// 分页配置
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 详情弹窗
const detailDialogVisible = ref(false)
const selectedAppointment = ref(null)

// 状态选项
const statusOptions = ref([
  { value: 'pending', label: '待确认', icon: Timer },
  { value: 'confirmed', label: '已确认', icon: Check },
  { value: 'canceled', label: '已取消', icon: Close },
  { value: 'completed', label: '已完成', icon: CircleCheck }
])

// 服务类型映射
const serviceTypeMap = ref({
  maintenance: '常规保养',
  repair: '故障维修',
  inspection: '检测诊断',
  beauty: '美容清洁',
  other: '其他服务'
})

// 日期快捷方式
const dateShortcuts = ref([
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
])

// 计算预约统计
const appointmentStats = computed(() => {
  if (!appointmentList.value.length) return null

  const stats = {
    total: { type: 'total', label: '全部预约', count: 0, icon: List, color: '#409eff' },
    pending: { type: 'pending', label: '待确认', count: 0, icon: Timer, color: '#e6a23c' },
    confirmed: { type: 'confirmed', label: '已确认', count: 0, icon: Check, color: '#67c23a' },
    canceled: { type: 'canceled', label: '已取消', count: 0, icon: Close, color: '#909399' }
  }

  appointmentList.value.forEach(item => {
    stats.total.count++
    if (stats[item.status]) {
      stats[item.status].count++
    }
  })

  return Object.values(stats)
})

// 组件挂载
onMounted(() => {
  loadAppointments()
})

// 监听筛选条件变化
watch(() => filter.value, () => {
  pagination.value.currentPage = 1 // 重置到第一页
  loadAppointments()
}, { deep: true })

// 加载预约列表
const loadAppointments = async () => {
  loading.value = true
  try {
    const userId = store.state.user.userId

    // 获取所有预约
    const response = await appointmentApi.getAppointmentsByOwner(userId)

    if (response.code === 200) {
      let data = response.data || []

      // 应用筛选条件
      data = applyFilters(data)

      // 更新总数
      pagination.value.total = data.length

      // 应用分页
      const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
      const end = start + pagination.value.pageSize
      appointmentList.value = data.slice(start, end)
    } else {
      ElMessage.error(response.message || '加载预约列表失败')
      appointmentList.value = []
      pagination.value.total = 0
    }
  } catch (error) {
    console.error('加载预约失败:', error)
    ElMessage.error('加载预约列表失败，请稍后重试')
    appointmentList.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// 应用筛选条件
const applyFilters = (data) => {
  let filtered = [...data]

  // 状态筛选
  if (filter.value.status !== 'all') {
    filtered = filtered.filter(item => item.status === filter.value.status)
  }

  // 日期范围筛选
  if (filter.value.dateRange && filter.value.dateRange.length === 2) {
    const [startDate, endDate] = filter.value.dateRange
    filtered = filtered.filter(item => {
      const itemDate = new Date(item.startTime)
      return itemDate >= startDate && itemDate <= endDate
    })
  }

  // 搜索筛选
  if (filter.value.search) {
    const searchTerm = filter.value.search.toLowerCase()
    filtered = filtered.filter(item => {
      return (
          (item.appointmentNo && item.appointmentNo.toLowerCase().includes(searchTerm)) ||
          (item.vehicle?.plateNumber && item.vehicle.plateNumber.toLowerCase().includes(searchTerm)) ||
          (item.serviceType && getServiceTypeLabel(item.serviceType).toLowerCase().includes(searchTerm)) ||
          (item.problemDescription && item.problemDescription.toLowerCase().includes(searchTerm))
      )
    })
  }

  // 按时间倒序排序
  filtered.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))

  return filtered
}

// 刷新列表
const refreshList = () => {
  loadAppointments()
  ElMessage.success('列表已刷新')
}

// 跳转到创建预约页面
const goToCreateAppointment = () => {
  router.push('/appointment/create')
}

// 获取状态标签
const getStatusLabel = (status) => {
  const option = statusOptions.value.find(opt => opt.value === status)
  return option ? option.label : status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const types = {
    pending: 'warning',
    confirmed: 'success',
    canceled: 'info',
    completed: 'primary'
  }
  return types[status] || ''
}

// 获取服务类型标签
const getServiceTypeLabel = (type) => {
  return serviceTypeMap.value[type] || type
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '--'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算持续时间
const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return '--'

  const start = new Date(startTime)
  const end = new Date(endTime)
  const diffMs = end - start
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))

  if (diffHours > 0) {
    return `${diffHours}小时${diffMinutes}分钟`
  } else {
    return `${diffMinutes}分钟`
  }
}

// 判断是否可以取消预约
const canCancel = (appointment) => {
  if (!appointment) return false

  const validStatus = ['pending', 'confirmed']
  if (!validStatus.includes(appointment.status)) return false

  // 检查时间（提前2小时可取消）
  const startTime = new Date(appointment.startTime)
  const now = new Date()
  const twoHoursBefore = new Date(startTime.getTime() - 2 * 60 * 60 * 1000)

  return now < twoHoursBefore
}

// 显示详情
const showDetail = (appointment) => {
  selectedAppointment.value = appointment
  detailDialogVisible.value = true
}

// 取消预约
const handleCancel = async (appointment) => {
  try {
    await ElMessageBox.confirm(
        `确定要取消预约 ${appointment.appointmentNo} 吗？`,
        '确认取消',
        {
          confirmButtonText: '确定取消',
          cancelButtonText: '再想想',
          type: 'warning'
        }
    )

    await cancelAppointment(appointment)

  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约确认失败:', error)
    }
  }
}

// 确认取消预约
const confirmCancel = async (appointment) => {
  await cancelAppointment(appointment)
}

// 执行取消预约操作
const cancelAppointment = async (appointment) => {
  canceling.value = true
  try {
    const userId = store.state.user.userId

    const response = await appointmentApi.cancelAppointment(appointment.id, userId)

    if (response.code === 200) {
      ElMessage.success('预约已成功取消')
      detailDialogVisible.value = false
      loadAppointments() // 刷新列表
    } else {
      ElMessage.error(response.message || '取消预约失败')
    }
  } catch (error) {
    console.error('取消预约失败:', error)
    ElMessage.error('取消预约失败，请稍后重试')
  } finally {
    canceling.value = false
  }
}

// 筛选条件变化处理
const handleFilterChange = () => {
  loadAppointments()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  loadAppointments()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.value.currentPage = page
  loadAppointments()
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