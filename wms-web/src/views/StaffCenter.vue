<template>
  <div class="staff-center">
    <h1>欢迎来到员工中心</h1>
    <p>您已成功登录！</p>
    <div class="user-info">
      <p><strong>姓名：</strong> {{ userName }}</p>
      <p><strong>角色：</strong> {{ userRoleName }}</p>
    </div>
    <div class="actions">
      <button @click="goToHome" class="btn">返回首页</button>
      <button @click="logout" class="btn logout-btn">退出登录</button>
    </div>
  </div>
</template>

<script>
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

.user-info {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin: 30px 0;
  text-align: left;
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