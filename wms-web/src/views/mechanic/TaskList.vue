<!-- src/views/mechanic/TaskList.vue -->
<template>
  <div class="mechanic-task-container">
    <div class="page-header">
      <h1><el-icon :icon="Wrench" /> 我的任务</h1>
    </div>

    <el-card shadow="hover" class="task-card">
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="全部任务状态" clearable @change="loadTasks">
          <el-option label="待维修" value="repairing" />
          <el-option label="已完成" value="completed" />
        </el-select>
      </div>

      <el-table
          v-loading="loading"
          :data="taskList"
          border
          style="width: 100%"
          row-key="id"
      >
        <el-table-column prop="orderNo" label="工单号" width="180" />
        <el-table-column
            label="车辆信息"
            min-width="220"
            :formatter="formatVehicleInfo"
        />
        <el-table-column prop="problemDesc" label="问题描述" :show-overflow-tooltip="true" />
        <el-table-column prop="estimatedAmount" label="预估金额" width="120">
          <template #default="scope">
            ¥ {{ scope.row.estimatedAmount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
                type="success"
                size="small"
                icon="View"
                @click="viewTaskDetail(scope.row)"
            >
              查看详情
            </el-button>
            <el-button
                v-if="scope.row.status === 'repairing'"
                type="primary"
                size="small"
                icon="Check"
                @click="completeTask(scope.row)"
            >
              完成维修
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="empty-state" v-if="!loading && taskList.length === 0">
        <el-empty description="暂无任务" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Wrench } from '@element-plus/icons-vue'
import repairOrderApi from '@/api/repairOrder' // 假设API文件叫这个名字

const router = useRouter()
const loading = ref(true)
const taskList = ref([])
const statusFilter = ref('repairing') // 默认看待维修任务

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    // 调用你刚刚在后端新增的接口
    const response = await repairOrderApi.getPendingRepairOrders(statusFilter.value)
    if (response.code === 200) {
      taskList.value = response.data || []
    } else {
      ElMessage.error(response.msg || '获取任务列表失败')
    }
  } catch (error) {
    console.error('加载任务列表失败:', error)
    ElMessage.error('网络错误，获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 查看任务详情
const viewTaskDetail = (task) => {
  // 后续可以跳转到一个详情页，目前先弹窗提示
  ElMessage.info(`正在查看工单号 ${task.orderNo} 的详情...`)
  // router.push({ name: 'MechanicTaskDetail', query: { id: task.id } })
}

// 完成维修
const completeTask = async (task) => {
  try {
    await ElMessageBox.confirm(
        `确认已完成工单号 ${task.orderNo} 的维修工作吗？`,
        '提示',
        { type: 'warning' }
    )

    // 调用后端接口更新工单状态为 'completed'
    // const response = await repairOrderApi.completeRepair(task.id)
    // if (response.code === 200) {
    //   ElMessage.success('维修完成！')
    //   loadTasks() // 刷新列表
    // } else {
    //   ElMessage.error(response.msg || '操作失败')
    // }

    // 暂时先用前端模拟
    task.status = 'completed';
    ElMessage.success('维修完成！状态已更新。')
    loadTasks()

  } catch (error) {
    if (error.type !== 'cancel') {
      console.error('完成维修失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 格式化方法
const formatVehicleInfo = (row) => {
  return row.vehicle ? `${row.vehicle.brand || ''} ${row.vehicle.model || ''}（${row.vehicle.plateNumber || ''}）` : '车辆信息未关联';
}
const formatStatus = (status) => {
  const map = { 'repairing': '待维修', 'completed': '已完成' }
  return map[status] || status
}
const statusTagType = (status) => {
  const map = { 'repairing': 'warning', 'completed': 'success' }
  return map[status] || 'info'
}

// 页面加载时获取数据
onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.mechanic-task-container {
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.task-card {
  padding: 0;
}
.filter-bar {
  padding: 20px 20px 0;
}
.empty-state {
  padding: 50px 0;
  text-align: center;
}
</style>