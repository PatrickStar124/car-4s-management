<!-- src/views/StaffCenter.vue -->
<template>
  <div class="staff-center">
    <h1>欢迎来到员工中心</h1>
    <p>您已成功登录！</p>
    <div class="user-info">
      <p><strong>姓名：</strong> {{ userName }}</p>
      <p><strong>角色：</strong> {{ userRoleName }}</p>
    </div>

    <div class="role-actions" v-if="userRole">
      <h2>工作台</h2>
      <div class="action-buttons">
        <!-- 服务顾问 -->
        <router-link to="/service/appointment-manage" v-if="userRole === 'service'">
          <el-button type="primary" size="large" :icon="Calendar">预约管理</el-button>
        </router-link>

        <!-- 维修技师 -->
        <router-link to="/mechanic/task-list" v-if="userRole === 'mechanic'">
          <el-button type="success" size="large" :icon="Wrench">我的任务</el-button>
        </router-link>

        <!-- 仓库管理员 -->
        <router-link to="/warehouse/stock-management" v-if="userRole === 'warehouse'">
          <el-button type="warning" size="large" :icon="Box">配件管理</el-button>
        </router-link>

        <!-- 管理员 (占位符) -->
        <router-link to="/placeholder/admin" v-if="userRole === 'admin'">
          <el-button type="danger" size="large" :icon="User">用户管理</el-button>
        </router-link>
      </div>
    </div>

    <div class="actions">
      <button @click="goToHome" class="btn">返回首页</button>
      <button @click="logout" class="btn logout-btn">退出登录</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus' // 引入 ElMessage
import { Calendar, Wrench, Box, User } from '@element-plus/icons-vue'

const router = useRouter()

// ========== 关键修改：统一从 'user' 读取用户信息 ==========
const getUserInfo = () => {
  try {
    const userStr = localStorage.getItem('user') // <-- 修改点 1
    return userStr ? JSON.parse(userStr) : {}
  } catch (error) {
    console.error('解析用户信息失败:', error)
    return {}
  }
}

const userName = computed(() => {
  const userInfo = getUserInfo()
  return userInfo.name || userInfo.no || '员工'
})

const userRoleName = computed(() => {
  const userInfo = getUserInfo()
  const roleMap = {
    'service': '服务顾问',
    'mechanic': '维修技师',
    'warehouse': '仓库管理员',
    'admin': '管理员'
  }
  return roleMap[userInfo.role] || userInfo.role || '员工'
})

const userRole = computed(() => {
  const userInfo = getUserInfo()
  return userInfo.role
})

const goToHome = () => {
  router.push('/home')
}

const logout = () => {
  if (confirm('确定要退出登录吗？')) {
    // 清除所有相关的本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('user') // <-- 修改点 2：确保清除的是 'user'
    localStorage.removeItem('rememberedUsername') // 也清除记住的用户名

    ElMessage.success('已成功退出登录') // 使用 Element Plus 的消息提示
    router.push('/login')
  }
}
</script>

<style scoped>
.staff-center {
  padding: 40px 20px;
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

h2 {
  color: #444;
  margin: 30px 0 20px;
}

.user-info {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin: 30px 0;
  text-align: left;
}

.role-actions {
  margin: 40px 0;
}
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

.btn {
  padding: 10px 25px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}

.btn {
  background: #4299e1;
  color: white;
}

.btn:hover {
  background: #3182ce;
}

.logout-btn {
  background: #f5222d;
}

.logout-btn:hover {
  background: #ff4d4f;
}
</style>