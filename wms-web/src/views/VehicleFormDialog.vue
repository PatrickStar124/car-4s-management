<template>
  <el-dialog
      v-model="visible"
      :title="mode === 'add' ? '添加车辆' : '编辑车辆'"
      width="600px"
      @close="handleClose"
  >
    <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
    >
      <el-form-item label="车牌号" prop="plateNumber">
        <el-input v-model="form.plateNumber" placeholder="请输入车牌号" />
      </el-form-item>

      <el-form-item label="车架号" prop="vin">
        <el-input v-model="form.vin" placeholder="请输入车架号" />
      </el-form-item>

      <el-form-item label="品牌" prop="brand">
        <el-input v-model="form.brand" placeholder="请输入品牌" />
      </el-form-item>

      <el-form-item label="型号" prop="model">
        <el-input v-model="form.model" placeholder="请输入型号" />
      </el-form-item>

      <el-form-item label="颜色" prop="color">
        <el-input v-model="form.color" placeholder="请输入颜色" />
      </el-form-item>

      <el-form-item label="车主ID" prop="ownerId">
        <el-input
            v-model="form.ownerId"
            type="number"
            placeholder="请输入车主ID"
        />
      </el-form-item>

      <el-form-item label="购买日期" prop="purchaseDate">
        <el-date-picker
            v-model="form.purchaseDate"
            type="date"
            placeholder="选择购买日期"
            style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="当前里程" prop="mileage">
        <el-input
            v-model="form.mileage"
            type="number"
            placeholder="请输入里程"
        >
          <template #append>km</template>
        </el-input>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确认
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, defineEmits, defineProps } from 'vue'
import { ElMessage } from 'element-plus'
import { addVehicle, updateVehicle } from '@/api/vehicle'

const props = defineProps({
  modelValue: Boolean,
  vehicle: Object,
  mode: String // 'add' 或 'edit'
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const form = ref({
  id: null,
  plateNumber: '',
  vin: '',
  brand: '',
  model: '',
  ownerId: '',
  purchaseDate: '',
  mileage: '',
  color: ''
})

// 表单验证规则
const rules = {
  plateNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' }
  ],
  vin: [
    { required: true, message: '请输入车架号', trigger: 'blur' }
  ],
  brand: [
    { required: true, message: '请输入品牌', trigger: 'blur' }
  ],
  ownerId: [
    { required: true, message: '请输入车主ID', trigger: 'blur' }
  ]
}

// 监听visible变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.vehicle) {
    form.value = { ...props.vehicle }
  } else if (val) {
    resetForm()
  }
})

// 监听visible变化并通知父组件
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 重置表单
const resetForm = () => {
  form.value = {
    id: null,
    plateNumber: '',
    vin: '',
    brand: '',
    model: '',
    ownerId: '',
    purchaseDate: '',
    mileage: '',
    color: ''
  }
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const submitData = {
      ...form.value,
      mileage: form.value.mileage ? parseInt(form.value.mileage) : 0
    }

    let res
    if (props.mode === 'add') {
      res = await addVehicle(submitData)
    } else {
      res = await updateVehicle(submitData)
    }

    if (res.code === 200) {
      ElMessage.success(props.mode === 'add' ? '添加成功' : '更新成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('表单提交失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* 样式可以留空，使用默认样式 */
</style>