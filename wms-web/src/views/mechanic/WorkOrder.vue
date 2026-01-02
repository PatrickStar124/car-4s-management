<!-- src/views/mechanic/WorkOrder.vue -->
<template>
  <div class="mechanic-workorder-container">
    <h1>我的维修任务</h1>
    <el-table :data="workorderList" border style="width: 100%" v-loading="loading" row-key="id">
      <el-table-column prop="orderNo" label="工单号" width="180" />
      <el-table-column prop="vehicle.licensePlate" label="车牌号" width="120" />
      <el-table-column prop="problemDesc" label="问题描述" show-overflow-tooltip min-width="200" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'repairing' ? 'warning' : 'success'">
            {{ statusMap[scope.row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
          <el-button
              v-if="scope.row.status === 'repairing'"
              type="success"
              size="small"
              @click="handleComplete(scope.row)"
          >
            标记完成
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import repairOrderApi from '@/api/repairOrder'

const router = useRouter()
const loading = ref(true)
const workorderList = ref([])

// 状态映射
const statusMap = {
  'pending': '待接车',
  'inspecting': '检查中',
  'quote_pending': '待确认报价',
  'repairing': '维修中',
  'completed': '已完成',
  'delivered': '已交车'
}

// 假设从localStorage获取当前登录技师的信息
const currentUser = JSON.parse(localStorage.getItem('userInfo') || '{}');
const mechanicId = currentUser.id;

const loadWorkorders = async () => {
  loading.value = true
  try {
    // 调用接口获取所有 'repairing' 状态的工单
    const res = await repairOrderApi.getOrdersByStatus('repairing')
    if (res.code === 200) {
      // 过滤出分配给自己的工单
      workorderList.value = res.data.filter(order => order.mechanicId === mechanicId);
    } else {
      ElMessage.error(res.msg || '获取工单失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络错误，获取工单失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  // 可以跳转到一个详情页，这里先用路由参数示例
  // router.push(`/mechanic/order-detail/${row.id}`);
  ElMessage.info(`查看工单【${row.orderNo}】的详情`);
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要将工单【${row.orderNo}】标记为已完成吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 为了简化，我们假设实际金额等于预估金额
    const actualAmount = row.estimatedAmount || 0;

    // 调用 completeOrder 接口
    const res = await repairOrderApi.completeOrder(row.id, actualAmount)

    if (res.code === 200) {
      ElMessage.success('工单已成功标记为已完成！')
      loadWorkorders() // 刷新列表
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    // 如果用户取消，则 error 是一个对象，否则是网络错误
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作取消或发生错误')
    }
  }
}

onMounted(() => {
  loadWorkorders()
})
</script>

<style scoped>
.mechanic-workorder-container {
  padding: 20px;
}
h1 {
  margin-bottom: 20px;
}
</style>