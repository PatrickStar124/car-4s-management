<!-- src/views/VehicleFormDialog.vue - 最终解决方案 -->
<template>
  <el-dialog
      :model-value="visible"
      :title="dialogTitle"
      width="600px"
      @close="handleClose"
      @update:model-value="handleUpdateVisible"
  >
    <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        @submit.prevent="handleSubmit"
    >
      <!-- 车辆信息 -->
      <div class="vehicle-info-section" v-if="vehicle">
        <div class="info-item">
          <span class="label">车牌号：</span>
          <span class="value">{{ vehicle.plateNumber }}</span>
        </div>
        <div class="info-item">
          <span class="label">车型：</span>
          <span class="value">{{ vehicle.brand }} {{ vehicle.model }}</span>
        </div>
        <div class="info-item">
          <span class="label">车架号：</span>
          <span class="value">{{ vehicle.vin }}</span>
        </div>
      </div>

      <!-- 服务类型 -->
      <el-form-item label="服务类型" prop="serviceType">
        <el-select
            v-model="formData.serviceType"
            placeholder="请选择服务类型"
            style="width: 100%"
        >
          <el-option label="保养" value="maintenance" />
          <el-option label="维修" value="repair" />
          <el-option label="检测" value="inspection" />
          <el-option label="美容" value="beauty" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>

      <!-- 预约时间 -->
      <el-form-item label="预约时间" required>
        <div class="time-select-group">
          <el-form-item prop="appointmentDate" style="display: inline-block; width: 48%; margin-right: 4%">
            <el-date-picker
                v-model="formData.appointmentDate"
                type="date"
                placeholder="选择日期"
                :disabled-date="disabledDate"
                style="width: 100%"
                @change="handleDateChange"
            />
          </el-form-item>
          <el-form-item prop="appointmentTime" style="display: inline-block; width: 48%">
            <el-time-select
                v-model="formData.appointmentTime"
                :disabled="!formData.appointmentDate"
                :start="startTime"
                step="00:30"
                end="17:00"
                placeholder="选择时间"
                style="width: 100%"
            />
          </el-form-item>
        </div>
      </el-form-item>

      <!-- 服务时长 -->
      <el-form-item label="服务时长" prop="duration">
        <el-radio-group v-model="formData.duration">
          <el-radio :label="30">30分钟</el-radio>
          <el-radio :label="60">1小时</el-radio>
          <el-radio :label="90">1.5小时</el-radio>
          <el-radio :label="120">2小时</el-radio>
          <el-radio :label="180">3小时</el-radio>
          <el-radio :label="240">4小时</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 备注 -->
      <el-form-item label="备注" prop="remark">
        <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息，如：需要更换机油、检查刹车片等"
            maxlength="500"
            show-word-limit
        />
      </el-form-item>

      <!-- 温馨提示 -->
      <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
      >
        <ul style="margin: 0; padding-left: 20px">
          <li>请提前30分钟到达门店</li>
          <li>如需取消预约，请提前2小时操作</li>
          <li>预约成功后，服务顾问会与您确认</li>
        </ul>
      </el-alert>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose" :disabled="submitting">取消</el-button>
        <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitting"
            :disabled="!formData.appointmentDate || !formData.appointmentTime"
        >
          提交预约
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
/* eslint-disable vue/no-side-effects-in-computed-properties */
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'

export default {
  name: 'VehicleFormDialog',

  props: {
    visible: {
      type: Boolean,
      required: true
    },
    vehicle: {
      type: Object,
      default: () => ({})
    }
  },

  emits: ['close', 'success', 'update:visible'],

  setup(props, { emit }) {
    const store = useStore()
    const formRef = ref(null)
    const submitting = ref(false)

    // 表单数据
    const formData = ref({
      serviceType: 'maintenance',
      appointmentDate: '',
      appointmentTime: '',
      duration: 60,
      remark: ''
    })

    // 表单验证规则
    const formRules = {
      serviceType: [
        { required: true, message: '请选择服务类型', trigger: 'change' }
      ],
      appointmentDate: [
        { required: true, message: '请选择预约日期', trigger: 'change' }
      ],
      appointmentTime: [
        { required: true, message: '请选择预约时间', trigger: 'change' }
      ],
      duration: [
        { required: true, message: '请选择服务时长', trigger: 'change' }
      ]
    }

    // 计算属性
    const dialogTitle = computed(() => {
      return `预约保养 - ${props.vehicle?.plateNumber || ''}`
    })

    const startTime = computed(() => {
      if (!formData.value.appointmentDate) return '08:00'

      const now = new Date()
      const selectedDate = new Date(formData.value.appointmentDate)
      const isToday = selectedDate.toDateString() === now.toDateString()

      if (!isToday) return '08:00'

      const hours = now.getHours()
      const minutes = now.getMinutes()

      // 检查是否超过营业时间
      if (hours >= 17) {
        // 今天已过营业时间
        return '08:00'
      }

      // 返回下一个半小时时间点
      if (minutes < 30) {
        return `${hours.toString().padStart(2, '0')}:30`
      } else {
        return `${(hours + 1).toString().padStart(2, '0')}:00`
      }
    })

    // 禁止选择过去的日期
    const disabledDate = (date) => {
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      return date < today
    }

    // 日期变化处理
    const handleDateChange = () => {
      const now = new Date()
      const selectedDate = new Date(formData.value.appointmentDate)
      const isToday = selectedDate.toDateString() === now.toDateString()
      const hours = now.getHours()

      // 日期变化时清空时间
      formData.value.appointmentTime = ''

      // 检查是否超过营业时间
      if (isToday && hours >= 17) {
        ElMessage.warning('今天已过营业时间，请选择其他日期')
        formData.value.appointmentDate = ''
      }
    }

    // 检查时间冲突
    const checkTimeConflict = async () => {
      const times = calculateTimes()
      if (!times) return false

      try {
        // 模拟 API 调用
        return await mockCheckTimeConflict()
      } catch (error) {
        console.error('检查时间冲突失败:', error)
        return false
      }
    }

    // 模拟检查时间冲突
    const mockCheckTimeConflict = async () => {
      return new Promise((resolve) => {
        setTimeout(() => {
          // 模拟90%的成功率
          const success = Math.random() > 0.1
          resolve(success)
        }, 500)
      })
    }

    // 计算时间
    const calculateTimes = () => {
      const { appointmentDate, appointmentTime, duration } = formData.value
      if (!appointmentDate || !appointmentTime) return null

      const date = new Date(appointmentDate)
      const [hours, minutes] = appointmentTime.split(':')

      date.setHours(parseInt(hours), parseInt(minutes), 0, 0)

      const startTime = new Date(date)
      const endTime = new Date(date.getTime() + duration * 60000)

      return {
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString()
      }
    }

    // 模拟创建预约
    const mockCreateAppointment = async (appointmentData) => {
      return new Promise((resolve) => {
        setTimeout(() => {
          const appointmentNo = 'AP' + Date.now()
          resolve({
            code: 200,
            data: {
              ...appointmentData,
              id: Date.now(),
              appointmentNo,
              status: 'pending',
              createTime: new Date().toISOString()
            },
            msg: '预约创建成功'
          })
        }, 1000)
      })
    }

    // 提交表单
    const handleSubmit = async () => {
      if (!formRef.value) return

      try {
        await formRef.value.validate()
      } catch (error) {
        return
      }

      // 检查时间冲突
      const isAvailable = await checkTimeConflict()
      if (!isAvailable) {
        ElMessage.warning('该时间段已有预约，请选择其他时间')
        return
      }

      submitting.value = true

      try {
        const times = calculateTimes()
        if (!times) {
          throw new Error('时间计算错误')
        }

        const appointmentData = {
          vehicleId: props.vehicle.id,
          ownerId: store.state.user.userInfo.id,
          serviceType: formData.value.serviceType,
          startTime: times.startTime,
          endTime: times.endTime,
          remark: formData.value.remark || undefined
        }

        // 模拟 API 调用
        const response = await mockCreateAppointment(appointmentData)

        if (response.code === 200) {
          ElMessage.success('预约成功！服务顾问会尽快与您确认')
          emit('success', response.data)
          resetForm()
          emit('close')
        } else {
          throw new Error(response.msg || '预约失败')
        }
      } catch (error) {
        console.error('预约失败:', error)
        ElMessage.error(error.message || '提交预约失败')
      } finally {
        submitting.value = false
      }
    }

    // 重置表单
    const resetForm = () => {
      formData.value = {
        serviceType: 'maintenance',
        appointmentDate: '',
        appointmentTime: '',
        duration: 60,
        remark: ''
      }
      if (formRef.value) {
        formRef.value.clearValidate()
      }
    }

    // 关闭对话框
    const handleClose = () => {
      if (!submitting.value) {
        resetForm()
        emit('close')
      }
    }

    // 更新 visible 状态
    const handleUpdateVisible = (value) => {
      emit('update:visible', value)
    }

    // 监听visible变化
    watch(() => props.visible, (newVal) => {
      if (newVal) {
        nextTick(() => {
          resetForm()
        })
      }
    })

    // 暴露给模板的方法
    return {
      formRef,
      formData,
      formRules,
      submitting,
      dialogTitle,
      startTime,
      disabledDate,
      handleDateChange,
      handleSubmit,
      handleClose,
      handleUpdateVisible
    }
  }
}
</script>

<style scoped>
.vehicle-info-section {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #606266;
  min-width: 80px;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}

.time-select-group {
  display: flex;
  justify-content: space-between;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-alert__description) {
  font-size: 12px;
}
</style>