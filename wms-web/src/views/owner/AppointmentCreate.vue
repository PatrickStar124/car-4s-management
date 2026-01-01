<template>
  <div class="appointment-create-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1><el-icon><Calendar /></el-icon> 在线预约保养</h1>
      <p class="page-desc">预约您的车辆维修保养服务，选择方便的时间，我们会为您安排专业的服务顾问</p>
    </div>

    <!-- 预约步骤 -->
    <div class="steps-container">
      <el-steps :active="activeStep" align-center finish-status="success">
        <el-step title="选择车辆" description="选择需要服务的车辆" />
        <el-step title="选择服务" description="选择服务类型和内容" />
        <el-step title="选择时间" description="选择预约日期和时间" />
        <el-step title="确认提交" description="确认预约信息并提交" />
      </el-steps>
    </div>

    <!-- 主内容区域 -->
    <div class="appointment-form-container">
      <!-- 步骤1：选择车辆 -->
      <div v-if="activeStep === 0" class="step-content">
        <h3 class="step-title">选择服务车辆</h3>
        <p class="step-desc">请选择您需要预约服务的车辆，如果车辆未添加，请先添加车辆。</p>

        <!-- 车辆列表 -->
        <div class="vehicle-list">
          <div
              v-for="vehicle in vehicleList"
              :key="vehicle.id"
              class="vehicle-card"
              :class="{ 'active': selectedVehicleId === vehicle.id }"
              @click="selectVehicle(vehicle)"
          >
            <div class="vehicle-card-content">
              <div class="vehicle-icon">
                <el-icon><Truck /></el-icon>
              </div>
              <div class="vehicle-info">
                <div class="vehicle-name">{{ vehicle.brand || '未知品牌' }} {{ vehicle.model || '未知型号' }}</div>
                <div class="vehicle-plate">
                  <el-tag size="small" type="primary">{{ vehicle.plateNumber }}</el-tag>
                </div>
                <div class="vehicle-details">
                  <span><el-icon><User /></el-icon> 车主：{{ vehicle.owner?.name || vehicle.ownerId }}</span>
                  <span><el-icon><Odometer /></el-icon> 里程：{{ vehicle.mileage || 0 }} km</span>
                  <span><el-icon><DataLine /></el-icon> 车架号：{{ vehicle.vin || '未记录' }}</span>
                </div>
              </div>
              <div class="vehicle-select">
                <el-radio :label="vehicle.id" v-model="selectedVehicleId"></el-radio>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="vehicleList.length === 0" class="empty-vehicle">
            <el-empty description="您还没有添加车辆">
              <el-button type="primary" @click="goToAddVehicle">添加车辆</el-button>
            </el-empty>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="step-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button
              type="primary"
              :disabled="!selectedVehicleId"
              @click="nextStep"
          >
            下一步
          </el-button>
        </div>
      </div>

      <!-- 步骤2：选择服务 -->
      <div v-if="activeStep === 1" class="step-content">
        <h3 class="step-title">选择服务类型</h3>
        <p class="step-desc">请选择您需要的服务类型，并填写相关描述。</p>

        <div class="service-form">
          <!-- 服务类型选择 -->
          <div class="form-section">
            <h4><el-icon><Setting /></el-icon> 服务类型</h4>
            <div class="service-types">
              <div
                  v-for="service in serviceTypes"
                  :key="service.value"
                  class="service-type-card"
                  :class="{ 'active': formData.serviceType === service.value }"
                  @click="selectServiceType(service.value)"
              >
                <div class="service-icon">
                  <el-icon><component :is="service.icon" /></el-icon>
                </div>
                <div class="service-info">
                  <div class="service-name">{{ service.label }}</div>
                  <div class="service-desc">{{ service.desc }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 问题描述 -->
          <div class="form-section">
            <h4><el-icon><Edit /></el-icon> 问题描述</h4>
            <el-input
                v-model="formData.problemDescription"
                type="textarea"
                :rows="4"
                placeholder="请详细描述您遇到的问题或需要的服务（例如：车辆异响、定期保养、更换机油等）"
                maxlength="500"
                show-word-limit
            />
          </div>

          <!-- 选择服务顾问（可选） -->
          <div class="form-section">
            <h4><el-icon><User /></el-icon> 选择服务顾问（可选）</h4>
            <el-select
                v-model="formData.preferredAdvisorId"
                placeholder="选择服务顾问"
                clearable
                filterable
                style="width: 100%"
            >
              <el-option
                  v-for="advisor in serviceAdvisors"
                  :key="advisor.id"
                  :label="advisor.name || advisor.no"
                  :value="advisor.id"
              >
                <div class="advisor-option">
                  <span>{{ advisor.name || advisor.no }}</span>
                  <span class="option-desc">工号：{{ advisor.no }}</span>
                </div>
              </el-option>
            </el-select>
            <p class="form-tip">不选择将为您随机分配服务顾问</p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button
              type="primary"
              :disabled="!formData.serviceType"
              @click="nextStep"
          >
            下一步
          </el-button>
        </div>
      </div>

      <!-- 步骤3：选择时间 -->
      <div v-if="activeStep === 2" class="step-content">
        <h3 class="step-title">选择预约时间</h3>
        <p class="step-desc">请选择您方便的预约时间，系统会自动检查时间冲突。</p>

        <div class="time-selection">
          <!-- 日期选择 -->
          <div class="form-section">
            <h4><el-icon><Calendar /></el-icon> 选择日期</h4>
            <el-date-picker
                v-model="selectedDate"
                type="date"
                placeholder="选择预约日期"
                :disabled-date="disabledDate"
                :shortcuts="dateShortcuts"
                @change="onDateChange"
                style="width: 100%"
            />
          </div>

          <!-- 时间选择 -->
          <div class="form-section" v-if="selectedDate">
            <h4><el-icon><Clock /></el-icon> 选择时间段</h4>
            <div class="time-slots">
              <div
                  v-for="slot in timeSlots"
                  :key="slot.value"
                  class="time-slot"
                  :class="{
                  'active': formData.startTime === slot.start,
                  'disabled': isTimeSlotDisabled(slot),
                  'conflict': isTimeSlotConflict(slot)
                }"
                  @click="selectTimeSlot(slot)"
              >
                <div class="slot-time">{{ slot.label }}</div>
                <div class="slot-status">
                  <span v-if="isTimeSlotConflict(slot)" class="conflict-text">冲突</span>
                  <span v-else-if="isTimeSlotDisabled(slot)" class="disabled-text">不可用</span>
                  <span v-else class="available-text">可用</span>
                </div>
              </div>
            </div>
            <p class="form-tip">每个时间段为2小时，请根据您的需求选择</p>
          </div>

          <!-- 时间冲突检测 -->
          <div v-if="hasTimeConflict && !selectedTimeSlot" class="conflict-warning">
            <el-alert
                title="检测到时间冲突"
                type="warning"
                show-icon
                :closable="false"
            >
              <p>您选择的车辆在该时间段已有预约，请选择其他时间。</p>
            </el-alert>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button
              type="primary"
              :disabled="!selectedTimeSlot || hasTimeConflict"
              @click="checkTimeConflict"
          >
            检查时间冲突
          </el-button>
          <el-button
              type="success"
              :disabled="!selectedTimeSlot || hasTimeConflict"
              @click="nextStep"
          >
            下一步
          </el-button>
        </div>
      </div>

      <!-- 步骤4：确认提交 -->
      <div v-if="activeStep === 3" class="step-content">
        <h3 class="step-title">确认预约信息</h3>
        <p class="step-desc">请确认您的预约信息，确认无误后提交。</p>

        <div class="confirmation-card">
          <h4><el-icon><Document /></el-icon> 预约详情</h4>

          <div class="info-grid">
            <!-- 车辆信息 -->
            <div class="info-section">
              <h5><el-icon><Truck /></el-icon> 车辆信息</h5>
              <div class="info-details">
                <div class="info-item">
                  <span class="label">车牌号码：</span>
                  <span class="value">{{ selectedVehicle?.plateNumber }}</span>
                </div>
                <div class="info-item">
                  <span class="label">车辆型号：</span>
                  <span class="value">{{ selectedVehicle?.brand }} {{ selectedVehicle?.model }}</span>
                </div>
                <div class="info-item">
                  <span class="label">车架号：</span>
                  <span class="value">{{ selectedVehicle?.vin || '未记录' }}</span>
                </div>
              </div>
            </div>

            <!-- 服务信息 -->
            <div class="info-section">
              <h5><el-icon><Setting /></el-icon> 服务信息</h5>
              <div class="info-details">
                <div class="info-item">
                  <span class="label">服务类型：</span>
                  <span class="value">{{ getServiceTypeLabel(formData.serviceType) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">问题描述：</span>
                  <span class="value">{{ formData.problemDescription || '无' }}</span>
                </div>
                <div class="info-item" v-if="formData.preferredAdvisorId">
                  <span class="label">偏好顾问：</span>
                  <span class="value">{{ getAdvisorName(formData.preferredAdvisorId) }}</span>
                </div>
              </div>
            </div>

            <!-- 时间信息 -->
            <div class="info-section">
              <h5><el-icon><Clock /></el-icon> 时间信息</h5>
              <div class="info-details">
                <div class="info-item">
                  <span class="label">预约日期：</span>
                  <span class="value">{{ formatDateForDisplay(selectedDate) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">预约时间：</span>
                  <span class="value">{{ formatTimeForDisplay(selectedTimeSlot) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">预计时长：</span>
                  <span class="value">2小时</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 特别提示 -->
          <div class="notice-section">
            <el-alert
                title="重要提示"
                type="info"
                show-icon
                :closable="false"
            >
              <ul>
                <li>请在预约时间前10分钟到达门店</li>
                <li>如需取消预约，请提前2小时以上操作</li>
                <li>请携带车辆行驶证和车主身份证</li>
                <li>预约确认后，服务顾问会与您联系确认细节</li>
              </ul>
            </el-alert>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button
              type="primary"
              :loading="submitting"
              @click="submitAppointment"
          >
            确认提交预约
          </el-button>
        </div>
      </div>
    </div>

    <!-- 加载中遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import {
  Calendar, Truck, User, Odometer, DataLine,
  Setting, Edit, Clock, Document, Loading
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import vehicleApi from '@/api/vehicle'
import appointmentApi from '@/api/appointment'
import userApi from '@/api/user'

const router = useRouter()
const store = useStore()
const loading = ref(false)
const submitting = ref(false)

// 步骤控制
const activeStep = ref(0)

// 车辆相关
const vehicleList = ref([])
const selectedVehicleId = ref(null)
const selectedVehicle = computed(() => {
  return vehicleList.value.find(v => v.id === selectedVehicleId.value)
})

// 服务类型选项
const serviceTypes = ref([
  { value: 'maintenance', label: '常规保养', desc: '机油、机滤、空滤等常规保养项目', icon: 'Setting' },
  { value: 'repair', label: '故障维修', desc: '车辆故障诊断与修复', icon: 'Tools' },
  { value: 'inspection', label: '检测诊断', desc: '车辆全面检测与诊断', icon: 'Search' },
  { value: 'beauty', label: '美容清洁', desc: '洗车、打蜡、内饰清洁等', icon: 'Brush' },
  { value: 'other', label: '其他服务', desc: '其他特殊服务需求', icon: 'More' }
])

// 服务顾问列表
const serviceAdvisors = ref([])

// 表单数据
const formData = ref({
  serviceType: '',
  problemDescription: '',
  preferredAdvisorId: null,
  startTime: null,
  endTime: null
})

// 时间选择相关
const selectedDate = ref(null)
const selectedTimeSlot = ref(null)
const hasTimeConflict = ref(false)

// 时间槽位配置（每天9:00-17:00，每2小时一个槽位）
const timeSlots = ref([
  { label: '09:00 - 11:00', value: '09:00', start: '09:00', end: '11:00' },
  { label: '11:00 - 13:00', value: '11:00', start: '11:00', end: '13:00' },
  { label: '13:00 - 15:00', value: '13:00', start: '13:00', end: '15:00' },
  { label: '15:00 - 17:00', value: '15:00', start: '15:00', end: '17:00' }
])

// 日期快捷方式
const dateShortcuts = ref([
  {
    text: '今天',
    value: new Date()
  },
  {
    text: '明天',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24)
      return date
    }
  },
  {
    text: '一周后',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24 * 7)
      return date
    }
  }
])

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})

// 加载所需数据
const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadVehicles(),
      loadServiceAdvisors()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 加载车辆列表
const loadVehicles = async () => {
  try {
    // 【核心修改1】使用 getter 获取 userId
    const userId = store.getters['user/userId']

    // 增加健壮性检查
    if (!userId) {
      console.error('用户未登录或信息不完整，无法加载车辆列表。');
      ElMessage.warning('用户信息无效，请重新登录');
      vehicleList.value = [];
      return;
    }

    const response = await vehicleApi.getVehiclesByOwner(userId)

    if (response.code === 200) {
      vehicleList.value = response.data || []
      if (vehicleList.value.length > 0) {
        selectedVehicleId.value = vehicleList.value[0].id
      }
    } else {
      ElMessage.error(response.message || '加载车辆列表失败')
    }
  } catch (error) {
    console.error('加载车辆失败:', error)
    vehicleList.value = []
  }
}

// 加载服务顾问列表
const loadServiceAdvisors = async () => {
  try {
    const response = await userApi.getServiceAdvisors()

    if (response.code === 200) {
      serviceAdvisors.value = response.data || []
    }
  } catch (error) {
    console.error('加载服务顾问失败:', error)
    serviceAdvisors.value = []
  }
}

// 选择车辆
const selectVehicle = (vehicle) => {
  selectedVehicleId.value = vehicle.id
}

// 选择服务类型
const selectServiceType = (type) => {
  formData.value.serviceType = type
}

// 根据服务类型值获取标签
const getServiceTypeLabel = (type) => {
  const service = serviceTypes.value.find(s => s.value === type)
  return service ? service.label : type
}

// 获取顾问姓名
const getAdvisorName = (advisorId) => {
  const advisor = serviceAdvisors.value.find(a => a.id === advisorId)
  return advisor ? (advisor.name || advisor.no) : '未知顾问'
}

// 步骤控制方法
const nextStep = () => {
  if (activeStep.value < 3) {
    activeStep.value++

    // 进入时间选择步骤时，检查是否选择了服务类型
    if (activeStep.value === 2 && !formData.value.serviceType) {
      ElMessage.warning('请先选择服务类型')
      activeStep.value = 1
      return
    }
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到添加车辆页面
const goToAddVehicle = () => {
  router.push('/vehicle')
}

// 日期禁用规则（不能选择过去的日期）
const disabledDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

// 日期变化处理
const onDateChange = () => {
  selectedTimeSlot.value = null
  formData.value.startTime = null
  formData.value.endTime = null
  hasTimeConflict.value = false
}

// 判断时间槽位是否可用（简单判断：不选择过去的时间）
const isTimeSlotDisabled = (slot) => {
  if (!selectedDate.value) return true

  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const selectedDay = new Date(selectedDate.value.getFullYear(), selectedDate.value.getMonth(), selectedDate.value.getDate())

  // 如果是今天，判断时间是否已经过去
  if (selectedDay.getTime() === today.getTime()) {
    const currentHour = now.getHours()
    const slotHour = parseInt(slot.start.split(':')[0])
    return currentHour >= slotHour
  }

  return false
}

// 判断时间槽位是否有冲突（这里只是前端演示，真实冲突检查在后端）
const isTimeSlotConflict = () => {
  // 这里可以添加更复杂的冲突检测逻辑
  // 实际项目中，冲突检测应该在后端进行
  return false
}

// 选择时间槽位
const selectTimeSlot = (slot) => {
  if (isTimeSlotDisabled(slot)) {
    ElMessage.warning('该时间段不可用，请选择其他时间')
    return
  }

  selectedTimeSlot.value = slot
  formData.value.startTime = slot.start
  formData.value.endTime = slot.end
  hasTimeConflict.value = false
}

// 检查时间冲突
const checkTimeConflict = async () => {
  if (!selectedVehicleId.value || !selectedDate.value || !selectedTimeSlot.value) {
    ElMessage.warning('请先选择车辆和时间')
    return
  }

  loading.value = true
  try {
    // 构建完整的时间字符串
    const dateStr = selectedDate.value.toISOString().split('T')[0]
    const startDateTime = `${dateStr}T${selectedTimeSlot.value.start}:00`
    const endDateTime = `${dateStr}T${selectedTimeSlot.value.end}:00`

    const response = await appointmentApi.checkTimeConflict(
        selectedVehicleId.value,
        startDateTime,
        endDateTime
    )

    if (response.code === 200) {
      hasTimeConflict.value = false
      ElMessage.success('时间可用，无冲突')
      nextStep() // 自动进入下一步
    } else {
      hasTimeConflict.value = true
      ElMessage.error(response.message || '时间冲突，请选择其他时间')
    }
  } catch (error) {
    console.error('检查时间冲突失败:', error)
    // 如果检查失败，仍然允许用户继续（或者提示用户手动检查）
    ElMessage.warning('时间冲突检查失败，请确保选择的时间没有重复预约')
    hasTimeConflict.value = false
  } finally {
    loading.value = false
  }
}

// 格式化日期显示
const formatDateForDisplay = (date) => {
  if (!date) return ''
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

// 格式化时间显示
const formatTimeForDisplay = (slot) => {
  if (!slot) return ''
  return slot.label
}

// 提交预约
const submitAppointment = async () => {
  try {
    // 数据验证
    if (!selectedVehicleId.value) {
      ElMessage.error('请选择车辆')
      return
    }

    if (!formData.value.serviceType) {
      ElMessage.error('请选择服务类型')
      return
    }

    if (!selectedDate.value || !selectedTimeSlot.value) {
      ElMessage.error('请选择预约时间')
      return
    }

    // 【核心修改2】使用 getter 获取 userId
    const userId = store.getters['user/userId']

    // 增加健壮性检查
    if (!userId) {
      console.error('用户未登录或信息不完整，无法提交预约。');
      ElMessage.warning('用户信息无效，请重新登录');
      return;
    }

    // 构建预约数据
    const appointmentData = {
      vehicleId: selectedVehicleId.value,
      ownerId: userId, // 使用从 getter 获取的 userId
      serviceType: formData.value.serviceType,
      problemDescription: formData.value.problemDescription,
      serviceAdvisorId: formData.value.preferredAdvisorId,
      startTime: `${selectedDate.value.toISOString().split('T')[0]}T${selectedTimeSlot.value.start}:00`,
      endTime: `${selectedDate.value.toISOString().split('T')[0]}T${selectedTimeSlot.value.end}:00`
    }

    // 确认对话框
    await ElMessageBox.confirm(
        '确认提交预约吗？提交后服务顾问会尽快与您联系确认。',
        '确认预约',
        {
          confirmButtonText: '确认提交',
          cancelButtonText: '再检查一下',
          type: 'warning'
        }
    )

    submitting.value = true

    // 调用API
    const response = await appointmentApi.createAppointment(appointmentData)

    if (response.code === 200) {
      ElMessage.success('预约创建成功！预约号：' + response.data.appointmentNo)

      // 跳转到预约列表页面
      setTimeout(() => {
        router.push({
          name: 'AppointmentList',
          query: { success: true }
        })
      }, 1500)
    } else {
      ElMessage.error(response.message || '预约创建失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交预约失败:', error)
      ElMessage.error('提交预约失败：' + (error.message || '未知错误'))
    }
  } finally {
    submitting.value = false
  }
}
</script>
<style scoped lang="scss">
.appointment-create-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.page-header {
  text-align: center;
  margin-bottom: 40px;

  h1 {
    color: #303133;
    font-size: 28px;
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
  }

  .page-desc {
    color: #909399;
    font-size: 16px;
    line-height: 1.6;
  }
}

.steps-container {
  margin-bottom: 40px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.appointment-form-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.step-content {
  .step-title {
    font-size: 22px;
    color: #303133;
    margin-bottom: 10px;
  }

  .step-desc {
    color: #606266;
    margin-bottom: 30px;
    line-height: 1.6;
  }
}

// 车辆列表样式
.vehicle-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.vehicle-card {
  border: 2px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;

  &:hover {
    border-color: #409eff;
    background: #f0f9ff;
  }

  &.active {
    border-color: #409eff;
    background: #ecf5ff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
  }
}

.vehicle-card-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.vehicle-icon {
  width: 60px;
  height: 60px;
  background: #409eff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.vehicle-info {
  flex: 1;

  .vehicle-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .vehicle-details {
    display: flex;
    flex-direction: column;
    gap: 5px;
    color: #606266;
    font-size: 13px;

    span {
      display: flex;
      align-items: center;
      gap: 5px;
    }
  }
}

.vehicle-select {
  display: flex;
  align-items: center;
}

.empty-vehicle {
  grid-column: 1 / -1;
  padding: 40px 0;
}

// 服务类型样式
.service-types {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 30px;
}

.service-type-card {
  border: 2px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;

  &:hover {
    border-color: #409eff;
    transform: translateY(-2px);
  }

  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }
}

.service-icon {
  width: 50px;
  height: 50px;
  background: #f0f9ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 24px;
  margin-bottom: 15px;
}

.service-info {
  .service-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 5px;
  }

  .service-desc {
    font-size: 12px;
    color: #909399;
    line-height: 1.4;
  }
}

// 时间槽位样式
.time-slots {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.time-slot {
  border: 2px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover:not(.disabled) {
    border-color: #409eff;
  }

  &.active {
    border-color: #67c23a;
    background: #f0f9eb;
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background: #f5f7fa;

    .disabled-text {
      color: #f56c6c;
    }
  }

  &.conflict {
    border-color: #f56c6c;
    background: #fef0f0;

    .conflict-text {
      color: #f56c6c;
      font-weight: 600;
    }
  }
}

.slot-time {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 5px;
  color: #303133;
}

.slot-status {
  font-size: 12px;

  .available-text {
    color: #67c23a;
  }
}

// 确认信息样式
.confirmation-card {
  .info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 30px;
    margin-bottom: 30px;
  }
}

.info-section {
  h5 {
    font-size: 16px;
    color: #303133;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.info-details {
  .info-item {
    margin-bottom: 12px;
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

.notice-section {
  margin-top: 30px;

  ul {
    margin: 10px 0 0 20px;

    li {
      margin-bottom: 5px;
      color: #606266;
    }
  }
}

// 表单部分样式
.form-section {
  margin-bottom: 30px;

  h4 {
    font-size: 16px;
    color: #303133;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.form-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}

.conflict-warning {
  margin: 20px 0;
}

// 操作按钮样式
.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

// 顾问选项样式
.advisor-option {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .option-desc {
    font-size: 12px;
    color: #909399;
  }
}

// 加载中样式
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;

  .loading-icon {
    font-size: 40px;
    color: #409eff;
    animation: rotate 2s linear infinite;
    margin-bottom: 20px;
  }

  p {
    color: #606266;
    font-size: 16px;
  }
}

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
  .appointment-create-container {
    padding: 15px;
  }

  .steps-container {
    padding: 15px;
  }

  .appointment-form-container {
    padding: 20px;
  }

  .vehicle-list,
  .service-types,
  .time-slots {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .vehicle-card-content {
    flex-direction: column;
    text-align: center;
  }

  .step-actions {
    flex-direction: column;

    button {
      width: 100%;
    }
  }
}
</style>