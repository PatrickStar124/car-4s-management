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
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <!-- 确认并创建工单按钮 -->
            <el-button
                v-if="scope.row.status === 'pending'"
                type="primary"
                size="small"
                @click="handleConfirmAppointment(scope.row)"
            >
              确认并创建工单
            </el-button>

            <!-- =================== 新增：“查看工单”按钮 (START) =================== -->
            <el-button
                v-else-if="scope.row.status === 'confirmed'"
                type="success"
                size="small"
                icon="View"
                @click="goToOrderDetail(scope.row)"
            >
              查看工单
            </el-button>
            <!-- =================== 新增：“查看工单”按钮 (END) =================== -->

            <!-- 已处理按钮 -->
            <el-button
                v-else
                type="default"
                size="small"
                disabled
            >
              已处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="empty-state" v-if="!loading && appointmentList.length === 0">
        <el-empty description="暂无预约信息" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router' // ✅ 新增：引入 useRouter
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CalendarCheck } from '@element-plus/icons-vue' // ✅ 新增：引入 View 图标
import appointmentApi from '@/api/appointment'
import repairOrderApi from '@/api/repairOrder'

const router = useRouter() // ✅ 新增：初始化 router
const store = useStore()
const loading = ref(true)
const appointmentList = ref([])
const statusFilter = ref('pending')

// =================== 新增：跳转到工单详情页的方法 (START) ===================
const goToOrderDetail = (appointment) => {
  // 从当前行数据中获取之前保存的 orderId
  const orderId = appointment.orderId;

  if (orderId) {
    router.push({
      name: 'OrderDetail', // 使用路由的 name 进行跳转
      query: { id: orderId } // 将工单ID作为参数传递
    })
  } else {
    ElMessage.warning('未能找到关联的工单信息，可能需要刷新页面。')
  }
}
// =================== 新增：跳转到工单详情页的方法 (END) ===================


const loadAppointments = async () => {
  loading.value = true
  try {
    const response = await appointmentApi.getAppointmentsByStatus(statusFilter.value)
    if (response.code === 200) {
      appointmentList.value = response.data
    } else {
      ElMessage.error(response.msg || '获取预约列表失败')
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('网络错误，获取预约列表失败')
  } finally {
    loading.value = false
  }
}

const handleConfirmAppointment = async (appointment) => {
  try {
    await ElMessageBox.confirm(
        '确认要将此预约转为维修工单吗？',
        '提示',
        { type: 'warning' }
    )

    const serviceAdvisorId = store.getters['user/userId'];
    await appointmentApi.confirmAppointment(appointment.id, serviceAdvisorId);

    const response = await repairOrderApi.createOrderFromAppointment(appointment.id);

    if (response.code === 200) {
      ElMessage.success('工单创建成功！');

      // ✅ 修改：将新创建的工单ID存入当前行数据中，以便“查看工单”按钮使用
      // 假设后端返回的数据结构为 { code: 200, data: { id: 123, ... } }
      const newOrderId = response.data.id;
      if (newOrderId) {
        appointment.orderId = newOrderId;
      }

      loadAppointments();
    } else {
      ElMessage.error(response.msg || '工单创建失败');
    }
  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('确认预约并创建工单失败:', error);
      ElMessage.error('操作失败，请联系管理员');
    }
  }
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