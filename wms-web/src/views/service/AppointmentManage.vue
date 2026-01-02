<!-- src/views/service/AppointmentManage.vue -->
<template>
  <div class="appointment-manage-container">
    <div class="page-header">
      <h1><el-icon :icon="CalendarCheck" /> 预约管理</h1>
    </div>

    <el-card shadow="hover" class="appointment-card">
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="全部状态" @change="loadAppointments">
          <el-option label="待处理" value="pending" />
          <el-option label="已确认" value="confirmed" />
          <el-option label="已取消" value="canceled" />
        </el-select>
      </div>

      <el-table
          :data="appointmentList"
          border
          style="width: 100%"
          v-loading="loading"
      >
        <el-table-column prop="appointmentNo" label="预约单号" width="180" />
        <el-table-column
            label="预约车辆"
            min-width="200"
            :formatter="formatVehicleInfo"
        />
        <el-table-column prop="serviceType" label="服务类型" width="120">
          <template #default="scope">
            <el-tag type="info">{{ formatServiceType(scope.row.serviceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
            prop="startTime"
            label="预约时间"
            width="200"
            :formatter="formatDateTime"
        />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
                :type="statusTagType(scope.row.status)"
            >
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 新增：已分配技师列 -->
        <el-table-column prop="assignedMechanicName" label="已分配技师" min-width="150" />

        <el-table-column label="操作" width="350" fixed="right">
          <template #default="scope">
            <!-- 1. 如果状态是“待处理”，只显示“确认并创建工单”按钮 -->
            <el-button
                v-if="scope.row.status === 'pending'"
                type="primary"
                size="small"
                @click="handleConfirmAppointment(scope.row)"
            >
              确认并创建工单
            </el-button>

            <!-- 2. 如果状态不是“待处理”，则进入后续逻辑 -->
            <template v-else>
              <!-- 2.1 如果 orderId 存在（说明工单已创建） -->
              <template v-if="scope.row.orderId">
                <!-- 显示“查看工单”按钮 -->
                <el-button
                    type="success"
                    size="small"
                    :icon="View"
                    @click="goToOrderDetail(scope.row)"
                >
                  查看工单
                </el-button>

                <!-- 如果还未分配技师，则显示“分配技师”按钮 -->
                <el-button
                    v-if="!scope.row.assignedMechanicId"
                    type="info"
                    size="small"
                    :icon="User"
                    @click="openAssignMechanicDialog(scope.row)"
                >
                  分配技师
                </el-button>
              </template>

              <!-- 2.2 如果 orderId 不存在（例如状态为“已取消”），则显示“已处理” -->
              <el-button
                  v-else
                  type="default"
                  size="small"
                  disabled
              >
                已处理
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="empty-state" v-if="!loading && appointmentList.length === 0">
        <el-empty description="暂无预约信息" />
      </div>
    </el-card>

    <!-- 新增：分配技师弹窗 -->
    <el-dialog
        title="分配维修技师"
        v-model="assignDialogVisible"
        width="400px"
        @close="resetAssignForm"
    >
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="维修技师" prop="mechanicId">
          <el-select v-model="assignForm.mechanicId" placeholder="请选择技师" clearable>
            <el-option
                v-for="mechanic in mechanicList"
                :key="mechanic.id"
                :label="mechanic.name"
                :value="mechanic.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CalendarCheck, View, User } from '@element-plus/icons-vue'
import appointmentApi from '@/api/appointment'
import repairOrderApi from '@/api/repairOrder'
// 假设你有一个获取用户信息的API文件
import userApi from '@/api/user'

const router = useRouter()
const store = useStore()
const loading = ref(true)
const appointmentList = ref([])
const statusFilter = ref('pending')

// --- 新增：分配技师相关变量 ---
const assignDialogVisible = ref(false)
const assignForm = ref({ mechanicId: null })
const mechanicList = ref([]) // 维修技师列表
const currentAppointment = ref(null) // 当前操作的预约/工单

// ✅ 核心修正：跳转工单详情页的方法
const goToOrderDetail = (appointment) => {
  const orderId = appointment.orderId;
  if (orderId) {
    router.push({
      name: 'OrderDetail',
      params: { id: orderId }
    })
  } else {
    ElMessage.warning('未能找到关联的工单信息，请刷新页面重试。')
  }
}

const loadAppointments = async () => {
  loading.value = true
  try {
    const response = await appointmentApi.getAppointmentsByStatus(statusFilter.value)
    if (response.code === 200) {
      appointmentList.value = response.data
    } else {
      ElMessage.error('获取预约列表失败')
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('网络错误，获取预约列表失败')
  } finally {
    loading.value = false
  }
}

// ✅ 核心优化：实现 handleConfirmAppointment 函数
const handleConfirmAppointment = async (appointment) => {
  try {
    await ElMessageBox.confirm(
        '确认要将此预约转为维修工单吗？',
        '提示',
        { type: 'warning' }
    )

    const serviceAdvisorId = store.getters['user/userId'];
    if (!serviceAdvisorId) {
      ElMessage.error('无法获取当前用户信息，请重新登录！');
      return;
    }

    await appointmentApi.confirmAppointment(appointment.id, serviceAdvisorId);

    const createOrderResponse = await repairOrderApi.createOrderFromAppointment(appointment.id);

    if (createOrderResponse.code === 200) {
      ElMessage.success('工单创建成功！');
      // ✅ 核心修复：找到 appointmentList 中对应的项并更新
      const target = appointmentList.value.find(item => item.id === appointment.id);
      if (target) {
        target.status = 'confirmed'; // 状态变为已确认
        target.orderId = createOrderResponse.data.id; // 存入工单ID
      }
    } else {
      ElMessage.error(`创建工单失败: ${createOrderResponse.msg}`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认并创建工单失败:', error);
      ElMessage.error('操作失败，请联系管理员');
    }
  }
}

// --- 新增：加载维修技师列表 ---
const loadMechanics = async () => {
  try {
    // 假设 userApi.getUsersByRole('mechanic') 可以获取所有角色为 'mechanic' 的用户
    const res = await userApi.getUsersByRole('mechanic')
    if (res.code === 200) {
      mechanicList.value = res.data
    }
  } catch (err) {
    ElMessage.error('加载技师列表失败')
  }
}

// --- 新增：打开分配技师弹窗 ---
const openAssignMechanicDialog = (appointment) => {
  currentAppointment.value = appointment
  assignDialogVisible.value = true
  loadMechanics() // 打开弹窗时加载技师列表
}

// --- 新增：提交分配 ---
const submitAssign = async () => {
  if (!assignForm.value.mechanicId) {
    ElMessage.warning('请选择维修技师')
    return
  }

  // ✅ 增加一个健壮性检查，防止在异常情况下调用
  if (!currentAppointment.value || !currentAppointment.value.orderId) {
    ElMessage.error('操作无效，未找到关联的工单信息。')
    assignDialogVisible.value = false
    return
  }

  try {
    // 调用后端接口
    const res = await repairOrderApi.assignMechanic(
        currentAppointment.value.orderId, // 工单ID
        assignForm.value.mechanicId       // 技师ID
    )
    if (res.code === 200) {
      ElMessage.success('分配成功')
      assignDialogVisible.value = false
      // 更新本地视图
      const target = appointmentList.value.find(item => item.id === currentAppointment.value.id)
      if (target) {
        const assignedMechanic = mechanicList.value.find(m => m.id === assignForm.value.mechanicId)
        target.assignedMechanicId = assignForm.value.mechanicId
        target.assignedMechanicName = assignedMechanic ? assignedMechanic.name : '未知技师'
      }
    } else {
      ElMessage.error(res.msg || '分配失败')
    }
  } catch (err) {
    ElMessage.error('分配失败，请检查网络或联系管理员')
  }
}

// --- 新增：重置分配表单 ---
const resetAssignForm = () => {
  assignForm.value = { mechanicId: null }
}

const formatVehicleInfo = (row) => {
  return row.vehicle ? `${row.vehicle.brand || ''} ${row.vehicle.model || ''}（${row.vehicle.plateNumber || ''}）` : '车辆信息未关联';
}

const formatServiceType = (type) => {
  const map = { 'maintenance': '常规保养', 'repair': '维修', 'inspection': '检测' }
  return map[type] || type
}

const formatDateTime = (row) => {
  return row.startTime ? new Date(row.startTime).toLocaleString() : '-'
}

const formatStatus = (status) => {
  const map = { 'pending': '待处理', 'confirmed': '已确认', 'canceled': '已取消', 'completed': '已完成' }
  return map[status] || status
}

const statusTagType = (status) => {
  const map = { 'pending': 'warning', 'confirmed': 'success', 'canceled': 'danger', 'completed': 'info' }
  return map[status] || 'default'
}

onMounted(() => {
  loadAppointments()
})
</script>

<style scoped>
.appointment-manage-container {
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.filter-bar {
  margin-bottom: 15px;
}
.appointment-card {
  height: calc(100vh - 120px);
  overflow: auto;
}
.empty-state {
  margin-top: 40px;
  text-align: center;
}
</style>