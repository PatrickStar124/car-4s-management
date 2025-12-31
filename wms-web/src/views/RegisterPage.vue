<template>
  <div class="register-container">
    <div class="register-box">
      <!-- Logo和标题 -->
      <div class="register-header">
        <img src="@/assets/logo.png" alt="4S店平台" class="logo" v-if="logoExists">
        <h1>车主账号注册</h1>
        <p class="subtitle">注册成为车主，享受专业汽车服务</p>
      </div>

      <!-- 注册表单 -->
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">用户编号 *</label>
          <input
              id="username"
              v-model="registerForm.username"
              type="text"
              placeholder="请设置您的用户编号（用于登录）"
              required
              :disabled="loading"
          />
          <p class="hint">用户编号一旦设置不可修改，请妥善保管</p>
        </div>

        <div class="form-group">
          <label for="password">密码 *</label>
          <div class="password-input">
            <input
                id="password"
                v-model="registerForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请设置登录密码"
                required
                minlength="6"
                :disabled="loading"
            />
            <span class="toggle-password" @click="showPassword = !showPassword">
              {{ showPassword ? '🙈' : '👁️' }}
            </span>
          </div>
          <p class="hint">密码长度至少6位</p>
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认密码 *</label>
          <input
              id="confirmPassword"
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              required
              :disabled="loading"
          />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="name">姓名 *</label>
            <input
                id="name"
                v-model="registerForm.name"
                type="text"
                placeholder="请输入真实姓名"
                required
                :disabled="loading"
            />
          </div>

          <div class="form-group">
            <label for="phone">手机号 *</label>
            <input
                id="phone"
                v-model="registerForm.phone"
                type="tel"
                placeholder="请输入手机号"
                required
                :disabled="loading"
            />
          </div>
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <input
              id="email"
              v-model="registerForm.email"
              type="email"
              placeholder="请输入邮箱地址（可选）"
              :disabled="loading"
          />
        </div>

        <div class="form-agreement">
          <label class="agreement-label">
            <input type="checkbox" v-model="agreed" required :disabled="loading">
            我已阅读并同意
            <a href="#" @click.prevent="showAgreement = true">《用户服务协议》</a>
            和
            <a href="#" @click.prevent="showPrivacy = true">《隐私政策》</a>
          </label>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading || !agreed">
          <span v-if="!loading">注册车主账号</span>
          <span v-else class="loading">
            <span class="loading-spinner"></span>
            注册中...
          </span>
        </button>
      </form>

      <!-- 已有账号 -->
      <div class="login-link">
        <p>已有车主账号？
          <router-link to="/login">立即登录</router-link>
        </p>
        <p class="staff-register-note">
          员工账号需由管理员创建，如需开通请联系管理员
        </p>
      </div>

      <!-- 快速导航 -->
      <div class="quick-nav">
        <router-link to="/" class="nav-link">
          <i class="icon-home"></i>
          返回首页
        </router-link>
        <router-link to="/login" class="nav-link">
          <i class="icon-login"></i>
          用户登录
        </router-link>
      </div>
    </div>

    <!-- 用户协议弹窗 -->
    <div v-if="showAgreement" class="modal-overlay" @click="showAgreement = false">
      <div class="modal-content agreement-content" @click.stop>
        <h3>用户服务协议</h3>
        <div class="agreement-text">
          <p>本协议是您与汽车4S店服务平台之间关于使用服务的协议...</p>
          <p>1. 您承诺所提供的注册信息真实、准确、完整。</p>
          <p>2. 您需妥善保管账号密码，账号安全由您自行负责。</p>
        </div>
        <button @click="showAgreement = false" class="modal-close">我同意并关闭</button>
      </div>
    </div>

    <!-- 隐私政策弹窗 -->
    <div v-if="showPrivacy" class="modal-overlay" @click="showPrivacy = false">
      <div class="modal-content" @click.stop>
        <h3>隐私政策</h3>
        <div class="privacy-text">
          <p>我们非常重视您的隐私保护...</p>
          <p>1. 我们仅收集必要的用户信息用于服务提供。</p>
          <p>2. 我们不会向第三方泄露您的个人信息。</p>
        </div>
        <button @click="showPrivacy = false" class="modal-close">我同意并关闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'RegisterPage',
  data() {
    return {
      logoExists: true,
      showPassword: false,
      showAgreement: false,
      showPrivacy: false,
      loading: false,
      agreed: false,

      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        name: '',
        phone: '',
        email: ''
      }
    }
  },

  mounted() {
    this.checkLogo()
  },

  methods: {
    checkLogo() {
      // 可根据实际logo文件是否存在动态调整
      this.logoExists = true
    },

    async handleRegister() {
      // 表单验证
      if (!this.validateForm()) {
        return
      }

      this.loading = true
      try {
        // 核心：转换前端数据结构为后端需要的格式
        const userToRegister = {
          no: this.registerForm.username,    // 前端username -> 后端no
          password: this.registerForm.password,
          name: this.registerForm.name,
          phone: this.registerForm.phone,
          email: this.registerForm.email
          // role无需传，后端默认设置为owner
        }

        // 发送请求到后端正确接口 /api/user/register
        const response = await axios.post('/api/user/register', userToRegister)

        // 适配后端Result返回格式 {code, msg, data}
        if (response.data && response.data.code === 200) {
          this.$message.success('注册成功！即将为您跳转到登录页面')
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        } else {
          this.$message.error(response.data.msg || '注册失败，请联系管理员')
        }
      } catch (error) {
        console.error('注册请求失败:', error)
        let errorMsg = '注册失败，网络错误或服务器正忙'
        // 捕获后端返回的业务错误信息
        if (error.response && error.response.data && error.response.data.msg) {
          errorMsg = error.response.data.msg
        }
        this.$message.error(errorMsg)
      } finally {
        this.loading = false
      }
    },

    validateForm() {
      // 密码一致性验证
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        this.$message.error('两次输入的密码不一致')
        return false
      }

      // 密码长度验证
      if (this.registerForm.password.length < 6) {
        this.$message.error('密码长度至少6位')
        return false
      }

      // 用户编号非空验证
      if (!this.registerForm.username.trim()) {
        this.$message.error('用户编号不能为空')
        return false
      }

      // 姓名非空验证
      if (!this.registerForm.name.trim()) {
        this.$message.error('姓名不能为空')
        return false
      }

      // 手机号格式验证
      const phoneRegex = /^1[3-9]\d{9}$/
      if (!phoneRegex.test(this.registerForm.phone)) {
        this.$message.error('请输入正确的手机号')
        return false
      }

      // 邮箱格式验证（可选）
      if (this.registerForm.email && !this.isValidEmail(this.registerForm.email)) {
        this.$message.error('请输入正确的邮箱格式')
        return false
      }

      return true
    },

    isValidEmail(email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(email)
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.register-box {
  background: white;
  border-radius: 15px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 480px;
  padding: 40px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 80px;
  height: 80px;
  margin-bottom: 15px;
}

h1 {
  color: #2c3e50;
  font-size: 24px;
  margin-bottom: 8px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #4a5568;
  font-weight: 500;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  border-color: #4299e1;
  outline: none;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

.form-group input:disabled {
  background-color: #f7fafc;
  cursor: not-allowed;
}

.hint {
  font-size: 12px;
  color: #a0aec0;
  margin-top: 5px;
}

.password-input {
  position: relative;
}

.toggle-password {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  user-select: none;
  font-size: 18px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}

.form-agreement {
  margin: 25px 0;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.agreement-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #4a5568;
  cursor: pointer;
}

.agreement-label a {
  color: #4299e1;
  text-decoration: none;
}

.agreement-label a:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background: #38a169;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(56, 161, 105, 0.3);
}

.submit-btn:disabled {
  background: #a0aec0;
  cursor: not-allowed;
}

.loading {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #fff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.login-link {
  text-align: center;
  margin: 25px 0;
  padding-top: 25px;
  border-top: 1px solid #e2e8f0;
}

.login-link p {
  color: #718096;
  font-size: 14px;
  margin-bottom: 8px;
}

.login-link a {
  color: #4299e1;
  text-decoration: none;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}

.staff-register-note {
  font-size: 12px;
  color: #a0aec0;
  margin-top: 5px;
}

.quick-nav {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #718096;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #4299e1;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.agreement-content, .privacy-text {
  text-align: left;
}

.modal-content h3 {
  color: #2c3e50;
  margin-bottom: 15px;
  text-align: center;
}

.agreement-text, .privacy-text {
  color: #718096;
  line-height: 1.6;
  margin-bottom: 20px;
  font-size: 14px;
}

.modal-close {
  padding: 8px 20px;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.modal-close:hover {
  background: #3182ce;
}

/* 图标样式 */
.icon-home::before { content: '🏠'; }
.icon-login::before { content: '🔑'; }

@media (max-width: 480px) {
  .register-box {
    padding: 25px;
    margin: 10px;
  }

  h1 {
    font-size: 20px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .quick-nav {
    flex-direction: column;
    gap: 10px;
    align-items: center;
  }
}
</style>