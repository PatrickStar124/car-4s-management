<template>
  <div class="user-center">
    <header class="header">
      <h1>👤 个人中心</h1>
      <div class="header-actions">
        <button @click="goHome" class="home-btn">返回首页</button>
        <button @click="goToCart" class="cart-btn">🛒 购物车</button>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </header>

    <div v-if="!user" class="not-logged-in">
      <div class="login-prompt">
        <h3>请先登录</h3>
        <p>登录后查看个人信息</p>
        <button @click="goToLogin" class="login-btn">立即登录</button>
      </div>
    </div>

    <div v-else class="user-content">
      <div class="user-sidebar">
        <div class="user-avatar">
          <div class="avatar-circle">
            {{ userAvatar }}
          </div>
          <h3>{{ user.name }}</h3>
          <p class="user-no">账号：{{ user.no }}</p>
        </div>

        <div class="sidebar-menu">
          <button
              v-for="item in menuItems"
              :key="item.id"
              :class="{ active: activeMenu === item.id }"
              @click="activeMenu = item.id"
              class="menu-item"
          >
            <span class="menu-icon">{{ item.icon }}</span>
            <span class="menu-text">{{ item.text }}</span>
          </button>
        </div>
      </div>

      <div class="user-main">
        <!-- 基本信息 -->
        <div v-if="activeMenu === 'info'" class="info-section">
          <h2>基本信息</h2>
          <div class="info-card">
            <div class="info-item">
              <label>账号</label>
              <div class="info-value">{{ user.no }}</div>
            </div>
            <div class="info-item">
              <label>姓名</label>
              <div class="info-value">{{ user.name }}</div>
            </div>
            <div class="info-item" v-if="user.phone">
              <label>手机号码</label>
              <div class="info-value">{{ user.phone }}</div>
            </div>
            <div class="info-item" v-if="user.email">
              <label>邮箱</label>
              <div class="info-value">{{ user.email }}</div>
            </div>
            <div class="info-item">
              <label>用户ID</label>
              <div class="info-value">{{ user.id }}</div>
            </div>
            <div class="info-item">
              <label>注册时间</label>
              <div class="info-value">{{ user.createTime || '未知' }}</div>
            </div>
          </div>
        </div>

        <!-- 购物车 -->
        <div v-else-if="activeMenu === 'cart'" class="cart-section">
          <h2>购物车概览</h2>
          <div v-if="cartItems.length === 0" class="empty-cart">
            <p>购物车空空如也</p>
            <button @click="goToBookList" class="browse-btn">去逛逛</button>
          </div>
          <div v-else class="cart-overview">
            <div class="cart-stats">
              <div class="stat-item">
                <div class="stat-value">{{ cartItems.length }}</div>
                <div class="stat-label">商品数量</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">¥{{ totalPrice.toFixed(2) }}</div>
                <div class="stat-label">商品总额</div>
              </div>
            </div>
            <button @click="goToCart" class="view-cart-btn">查看购物车详情</button>
          </div>
        </div>

        <!-- 修改信息 -->
        <div v-else-if="activeMenu === 'edit'" class="edit-section">
          <h2>修改信息</h2>
          <div class="edit-form">
            <div class="form-group">
              <label for="editName">姓名</label>
              <input
                  id="editName"
                  v-model="editForm.name"
                  type="text"
                  placeholder="请输入姓名"
              >
            </div>
            <div class="form-group">
              <label for="editPhone">手机号码</label>
              <input
                  id="editPhone"
                  v-model="editForm.phone"
                  type="tel"
                  placeholder="请输入手机号码"
              >
            </div>
            <div class="form-group">
              <label for="editEmail">邮箱</label>
              <input
                  id="editEmail"
                  v-model="editForm.email"
                  type="email"
                  placeholder="请输入邮箱"
              >
            </div>
            <div class="form-actions">
              <button @click="resetEdit" class="cancel-btn">取消</button>
              <button @click="saveProfile" class="save-btn" :disabled="saving">
                {{ saving ? '保存中...' : '保存修改' }}
              </button>
            </div>
            <div class="edit-note">
              <p>注意：账号信息不可修改，如需修改密码请联系管理员。</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'UserCenter',
  data() {
    return {
      user: null,
      cartItems: [],
      activeMenu: 'info',
      saving: false,
      editForm: {
        name: '',
        phone: '',
        email: ''
      },
      menuItems: [
        { id: 'info', text: '基本信息', icon: '📋' },
        { id: 'cart', text: '购物车', icon: '🛒' },
        { id: 'edit', text: '修改信息', icon: '✏️' }
      ]
    }
  },
  computed: {
    userAvatar() {
      if (this.user && this.user.name) {
        return this.user.name.charAt(0)
      }
      return '👤'
    },
    totalPrice() {
      return this.cartItems.reduce((total, item) => {
        const price = item.book?.price || 0
        return total + price
      }, 0)
    }
  },
  created() {
    this.loadUser()
    if (this.user) {
      this.fetchCart()
      this.initEditForm()
    }
  },
  methods: {
    loadUser() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          this.user = JSON.parse(userStr)
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
    },
    initEditForm() {
      if (this.user) {
        this.editForm = {
          name: this.user.name || '',
          phone: this.user.phone || '',
          email: this.user.email || ''
        }
      }
    },
    async fetchCart() {
      if (!this.user) return

      try {
        const response = await axios.get('http://localhost:8090/cart/list', {
          params: { userId: this.user.id }
        })
        if (response.data.code === 200) {
          this.cartItems = response.data.data
        }
      } catch (error) {
        console.error('获取购物车失败:', error)
      }
    },
    async saveProfile() {
      if (!this.editForm.name.trim()) {
        alert('请输入姓名')
        return
      }

      this.saving = true
      try {
        // 这里需要后端提供更新用户信息的接口
        // 暂时只更新本地数据
        const updatedUser = {
          ...this.user,
          name: this.editForm.name,
          phone: this.editForm.phone,
          email: this.editForm.email
        }

        localStorage.setItem('user', JSON.stringify(updatedUser))
        this.user = updatedUser

        alert('信息更新成功！')
        this.activeMenu = 'info'
      } catch (error) {
        console.error('更新信息失败:', error)
        alert('更新失败，请重试')
      } finally {
        this.saving = false
      }
    },
    resetEdit() {
      this.initEditForm()
      this.activeMenu = 'info'
    },
    goHome() {
      this.$router.push('/')
    },
    goToCart() {
      this.$router.push('/cart')
    },
    goToLogin() {
      this.$router.push('/login')
    },
    goToBookList() {
      this.$router.push('/booklist')
    },
    logout() {
      localStorage.removeItem('user')
      this.user = null
      this.cartItems = []
      alert('已退出登录')
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.user-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.home-btn, .cart-btn, .logout-btn, .login-btn, .browse-btn, .view-cart-btn,
.cancel-btn, .save-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.home-btn {
  background: #1890ff;
  color: white;
}

.cart-btn {
  background: #faad14;
  color: white;
}

.logout-btn {
  background: #f5222d;
  color: white;
}

.login-btn, .browse-btn, .view-cart-btn {
  background: #1890ff;
  color: white;
  padding: 10px 20px;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.save-btn {
  background: #52c41a;
  color: white;
}

.save-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.not-logged-in {
  padding: 60px 40px;
  background: white;
  border-radius: 8px;
  text-align: center;
}

.login-prompt h3 {
  font-size: 24px;
  margin-bottom: 10px;
  color: #333;
}

.login-prompt p {
  color: #666;
  margin-bottom: 25px;
}

.user-content {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 30px;
}

.user-sidebar {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-avatar {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
}

.user-avatar h3 {
  font-size: 18px;
  margin-bottom: 5px;
  color: #333;
}

.user-no {
  font-size: 12px;
  color: #999;
}

.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 15px;
  background: none;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-align: left;
  color: #666;
  transition: all 0.3s;
}

.menu-item:hover {
  background: #f5f5f5;
}

.menu-item.active {
  background: #f0f7ff;
  color: #1890ff;
  font-weight: 500;
}

.menu-icon {
  font-size: 16px;
}

.user-main {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-main h2 {
  font-size: 22px;
  margin-bottom: 25px;
  color: #333;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.info-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-item {
  margin-bottom: 20px;
}

.info-item label {
  display: block;
  margin-bottom: 8px;
  color: #999;
  font-size: 14px;
}

.info-value {
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
  font-size: 16px;
  color: #333;
}

.empty-cart {
  text-align: center;
  padding: 60px 40px;
}

.cart-overview {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.cart-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.edit-form {
  max-width: 500px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #1890ff;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.cancel-btn, .save-btn {
  flex: 1;
  padding: 12px;
}

.edit-note {
  margin-top: 25px;
  padding: 15px;
  background: #f0f7ff;
  border-radius: 4px;
  font-size: 13px;
  color: #1890ff;
  border-left: 4px solid #1890ff;
}
</style>