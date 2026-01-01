<template>
  <div class="vehicle-create-container">
    <div class="page-header">
      <!-- ================ 关键修改点 ================ -->
      <!-- 1. 将 <el-icon><Truck /></el-icon> 修改为 <el-icon :icon="Truck" /> -->
      <h1><el-icon :icon="Truck" /> 添加我的车辆</h1>
      <!-- ========================================== -->
      <p class="page-desc">请填写您的车辆信息，以便进行后续的预约服务。</p>
    </div>

    <div class="form-card">
      <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="120px"
          class="vehicle-form"
      >
        <el-form-item label="车牌号码" prop="plateNumber">
          <el-input v-model="formData.plateNumber" placeholder="请输入车牌号码" />
        </el-form-item>

        <el-form-item label="车辆品牌" prop="brand">
          <el-input v-model="formData.brand" placeholder="请输入车辆品牌" />
        </el-form-item>

        <el-form-item label="车辆型号" prop="model">
          <el-input v-model="formData.model" placeholder="请输入车辆型号" />
        </el-form-item>

        <el-form-item label="车架号(VIN)" prop="vin">
          <el-input v-model="formData.vin" placeholder="请输入车架号" />
        </el-form-item>

        <el-form-item label="当前里程(km)">
          <el-input-number v-model="formData.mileage" :min="0" placeholder="请输入当前里程" />
        </el-form-item>

        <el-form-item label="备注信息">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>

      <div class="form-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button
            type="primary"
            :loading="submitting"
            @click="submitForm"
        >
          提交车辆信息
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
// 确保已导入 Truck 图标
import { Truck } from '@element-plus/icons-vue'
import vehicleApi from '@/api/vehicle'

const router = useRouter()
const store = useStore()
const formRef = ref(null)
const submitting = ref(false)

const formData = ref({
  plateNumber: '',
  brand: '',
  model: '',
  vin: '',
  mileage: null,
  remark: '',
  ownerId: store.getters['user/userId']
})

const rules = ref({
  plateNumber: [{ required: true, message: '车牌号码不能为空', trigger: 'blur' }],
  brand: [{ required: true, message: '车辆品牌不能为空', trigger: 'blur' }],
  model: [{ required: true, message: '车辆型号不能为空', trigger: 'blur' }],
  vin: [{ required: true, message: '车架号不能为空', trigger: 'blur' }],
})

const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const response = await vehicleApi.addVehicle(formData.value)

    if (response.code === 200) {
      ElMessage.success('车辆添加成功！')
      router.push({ path: '/vehicle', query: { t: Date.now() } })
    } else {
      ElMessage.error(response.message || '车辆添加失败')
    }
  } catch (error) {
    if (error.name !== 'ElForm') {
      console.error('提交失败:', error)
      ElMessage.error('提交失败，请检查网络或联系管理员')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.vehicle-create-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.page-header {
  text-align: center;
  margin-bottom: 30px;
}
.form-card {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.vehicle-form {
  margin-bottom: 20px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}
</style>