<!-- src/views/UserCenter.vue -->
<template>
  <div class="user-center">
    <!-- 用户信息卡片 -->
    <el-card class="user-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3><i class="el-icon-user"></i> 个人信息</h3>
          <el-button type="primary" size="small" @click="handleEditProfile">
            <i class="el-icon-edit"></i>编辑
          </el-button>
        </div>
      </template>

      <div class="user-info">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-avatar :size="80" :src="userAvatar" class="user-avatar">
            {{ userName.charAt(0) }}
          </el-avatar>
          <div class="avatar-info">
            <div class="user-name">{{ userName }}</div>
            <el-tag :type="roleTagType" size="small">{{ roleText }}</el-tag>
          </div>
        </div>

        <!-- 信息区域 -->
        <div class="info-section">
          <div class="info-grid">
            <div class="info-item">
              <span class="label"><i class="el-icon-user"></i> 用户ID：</span>
              <span class="value">{{ userId }}</span>
            </div>
            <div class="info-item">
              <span class="label"><i class="el-icon-postcard"></i> 账号：</span>
              <span class="value">{{ userNo }}</span>
            </div>
            <div class="info-item">
              <span class="label"><i class="el-icon-phone"></i> 电话：</span>
              <span class="value">{{ userInfo.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label"><i class="el-icon-message"></i> 邮箱：</span>
              <span class="value">{{ userInfo.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label"><i class="el-icon-date"></i> 注册时间：</span>
              <span class="value">{{ formatDate(userInfo.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3><i class="el-icon-s-operation"></i> 快捷操作</h3>
      <div class="action-grid">
        <!-- 我的车辆 -->
        <el-card class="action-card" shadow="hover" @click="handleMyVehicles">
          <div class="action-content">
            <div class="action-icon primary">
              <i class="el-icon-truck"></i>
            </div>
            <div class="action-text">
              <div class="action-title">我的车辆</div>
              <div class="action-desc">查看和管理我的车辆</div>
            </div>
          </div>
        </el-card>

        <!-- 预约保养 -->
        <el-card class="action-card" shadow="hover" @click="handleAppointment">
          <div class="action-content">
            <div class="action-icon success">
              <i class="el-icon-date"></i>
            </div>
            <div class="action-text">
              <div class="action-title">预约保养</div>
              <div class="action-desc">在线预约维修保养服务</div>
            </div>
          </div>
        </el-card>

        <!-- 我的预约 -->
        <el-card class="action-card" shadow="hover" @click="handleMyAppointments">
          <div class="action-content">
            <div class="action-icon warning">
              <i class="el-icon-tickets"></i>
            </div>
            <div class="action-text">
              <div class="action-title">我的预约</div>
              <div class="action-desc">查看和管理我的预约记录</div>
            </div>
          </div>
        </el-card>

        <!-- 维修历史 -->
        <el-card class="action-card" shadow="hover" @click="handleRepairHistory">
          <div class="action-content">
            <div class="action-icon danger">
              <i class="el-icon-document"></i>
            </div>
            <div class="action-text">
              <div class="action-title">维修历史</div>
              <div class="action-desc">查看车辆维修保养记录</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 最近预约 -->
    <el-card class="recent-card" shadow="never" v-if="recentAppointments.length > 0">
      <template #header>
        <h3><i class="el-icon-time"></i> 最近预约</h3>
      </template>

      <el-table :data="recentAppointments" style="width: 100%">
        <el-table-column prop="appointmentNo" label="预约号" width="120" />
        <el-table-column prop="vehicle.plateNumber" label="车牌号" width="100" />
        <el-table-column prop="serviceType" label="服务类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getServiceTypeTag(row.serviceType)">
              {{ formatServiceType(row.serviceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="预约时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusTag(row.status)">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="handleViewAppointment(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 车辆列表弹窗 -->
    <el-dialog
        v-model="showVehicleDialog"
        title="我的车辆"
        width="80%"
        top="5vh"
        @close="showVehicleDialog = false"
    >
      <VehicleList />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import VehicleList from './VehicleList.vue'
import appointmentApi from '@/api/appointment'

const router = useRouter()
const store = useStore()
const showVehicleDialog = ref(false)
const recentAppointments = ref([])
const loading = ref(false)

// 计算属性
const userInfo = computed(() => store.state.user.userInfo)
const userName = computed(() => store.state.user.userName)
const userRole = computed(() => store.state.user.userRole)
const userId = computed(() => store.state.user.userId)
const userNo = computed(() => store.state.user.userNo)

const userAvatar = computed(() => {
  // 可以添加真实头像逻辑
  return ''
})

const roleText = computed(() => {
  const roles = {
    owner: '车主',
    service: '服务顾问',
    mechanic: '维修技师',
    warehouse: '仓库管理员'
  }
  return roles[userRole.value] || '用户'
})

const roleTagType = computed(() => {
  const types = {
    owner: 'success',
    service: 'primary',
    mechanic: 'warning',
    warehouse: 'danger'
  }
  return types[userRole.value] || 'info'
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString()
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const formatServiceType = (type) => {
  const types = {
    maintenance: '保养',
    repair: '维修',
    inspection: '检测',
    beauty: '美容',
    other: '其他'
  }
  return types[type] || type
}

const formatStatus = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    canceled: '已取消',
    completed: '已完成'
  }
  return statusMap[status] || status
}

const getServiceTypeTag = (type) => {
  const tags = {
    maintenance: 'success',
    repair: 'danger',
    inspection: 'warning',
    beauty: 'info',
    other: ''
  }
  return tags[type] || ''
}

const getStatusTag = (status) => {
  const tags = {
    pending: 'warning',
    confirmed: 'primary',
    canceled: 'info',
    completed: 'success'
  }
  return tags[status] || ''
}

// 加载最近预约
const loadRecentAppointments = async () => {
  if (userRole.value !== 'owner') return

  try {
    loading.value = true
    const response = await appointmentApi.getAppointmentsByOwner(userId.value)
    if (response.code === 200) {
      recentAppointments.value = (response.data || []).slice(0, 5) // 只显示最近5条
    }
  } catch (error) {
    console.error('加载预约失败:', error)
  } finally {
    loading.value = false
  }
}

// 事件处理
const handleEditProfile = () => {
  ElMessage.info('编辑个人信息功能开发中...')
}

const handleMyVehicles = () => {
  showVehicleDialog.value = true
}

const handleAppointment = () => {
  // 先打开车辆列表
  showVehicleDialog.value = true
}

const handleMyAppointments = () => {
  router.push('/placeholder/appointments')
}

const handleRepairHistory = () => {
  router.push('/placeholder/history')
}

const handleViewAppointment = (appointment) => {
  router.push(`/placeholder/appointment/${appointment.id}`)
}

// 组件挂载
onMounted(() => {
  loadRecentAppointments()
})
</script>

<style scoped>
.user-center {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.user-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 40px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  background-color: #409eff;
  color: white;
  font-size: 24px;
  font-weight: bold;
}

.avatar-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-name {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.info-section {
  flex: 1;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #909399;
  min-width: 80px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}

.quick-actions {
  margin-bottom: 30px;
}

.quick-actions h3 {
  margin-bottom: 20px;
  color: #303133;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.action-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.action-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.action-icon.primary {
  background-color: #409eff;
}

.action-icon.success {
  background-color: #67c23a;
}

.action-icon.warning {
  background-color: #e6a23c;
}

.action-icon.danger {
  background-color: #f56c6c;
}

.action-text {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.4;
}

.recent-card {
  margin-top: 20px;
  border-radius: 8px;
}

.recent-card h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info {
    flex-direction: column;
    text-align: center;
  }

  .avatar-section {
    flex-direction: column;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>