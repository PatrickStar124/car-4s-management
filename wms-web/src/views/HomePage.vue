<template>
  <div class="home-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-left">
        <div class="logo">
          <span class="logo-text">汽车4S店服务平台</span>
        </div>
      </div>

      <div class="header-actions">
        <!-- 未登录用户显示 -->
        <div v-if="!isAuthenticated" class="guest-actions">
          <button @click="goToLogin" class="nav-btn">
            用户登录
          </button>
          <button @click="goToRegister" class="nav-btn register-btn">
            车主注册
          </button>
        </div>

        <!-- 已登录用户显示 -->
        <div v-else class="user-actions">
          <div class="user-info">
            <span class="user-name">欢迎，{{ userName }}</span>
            <span class="user-role">{{ userRoleName }}</span>
            <button @click="goToCenter" class="nav-btn">
              个人中心
            </button>
            <button @click="logout" class="nav-btn logout-btn">
              退出
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- 欢迎横幅 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>汽车4S店数字化服务平台</h1>
        <p>专业汽车服务，让出行更安心</p>
        <div class="welcome-actions">
          <button v-if="!isAuthenticated" @click="goToLogin" class="welcome-btn">
            立即登录
          </button>
          <button v-if="!isAuthenticated" @click="goToRegister" class="welcome-btn primary-btn">
            注册车主
          </button>
          <button v-if="isAuthenticated && isUser" @click="goToUserCenter" class="welcome-btn">
            进入车主中心
          </button>
          <button v-if="isAuthenticated && isStaff" @click="goToStaffCenter" class="welcome-btn">
            进入员工中心
          </button>
        </div>
      </div>
    </div>

    <!-- 服务介绍 -->
    <div class="services-intro">
      <h2>我们的服务</h2>
      <div class="services-grid">
        <div class="service-item">
          <div class="service-icon">🔧</div>
          <h3>维修保养</h3>
          <p>专业团队，原厂配件</p>
        </div>
        <div class="service-item">
          <div class="service-icon">🚗</div>
          <h3>车辆检测</h3>
          <p>全面检测，安全可靠</p>
        </div>
        <div class="service-item">
          <div class="service-icon">⚡</div>
          <h3>快速服务</h3>
          <p>在线预约，节省时间</p>
        </div>
        <div class="service-item">
          <div class="service-icon">📱</div>
          <h3>进度跟踪</h3>
          <p>实时查看维修进度</p>
        </div>
      </div>
    </div>

    <!-- 登录状态提示 -->
    <div class="status-info">
      <div v-if="!isAuthenticated" class="guest-info">
        <h3>还未登录？</h3>
        <p>登录后享受完整服务：车辆管理、维修预约、进度跟踪等</p>
        <button @click="goToLogin" class="action-btn">立即登录</button>
      </div>
      <div v-else class="user-info-card">
        <h3>欢迎回来，{{ userName }}！</h3>
        <p>您现在是 {{ userRoleName }}，可以享受相应的服务</p>
        <div class="user-actions">
          <button @click="goToCenter" class="action-btn">进入个人中心</button>
          <button @click="logout" class="action-btn secondary-btn">退出登录</button>
        </div>
      </div>
    </div>

    <!-- 底部信息 -->
    <footer class="footer">
      <p>© 2024 汽车4S店数字化服务平台</p>
      <p>客服热线：400-1234-5678</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  computed: {
    isAuthenticated() {
      return !!localStorage.getItem('token')
    },
    userName() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return userInfo.name || userInfo.username || '用户'
    },
    userRoleName() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const roleMap = {
        'owner': '车主',
        'service': '服务顾问',
        'mechanic': '维修技师',
        'warehouse': '仓库管理员',
        'admin': '管理员'
      }
      return roleMap[userInfo.role] || userInfo.role || '用户'
    },
    isUser() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return userInfo.role === 'owner'
    },
    isStaff() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      return ['service', 'mechanic', 'warehouse', 'admin'].includes(userInfo.role)
    }
  },
  methods: {
    goToLogin() {
      this.$router.push('/login')
    },
    goToRegister() {
      this.$router.push('/register')
    },
    goToUserCenter() {
      this.$router.push('/user-center')
    },
    goToStaffCenter() {
      this.$router.push('/staff-center')
    },
    goToCenter() {
      if (this.isUser) {
        this.goToUserCenter()
      } else {
        this.goToStaffCenter()
      }
    },
    logout() {
      if (confirm('确定要退出登录吗？')) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userType')
        alert('已退出登录')
        window.location.reload()
      }
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  color: #333;
  cursor: pointer;
  font-size: 14px;
}

.nav-btn:hover {
  background: #f5f5f5;
}

.register-btn {
  background: #48bb78;
  color: white;
  border-color: #48bb78;
}

.logout-btn {
  background: #f5222d;
  color: white;
  border-color: #f5222d;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-weight: 500;
}

.user-role {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.welcome-section {
  text-align: center;
  padding: 60px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-content h1 {
  font-size: 36px;
  margin-bottom: 15px;
}

.welcome-content p {
  font-size: 18px;
  margin-bottom: 25px;
  opacity: 0.9;
}

.welcome-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.welcome-btn {
  padding: 12px 24px;
  border: 2px solid white;
  background: transparent;
  color: white;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
}

.welcome-btn:hover {
  background: white;
  color: #667eea;
}

.primary-btn {
  background: white;
  color: #667eea;
}

.services-intro {
  padding: 60px 30px;
  background: white;
}

.services-intro h2 {
  text-align: center;
  margin-bottom: 40px;
  color: #2c3e50;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.service-item {
  text-align: center;
  padding: 30px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  transition: all 0.3s;
}

.service-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.service-icon {
  font-size: 40px;
  margin-bottom: 15px;
}

.service-item h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.service-item p {
  color: #666;
  margin: 0;
}

.status-info {
  padding: 60px 30px;
  max-width: 800px;
  margin: 0 auto;
}

.guest-info, .user-info-card {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.guest-info h3, .user-info-card h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
}

.guest-info p, .user-info-card p {
  color: #666;
  margin-bottom: 25px;
}

.action-btn {
  padding: 12px 24px;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  margin: 0 5px;
}

.action-btn:hover {
  background: #3182ce;
}

.secondary-btn {
  background: #f7fafc;
  color: #4299e1;
  border: 1px solid #e2e8f0;
}

.secondary-btn:hover {
  background: #edf2f7;
}

.footer {
  text-align: center;
  padding: 30px;
  background: #2c3e50;
  color: white;
  margin-top: 40px;
}

.footer p {
  margin: 5px 0;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 15px;
  }

  .welcome-content h1 {
    font-size: 28px;
  }

  .welcome-actions {
    flex-direction: column;
    align-items: center;
  }

  .services-grid {
    grid-template-columns: 1fr;
  }
}
</style>