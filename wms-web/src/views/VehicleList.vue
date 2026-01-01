<template>
  <div class="vehicle-list-container">
    <div class="header">
      <h2>车辆管理</h2>
      <el-button type="primary" @click="handleAdd" icon="el-icon-plus">
        添加车辆
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="车牌号">
          <el-input
              v-model="searchForm.plateNumber"
              placeholder="请输入车牌号"
              clearable
          />
        </el-form-item>
        <el-form-item label="车主">
          <el-input
              v-model="searchForm.ownerName"
              placeholder="请输入车主姓名"
              clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 车辆列表 -->
    <el-card>
      <el-table :data="vehicleList" v-loading="loading">
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="color" label="颜色" width="80" />
        <el-table-column prop="vin" label="车架号" />
        <el-table-column prop="owner.name" label="车主" width="100" />
        <el-table-column prop="mileage" label="里程(km)" width="100" />
        <el-table-column prop="purchaseDate" label="购买日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.purchaseDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
                size="small"
                type="primary"
                @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
                size="small"
                type="info"
                @click="handleViewHistory(row.id)"
            >
              历史
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑车辆对话框 -->
    <VehicleFormDialog
        v-model="dialogVisible"
        :vehicle="currentVehicle"
        :mode="formMode"
        @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getVehicleList,
  deleteVehicle
} from '@/api/vehicle'
// 修改这一行：从 './components/VehicleFormDialog.vue' 改为 './VehicleFormDialog.vue'
import VehicleFormDialog from './VehicleFormDialog.vue'

const router = useRouter()
const loading = ref(false)
const vehicleList = ref([])
const currentVehicle = ref(null)
const dialogVisible = ref(false)
const formMode = ref('add') // 'add' 或 'edit'

// 搜索表单
const searchForm = ref({
  plateNumber: '',
  ownerName: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取车辆列表
const fetchVehicles = async () => {
  try {
    loading.value = true
    const res = await getVehicleList()
    if (res.code === 200) {
      vehicleList.value = res.data
      total.value = res.data.length
    }
  } catch (error) {
    console.error('获取车辆列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  // 实现搜索逻辑
  console.log('搜索:', searchForm.value)
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    plateNumber: '',
    ownerName: ''
  }
  fetchVehicles()
}

// 添加车辆
const handleAdd = () => {
  formMode.value = 'add'
  currentVehicle.value = null
  dialogVisible.value = true
}

// 编辑车辆
const handleEdit = (vehicle) => {
  formMode.value = 'edit'
  currentVehicle.value = { ...vehicle }
  dialogVisible.value = true
}

// 查看维修历史
const handleViewHistory = (vehicleId) => {
  router.push(`/vehicle/history/${vehicleId}`)
}

// 删除车辆
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这辆车吗？', '提示', {
      type: 'warning'
    })

    const res = await deleteVehicle(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchVehicles()
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  dialogVisible.value = false
  fetchVehicles()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchVehicles()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchVehicles()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
  fetchVehicles()
})
</script>

<style scoped>
.vehicle-list-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>