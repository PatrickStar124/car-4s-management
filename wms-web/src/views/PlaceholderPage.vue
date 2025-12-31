<template>
  <div class="placeholder-page">
    <h1>{{ pageTitle }}</h1>
    <div class="placeholder-content">
      <div class="placeholder-icon">🚧</div>
      <h3>功能正在开发中</h3>
      <p>请先完成登录注册功能的测试</p>
      <div class="placeholder-actions">
        <button @click="goToHome" class="action-btn">返回首页</button>
        <button @click="goToLogin" class="action-btn primary-btn">去登录</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlaceholderPage',
  computed: {
    pageTitle() {
      const routeName = this.$route.name
      const titleMap = {
        'WorkOrderPage': '维修工单管理',
        'WorkOrderAdd': '创建维修工单',
        'PartStockPage': '配件库存管理',
        'VehicleList': '我的车辆',
        'StaffCenter': '员工中心'
      }
      return titleMap[routeName] || '功能页面'
    }
  },
  methods: {
    goToHome() {
      this.$router.push('/home')
    },
    goToLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.placeholder-page {
  padding: 40px 20px;
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.placeholder-page h1 {
  color: #333;
  margin-bottom: 30px;
}

.placeholder-content {
  background: white;
  padding: 60px 40px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.placeholder-icon {
  font-size: 80px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.placeholder-content h3 {
  color: #333;
  margin-bottom: 15px;
}

.placeholder-content p {
  color: #666;
  margin-bottom: 30px;
}

.placeholder-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.action-btn {
  padding: 10px 25px;
  background: #f7fafc;
  color: #4299e1;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}

.action-btn:hover {
  background: #edf2f7;
}

.primary-btn {
  background: #4299e1;
  color: white;
  border-color: #4299e1;
}

.primary-btn:hover {
  background: #3182ce;
}
</style>