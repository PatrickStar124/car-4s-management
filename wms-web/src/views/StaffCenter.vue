<template>
  <div class="staff-center">
    <h1>欢迎来到员工中心</h1>
    <p>您已成功登录！</p>
    <div class="user-info">
      <p><strong>姓名：</strong> {{ userName }}</p>
      <p><strong>角色：</strong> {{ userRoleName }}</p>
    </div>

    <!-- =================== 新增：角色功能区 (START) =================== -->
    <div class="role-actions" v-if="userRole">
      <h2>工作台</h2>
      <div class="action-buttons">
        <!-- 服务顾问 -->
        <router-link to="/service/appointment-manage" v-if="userRole === 'service'">
          <el-button type="primary" size="large" icon="Calendar">预约管理</el-button>
        </router-link>

        <!-- 维修技师 -->
        <router-link to="/mechanic/task-list" v-if="userRole === 'mechanic'">
          <el-button type="success" size="large" icon="Wrench">我的任务</el-button>
        </router-link>

        <!-- 仓库管理员 (占位符) -->
        <router-link to="/placeholder/warehouse" v-if="userRole === 'warehouse'">
          <el-button type="warning" size="large" icon="Box">配件管理</el-button>
        </router-link>

        <!-- 管理员 (占位符) -->
        <router-link to="/placeholder/admin" v-if="userRole === 'admin'">
          <el-button type="danger" size="large" icon="User">用户管理</el-button>
        </router-link>
      </div>
    </div>
    <!-- =================== 新增：角色功能区 (END) =================== -->

    <div class="actions">
      <button @click="goToHome" class="btn">返回首页</button>
      <button @click="logout" class="btn logout-btn">退出登录</button>
    </div>
  </div>
</template>

<script>
// 确保你已经安装并全局注册了 Element Plus 图标
// 如果没有，请在 main.js 中添加：
// import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
//   app.component(key, component)
// }

export default {
  name: 'StaffCenter',
  computed: {
    userName() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return userInfo.name || '员工'
    },
    userRoleName() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const roleMap = {
        'service': '服务顾问',
        'mechanic': '维修技师',
        'warehouse': '仓库管理员',
        'admin': '管理员'
      }
      return roleMap[userInfo.role] || userInfo.role || '员工'
    },
    // ✅ 新增：获取原始角色值，用于判断显示哪个按钮
    userRole() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return userInfo.role
    }
  },
  methods: {
    goToHome() {
      this.$router.push('/home')
    },
    logout() {
      if (confirm('确定要退出登录吗？')) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userType')
        this.$router.push('/login')
      }
    }
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

/* ✅ 新增：角色功能区样式 */
.role-actions {
  margin: 40px 0;
}
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap; /* 允许按钮换行 */
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