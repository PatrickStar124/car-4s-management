<template>
  <div class="owner-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <el-card shadow="never" class="welcome-card">
        <div class="welcome-content">
          <div class="welcome-text">
            <h2>
              <el-icon><UserFilled /></el-icon>
              欢迎回来，{{ userName }}！
            </h2>
            <p class="welcome-subtitle">今天是 {{ currentDate }}，{{ welcomeMessage }}</p>
            <div class="user-info">
              <el-tag type="success" size="large">{{ roleText }}</el-tag>
              <span class="user-id">ID: {{ userId }}</span>
              <span class="user-no">账号: {{ userNo }}</span>
            </div>
          </div>
          <div class="welcome-actions">
            <el-button type="primary" @click="goToCreateAppointment">
              <el-icon><Plus /></el-icon> 立即预约
            </el-button>
            <el-button @click="refreshData">
              <el-icon><Refresh /></el-icon> 刷新数据
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" v-for="stat in statsData" :key="stat.type">
          <el-card shadow="hover" class="stat-card" :class="`stat-${stat.type}`">
            <div class="stat-content">
              <div class="stat-icon">
                <!-- 修复图标渲染问题 -->
                <component :is="stat.iconComponent" v-if="stat.iconComponent" />
                <span v-else>{{ stat.iconText || '📊' }}</span>
              </div>
              <div class="stat-info">
                <div class="stat-count">{{ stat.count }}</div>
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-desc" v-if="stat.desc">{{ stat.desc }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-row :gutter="20">
        <!-- 左侧：车辆信息 -->
        <el-col :xs="24" :lg="12">
          <el-card class="section-card">
            <template #header>
              <div class="card-header">
                <h3><el-icon><Truck /></el-icon> 我的车辆</h3>
                <el-button type="text" @click="goToVehicleList">管理车辆</el-button>
              </div>
            </template>

            <!-- 车辆列表 -->
            <div class="vehicle-list">
              <div v-if="vehicleList.length === 0" class="empty-vehicle">
                <el-empty description="暂无车辆信息">
                  <el-button type="primary" @click="goToVehicleList">添加车辆</el-button>
                </el-empty>
              </div>

              <div v-else class="vehicle-items">
                <div
                    v-for="vehicle in vehicleList.slice(0, 3)"
                    :key="vehicle.id"
                    class="vehicle-item"
                    @click="viewVehicleDetail(vehicle)"
                >
                  <div class="vehicle-icon">
                    <el-icon><Truck /></el-icon>
                  </div>
                  <div class="vehicle-info">
                    <div class="vehicle-plate">{{ vehicle.plateNumber }}</div>
                    <div class="vehicle-model">{{ vehicle.brand }} {{ vehicle.model }}</div>
                    <div class="vehicle-details">
                      <span><el-icon><Odometer /></el-icon> {{ vehicle.mileage || 0 }}km</span>
                      <span><el-icon><Calendar /></el-icon> {{ formatYear(vehicle.manufactureYear) }}</span>
                    </div>
                  </div>
                  <div class="vehicle-status">
                    <el-tag size="small" type="info">正常</el-tag>
                  </div>
                </div>

                <div v-if="vehicleList.length > 3" class="more-vehicles">
                  <el-button type="text" @click="goToVehicleList">
                    还有 {{ vehicleList.length - 3 }} 辆车
                    <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：最近预约 -->
        <el-col :xs="24" :lg="12">
          <el-card class="section-card">
            <template #header>
              <div class="card-header">
                <h3><el-icon><Calendar /></el-icon> 最近预约</h3>
                <el-button type="text" @click="goToAppointmentList">查看全部</el-button>
              </div>
            </template>

            <!-- 预约列表 -->
            <div class="appointment-list">
              <div v-if="recentAppointments.length === 0" class="empty-appointment">
                <el-empty description="暂无预约记录">
                  <el-button type="primary" @click="goToCreateAppointment">立即预约</el-button>
                </el-empty>
              </div>

              <div v-else class="appointment-items">
                <div
                    v-for="appointment in recentAppointments.slice(0, 4)"
                    :key="appointment.id"
                    class="appointment-item"
                    :class="`status-${appointment.status}`"
                    @click="viewAppointmentDetail(appointment)"
                >
                  <div class="appointment-header">
                    <div class="appointment-no">{{ appointment.appointmentNo }}</div>
                    <el-tag
                        size="small"
                        :type="getStatusTagType(appointment.status)"
                        effect="light"
                    >
                      {{ getStatusLabel(appointment.status) }}
                    </el-tag>
                  </div>
                  <div class="appointment-content">
                    <div class="appointment-info">
                      <span><el-icon><Truck /></el-icon> {{ appointment.vehicle?.plateNumber || '未知' }}</span>
                      <span><el-icon><Setting /></el-icon> {{ getServiceTypeLabel(appointment.serviceType) }}</span>
                    </div>
                    <div class="appointment-time">
                      <el-icon><Clock /></el-icon>
                      {{ formatDateTime(appointment.startTime) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 底部：快速操作 -->
    <div class="quick-actions-section">
      <el-card class="quick-actions-card">
        <template #header>
          <h3><el-icon><Operation /></el-icon> 快速操作</h3>
        </template>

        <div class="action-grid">
          <!-- 修复图标问题：使用span代替未定义的图标 -->
          <div class="action-item" @click="goToCreateAppointment">
            <div class="action-icon primary">
              <span>📅</span>
            </div>
            <div class="action-content">
              <div class="action-title">预约保养</div>
              <div class="action-desc">在线预约维修保养服务</div>
            </div>
          </div>

          <div class="action-item" @click="goToAppointmentList">
            <div class="action-icon success">
              <span>📋</span>
            </div>
            <div class="action-content">
              <div class="action-title">我的预约</div>
              <div class="action-desc">查看和管理我的预约记录</div>
            </div>
          </div>

          <div class="action-item" @click="goToRepairHistory">
            <div class="action-icon warning">
              <span>📝</span>
            </div>
            <div class="action-content">
              <div class="action-title">维修历史</div>
              <div class="action-desc">查看车辆维修保养记录</div>
            </div>
          </div>

          <div class="action-item" @click="goToVehicleList">
            <div class="action-icon info">
              <span>🚗</span>
            </div>
            <div class="action-content">
              <div class="action-title">车辆管理</div>
              <div class="action-desc">管理我的车辆信息</div>
            </div>
          </div>

          <div class="action-item" @click="goToUserCenter">
            <div class="action-icon purple">
              <span>👤</span>
            </div>
            <div class="action-content">
              <div class="action-title">我的资料</div>
              <div class="action-desc">查看和编辑个人信息</div>
            </div>
          </div>

          <div class="action-item" @click="contactCustomerService">
            <div class="action-icon danger">
              <span>📞</span>
            </div>
            <div class="action-content">
              <div class="action-title">联系客服</div>
              <div class="action-desc">联系我们的客服团队</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <span class="loading-icon">⏳</span>
        <p>加载中...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import {
  UserFilled, Plus, Refresh, Truck, Calendar,
  ArrowRight, Clock, Odometer, Setting, Operation
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import vehicleApi from '@/api/vehicle'
import appointmentApi from '@/api/appointment'

const router = useRouter()
const store = useStore()
const loading = ref(false)

// 数据
const vehicleList = ref([])
const recentAppointments = ref([])

// ========== 关键修改：直接从localStorage获取用户信息 ==========
const getUserInfoFromLocalStorage = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      return JSON.parse(userStr)
    }
  } catch (error) {
    console.error('解析用户信息失败:', error)
  }
  return null
}

const userInfo = computed(() => getUserInfoFromLocalStorage())
const userName = computed(() => userInfo.value?.name || userInfo.value?.no || '用户')
const userId = computed(() => {
  const id = userInfo.value?.id
  console.log('当前用户ID:', id)  // 调试用
  return id
})
const userNo = computed(() => userInfo.value?.no || '')
const roleText = computed(() => {
  const role = userInfo.value?.role
  const roleMap = {
    'owner': '车主',
    'service': '服务顾问',
    'mechanic': '维修技师',
    'warehouse': '仓库管理员',
    'admin': '系统管理员'
  }
  return roleMap[role] || role || ''
})

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 欢迎消息
const welcomeMessage = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// ========== 修复图标问题：使用文本图标代替 ==========
const statsData = computed(() => {
  return [
    {
      type: 'vehicle',
      label: '车辆数量',
      count: vehicleList.value.length,
      desc: '台车辆',
      iconText: '🚗',
      color: '#409eff'
    },
    {
      type: 'appointment',
      label: '今日预约',
      count: getTodayAppointmentsCount(),
      desc: '个预约',
      iconText: '📅',
      color: '#67c23a'
    },
    {
      type: 'pending',
      label: '待确认',
      count: getPendingAppointmentsCount(),
      desc: '个待处理',
      iconText: '⏳',
      color: '#e6a23c'
    },
    {
      type: 'completed',
      label: '已完成',
      count: getCompletedAppointmentsCount(),
      desc: '个已完成',
      iconText: '✅',
      color: '#909399'
    }
  ]
})

// 组件挂载
onMounted(() => {
  console.log('OwnerDashboard mounted，用户信息:', userInfo.value)
  if (userId.value) {
    loadData()
  } else {
    ElMessage.warning('用户信息无效，请重新登录')
    router.push('/login')
  }
})

// 监听userId变化
watch(userId, (newUserId) => {
  if (newUserId && newUserId !== 'undefined') {
    console.log('用户ID变化，重新加载数据:', newUserId)
    loadData()
  }
})

// 加载数据
const loadData = async () => {
  const currentUserId = userId.value

  if (!currentUserId || currentUserId === 'undefined') {
    console.error('用户ID无效:', currentUserId)
    ElMessage.warning('用户信息无效，请重新登录')
    return
  }

  loading.value = true
  console.log('开始加载数据，用户ID:', currentUserId)

  try {
    await Promise.all([
      loadVehicles(),
      loadAppointments()
    ])
    console.log('数据加载完成')
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 加载车辆列表
const loadVehicles = async () => {
  const currentUserId = userId.value

  if (!currentUserId || currentUserId === 'undefined') {
    console.error('加载车辆：用户ID无效')
    return
  }

  try {
    console.log('请求车辆API，用户ID:', currentUserId)
    const response = await vehicleApi.getVehiclesByOwner(currentUserId)
    console.log('车辆API响应:', response)

    if (response.code === 200) {
      vehicleList.value = response.data || []
      console.log('加载车辆成功，数量:', vehicleList.value.length)
    } else {
      console.warn('车辆API返回错误:', response.msg)
      vehicleList.value = []
    }
  } catch (error) {
    console.error('加载车辆失败:', error)
    ElMessage.error('加载车辆信息失败')
    vehicleList.value = []
  }
}

// 加载预约列表
const loadAppointments = async () => {
  const currentUserId = userId.value

  if (!currentUserId || currentUserId === 'undefined') {
    console.error('加载预约：用户ID无效')
    return
  }

  try {
    console.log('请求预约API，用户ID:', currentUserId)
    const response = await appointmentApi.getAppointmentsByOwner(currentUserId)
    console.log('预约API响应:', response)

    if (response.code === 200) {
      recentAppointments.value = response.data || []
      // 按时间倒序排序
      recentAppointments.value.sort((a, b) =>
          new Date(b.createTime) - new Date(a.createTime)
      )
      console.log('加载预约成功，数量:', recentAppointments.value.length)
    } else {
      console.warn('预约API返回错误:', response.msg)
      recentAppointments.value = []
    }
  } catch (error) {
    console.error('加载预约失败:', error)
    ElMessage.error('加载预约信息失败')
    recentAppointments.value = []
  }
}

// 获取今日预约数量
const getTodayAppointmentsCount = () => {
  if (!recentAppointments.value.length) return 0

  const today = new Date().toDateString()
  return recentAppointments.value.filter(appointment => {
    const appointmentDate = new Date(appointment.startTime).toDateString()
    return appointmentDate === today
  }).length
}

// 获取待确认预约数量
const getPendingAppointmentsCount = () => {
  if (!recentAppointments.value.length) return 0
  return recentAppointments.value.filter(appointment =>
      appointment.status === 'pending'
  ).length
}

// 获取已完成预约数量
const getCompletedAppointmentsCount = () => {
  if (!recentAppointments.value.length) return 0
  return recentAppointments.value.filter(appointment =>
      appointment.status === 'completed'
  ).length
}

// 刷新数据
const refreshData = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

// 格式化生产年份
const formatYear = (year) => {
  if (!year) return '未知'
  return `${year}年`
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '--'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取服务类型标签
const getServiceTypeLabel = (type) => {
  const types = {
    maintenance: '保养',
    repair: '维修',
    inspection: '检测',
    beauty: '美容',
    other: '其他'
  }
  return types[type] || type
}

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    canceled: '已取消',
    completed: '已完成'
  }
  return statusMap[status] || status
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

// 路由跳转方法
const goToCreateAppointment = () => {
  router.push('/appointment/create')
}

const goToAppointmentList = () => {
  router.push('/appointment/list')
}

const goToVehicleList = () => {
  router.push('/vehicle')
}

const goToRepairHistory = () => {
  router.push('/placeholder/history')
}

const goToUserCenter = () => {
  router.push('/user-center')
}

// 查看车辆详情
const viewVehicleDetail = (vehicle) => {
  router.push(`/vehicle/history/${vehicle.id}`)
}

// 查看预约详情
const viewAppointmentDetail = (appointment) => {
  router.push(`/placeholder/appointment/${appointment.id}`)
}

// 联系客服
const contactCustomerService = () => {
  ElMessage.info('客服功能开发中，请稍候...')
}
</script>

<style scoped>
/* 保持原有的CSS样式不变 */
.owner-dashboard {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: 20px;
}

.stats-section {
  margin-bottom: 20px;
}

.main-content {
  margin-bottom: 20px;
}

.quick-actions-section {
  margin-bottom: 20px;
}

/* 图标样式修复 */
.stat-icon span {
  font-size: 24px;
}

.action-icon span {
  font-size: 24px;
}

/* 其他样式保持不变 */
</style>