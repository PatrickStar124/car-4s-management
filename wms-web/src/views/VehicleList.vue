<!-- src/views/VehicleList.vue -->
<template>
  <div class="vehicle-list-container">
    <!-- 标题区域 -->
    <div class="page-header">
      <h2>{{ pageTitle }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddVehicle" v-if="userRole !== 'owner'">
          <i class="el-icon-plus"></i>添加车辆
        </el-button>
        <el-button type="success" @click="refreshList">
          <i class="el-icon-refresh"></i>刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索区域（员工使用） -->
    <div class="search-area" v-if="userRole !== 'owner'">
      <el-input
          v-model="searchQuery"
          placeholder="搜索车牌号、品牌、型号"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          style="width: 300px"
      >
        <template #prefix>
          <i class="el-icon-search"></i>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch" style="margin-left: 10px">
        搜索
      </el-button>
    </div>

    <!-- 车辆列表 -->
    <el-table
        :data="filteredVehicles"
        v-loading="loading"
        stripe
        style="width: 100%"
        @row-click="handleRowClick"
    >
      <el-table-column prop="plateNumber" label="车牌号" width="120" fixed />
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="model" label="型号" width="120" />
      <el-table-column prop="vin" label="车架号" width="180" show-overflow-tooltip />
      <el-table-column prop="color" label="颜色" width="80" />
      <el-table-column prop="purchaseDate" label="购买日期" width="120">
        <template #default="{ row }">
          {{ formatDate(row.purchaseDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="mileage" label="里程(km)" width="100">
        <template #default="{ row }">
          {{ row.mileage || '-' }}
        </template>
      </el-table-column>

      <!-- 车主信息列（员工可见） -->
      <el-table-column prop="owner.name" label="车主" width="120" v-if="userRole !== 'owner'">
        <template #default="{ row }">
          {{ row.owner?.name || row.owner?.no || '-' }}
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <!-- 车主操作 -->
          <template v-if="userRole === 'owner'">
            <el-button type="primary" size="small" @click.stop="handleAppointment(row)">
              <i class="el-icon-date"></i>预约
            </el-button>
            <el-button type="info" size="small" @click.stop="handleViewHistory(row)">
              <i class="el-icon-tickets"></i>历史
            </el-button>
          </template>

          <!-- 员工操作 -->
          <template v-else>
            <el-button type="primary" size="small" @click.stop="handleEdit(row)">
              <i class="el-icon-edit"></i>编辑
            </el-button>
            <el-button type="info" size="small" @click.stop="handleViewHistory(row)">
              <i class="el-icon-tickets"></i>详情
            </el-button>
            <el-button type="danger" size="small" @click.stop="handleDelete(row)">
              <i class="el-icon-delete"></i>删除
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页（员工使用） -->
    <div class="pagination-area" v-if="userRole !== 'owner' && vehicles.length > 0">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="vehicles.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 空状态 -->
    <el-empty
        v-if="!loading && vehicles.length === 0"
        :description="userRole === 'owner' ? '暂无车辆，请添加车辆后预约' : '暂无车辆数据'"
    >
      <el-button type="primary" @click="handleAddVehicle" v-if="userRole !== 'owner'">
        添加车辆
      </el-button>
    </el-empty>

    <!-- 预约对话框 -->
    <VehicleFormDialog
        v-if="showAppointmentDialog"
        :vehicle="selectedVehicle"
        :visible="showAppointmentDialog"
        @close="handleDialogClose"
        @success="handleAppointmentSuccess"
        @update:visible="handleDialogVisibleChange"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import VehicleFormDialog from './VehicleFormDialog.vue'

export default {
  name: 'VehicleList',

  components: {
    VehicleFormDialog
  },

  setup() {
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    const vehicles = ref([])
    const searchQuery = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const showAppointmentDialog = ref(false)
    const selectedVehicle = ref(null)

    // 计算属性
    const userRole = computed(() => store.state.user.userInfo.role || '')
    const userId = computed(() => store.state.user.userInfo.id || '')

    const pageTitle = computed(() => {
      return userRole.value === 'owner' ? '我的车辆' : '车辆管理'
    })

    // 过滤后的车辆列表
    const filteredVehicles = computed(() => {
      let result = vehicles.value

      // 车主只能看到自己的车
      if (userRole.value === 'owner') {
        result = result.filter(v => v.ownerId === userId.value)
      }

      // 搜索过滤
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(v =>
            (v.plateNumber && v.plateNumber.toLowerCase().includes(query)) ||
            (v.brand && v.brand.toLowerCase().includes(query)) ||
            (v.model && v.model.toLowerCase().includes(query)) ||
            (v.vin && v.vin.toLowerCase().includes(query))
        )
      }

      // 分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return result.slice(start, end)
    })

    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleDateString()
    }

    // 加载车辆列表
    const loadVehicles = async () => {
      loading.value = true
      try {
        let response

        if (userRole.value === 'owner') {
          // 车主：获取自己的车辆
          // 这里需要导入 vehicleApi 或使用全局变量
          response = await window.vehicleApi?.getMyVehicles(userId.value) ||
              { code: 200, data: [] } // 临时模拟
        } else {
          // 员工：获取所有车辆
          response = await window.vehicleApi?.getAllVehicles() ||
              { code: 200, data: [] } // 临时模拟
        }

        if (response.code === 200) {
          vehicles.value = response.data || []
        } else {
          ElMessage.error(response.msg || '获取车辆列表失败')
          vehicles.value = []
        }
      } catch (error) {
        console.error('加载车辆失败:', error)
        vehicles.value = []
      } finally {
        loading.value = false
      }
    }

    // 刷新列表
    const refreshList = () => {
      loadVehicles()
      ElMessage.success('刷新成功')
    }

    // 搜索
    const handleSearch = () => {
      currentPage.value = 1
    }

    // 分页大小改变
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }

    // 当前页改变
    const handleCurrentChange = (page) => {
      currentPage.value = page
    }

    // 行点击事件
    const handleRowClick = (row) => {
      if (userRole.value !== 'owner') {
        // 员工点击查看详情
        router.push(`/vehicle/history/${row.id}`)
      }
    }

    // 预约保养
    const handleAppointment = (vehicle) => {
      selectedVehicle.value = vehicle
      showAppointmentDialog.value = true
    }

    // 查看历史
    const handleViewHistory = (vehicle) => {
      router.push(`/vehicle/history/${vehicle.id}`)
    }

    // 编辑车辆（员工）
    const handleEdit = () => {
      ElMessage.info('编辑功能开发中')
    }

    // 删除车辆（员工）
    const handleDelete = async (vehicle) => {
      try {
        await ElMessageBox.confirm(
            `确定要删除车辆 ${vehicle.plateNumber} 吗？`,
            '删除确认',
            {
              type: 'warning',
              confirmButtonText: '确定',
              cancelButtonText: '取消'
            }
        )

        const response = await window.vehicleApi?.deleteVehicle(vehicle.id) ||
            { code: 200 } // 临时模拟
        if (response.code === 200) {
          ElMessage.success('删除成功')
          loadVehicles()
        } else {
          ElMessage.error(response.msg || '删除失败')
        }
      } catch (error) {
        // 用户取消删除
      }
    }

    // 添加车辆
    const handleAddVehicle = () => {
      if (userRole.value === 'owner') {
        // 车主添加车辆需要专门的弹窗
        ElMessage.info('添加车辆功能开发中')
      } else {
        ElMessage.info('员工添加车辆功能开发中')
      }
    }

    // 预约成功回调
    const handleAppointmentSuccess = () => {
      showAppointmentDialog.value = false
      ElMessage.success('预约成功！')
    }

    // 对话框关闭
    const handleDialogClose = () => {
      showAppointmentDialog.value = false
    }

    // 对话框 visible 变化
    const handleDialogVisibleChange = (value) => {
      showAppointmentDialog.value = value
    }

    // 组件挂载时加载数据
    onMounted(() => {
      loadVehicles()
    })

    // 暴露给模板的方法
    return {
      loading,
      vehicles,
      searchQuery,
      currentPage,
      pageSize,
      showAppointmentDialog,
      selectedVehicle,
      userRole,
      userId,
      pageTitle,
      filteredVehicles,
      formatDate,
      refreshList,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      handleRowClick,
      handleAppointment,
      handleViewHistory,
      handleEdit,
      handleDelete,
      handleAddVehicle,
      handleAppointmentSuccess,
      handleDialogClose,
      handleDialogVisibleChange
    }
  }
}
</script>

<style scoped>
.vehicle-list-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

:deep(.el-table) {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-button + .el-button) {
  margin-left: 8px;
}
</style>