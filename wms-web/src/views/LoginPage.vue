<template>
  <div class="login-container">
    <div class="login-box">
      <!-- Logo和标题 -->
      <div class="login-header">
        <h1>汽车4S店数字化服务平台</h1>
        <p class="subtitle">专业服务，全程保障</p>
      </div>

      <!-- 登录表单 -->
      <div class="login-form">
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">工号/用户编号</label>
            <input
                id="username"
                v-model="loginForm.username"
                type="text"
                placeholder="请输入工号或用户编号"
                required
                :disabled="loading"
                @keyup.enter="handleLogin"
            />
            <p class="hint">车主请输入注册时设置的用户编号</p>
          </div>

          <div class="form-group password-input">
            <label for="password">密码</label>
            <input
                id="password"
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                required
                :disabled="loading"
                @keyup.enter="handleLogin"
            />
            <span class="toggle-password" @click="showPassword = !showPassword">
              {{ showPassword ? '🙈' : '👁️' }}
            </span>
          </div>

          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="rememberMe" :disabled="loading">
              记住我
            </label>
            <a href="#" class="forgot-password" @click.prevent="showForgotPassword = true">
              忘记密码？
            </a>
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="!loading">登录系统</span>
            <span v-else class="loading">
              <span class="loading-spinner"></span>
              登录中...
            </span>
          </button>
        </form>

        <!-- 角色提示 -->
        <div class="role-hint">
          <h4>登录角色说明：</h4>
          <ul>
            <li><strong>车主</strong> - 使用注册时设置的用户编号登录</li>
            <li><strong>服务顾问</strong> - 使用公司分配的工号登录</li>
            <li><strong>维修技师</strong> - 使用公司分配的工号登录</li>
            <li><strong>仓库管理员</strong> - 使用公司分配的工号登录</li>
          </ul>
        </div>
      </div>

      <!-- 注册链接 -->
      <div class="register-link">
        <p>还不是车主？
          <router-link to="/register">立即注册车主账号</router-link>
        </p>
        <p class="staff-register-note">
          员工账号需由管理员创建，如有问题请联系管理员
        </p>
      </div>

      <!-- 快速导航 -->
      <div class="quick-nav">
        <router-link to="/" class="nav-link">
          <i class="icon-home"></i>
          返回首页
        </router-link>
        <a href="#" class="nav-link" @click.prevent="showHelp = true">
          <i class="icon-help"></i>
          登录帮助
        </a>
      </div>
    </div>

    <!-- 忘记密码弹窗 -->
    <div v-if="showForgotPassword" class="modal-overlay" @click="showForgotPassword = false">
      <div class="modal-content" @click.stop>
        <h3>忘记密码</h3>
        <p>请联系您的管理员或拨打客服热线：400-1234-5678</p>
        <button @click="showForgotPassword = false" class="modal-close">关闭</button>
      </div>
    </div>

    <!-- 帮助弹窗 -->
    <div v-if="showHelp" class="modal-overlay" @click="showHelp = false">
      <div class="modal-content" @click.stop>
        <h3>登录帮助</h3>
        <div class="help-content">
          <h4>常见问题：</h4>
          <ol>
            <li><strong>忘记用户编号/工号？</strong><br>车主请联系客服，员工请联系管理员</li>
            <li><strong>忘记密码？</strong><br>请联系管理员重置密码</li>
            <li><strong>无法登录？</strong><br>请检查网络连接，或联系技术支持</li>
          </ol>
          <div class="contact-info">
            <p><strong>客服电话：</strong>400-1234-5678</p>
            <p><strong>服务时间：</strong>8:00-20:00</p>
          </div>
        </div>
        <button @click="showHelp = false" class="modal-close">我知道了</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

// 响应式变量
const showPassword = ref(false)
const showForgotPassword = ref(false)
const showHelp = ref(false)
const rememberMe = ref(false)
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const router = useRouter()

// 登录处理函数
const handleLogin = async () => {
  // 防止重复提交
  if (loading.value) return

  // 表单验证
  if (!loginForm.value.username.trim()) {
    ElMessage.warning('请输入工号/用户编号')
    return
  }

  if (!loginForm.value.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }

  // 设置加载状态
  loading.value = true

  try {
    // 保存记住的用户名
    if (rememberMe.value) {
      localStorage.setItem('rememberedUsername', loginForm.value.username)
    } else {
      localStorage.removeItem('rememberedUsername')
    }

    console.log('正在登录...')

    // 调用登录API
    const response = await login({
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    console.log('登录响应:', response)

    // 处理响应
    if (response.code === 200) {
      const userData = response.data

      if (!userData) {
        throw new Error('登录成功但未获取到用户信息')
      }

      // 存储用户信息到localStorage
      localStorage.setItem('user', JSON.stringify(userData))

      // 如果有token就存储，否则生成一个模拟token
      if (userData.token) {
        localStorage.setItem('token', userData.token)
      } else {
        localStorage.setItem('token', `token-${userData.id || Date.now()}`)
      }

      // 显示成功消息
      const userName = userData.name || userData.no || '用户'
      ElMessage.success({
        message: `欢迎回来，${userName}！`,
        duration: 2000
      })

      // 根据角色跳转
      setTimeout(() => {
        if (userData.role === 'owner') {
          router.push('/user-center')
        } else {
          router.push('/staff-center')
        }
      }, 1000)

    } else {
      // 登录失败
      ElMessage.error(response.msg || '登录失败，请检查用户名和密码')
    }

  } catch (error) {
    console.error('登录失败:', error)

    // 显示错误信息
    let errorMsg = '登录失败，请检查网络连接'
    if (error.message.includes('Network Error')) {
      errorMsg = '无法连接到服务器，请检查网络或联系管理员'
    } else if (error.message) {
      errorMsg = error.message
    }

    ElMessage.error(errorMsg)

  } finally {
    // 重置加载状态
    loading.value = false
  }
}

// 页面加载时恢复记住的用户名
onMounted(() => {
  const savedUsername = localStorage.getItem('rememberedUsername')
  if (savedUsername) {
    loginForm.value.username = savedUsername
    rememberMe.value = true
  }
})
</script>

<style scoped>
/* 样式部分保持不变 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 15px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 480px;
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
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

.login-form {
  margin-bottom: 30px;
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
  top: 70%;
  transform: translateY(-50%);
  cursor: pointer;
  user-select: none;
  font-size: 18px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #718096;
  cursor: pointer;
}

.forgot-password {
  color: #4299e1;
  text-decoration: none;
  font-size: 14px;
}

.forgot-password:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.submit-btn:hover:not(:disabled) {
  background: #3182ce;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(49, 130, 206, 0.3);
}

.submit-btn:disabled {
  background: #a0aec0;
  cursor: not-allowed;
  opacity: 0.7;
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

.role-hint {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-top: 25px;
  border-left: 4px solid #4299e1;
}

.role-hint h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 14px;
}

.role-hint ul {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #4a5568;
}

.role-hint li {
  margin-bottom: 6px;
}

.role-hint strong {
  color: #2c3e50;
}

.register-link {
  text-align: center;
  margin: 25px 0;
  padding-top: 25px;
  border-top: 1px solid #e2e8f0;
}

.register-link p {
  color: #718096;
  font-size: 14px;
  margin-bottom: 8px;
}

.register-link a {
  color: #4299e1;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
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
  max-width: 400px;
  width: 90%;
  text-align: center;
}

.modal-content h3 {
  color: #2c3e50;
  margin-bottom: 15px;
}

.modal-content p {
  color: #718096;
  line-height: 1.6;
  margin-bottom: 20px;
}

.help-content {
  text-align: left;
}

.help-content h4 {
  color: #2c3e50;
  margin: 15px 0 10px 0;
}

.help-content ol {
  padding-left: 20px;
  margin-bottom: 15px;
}

.help-content li {
  margin-bottom: 10px;
  line-height: 1.5;
}

.contact-info {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  margin-top: 15px;
}

.contact-info p {
  margin: 5px 0;
  font-size: 13px;
}

.modal-close {
  padding: 8px 20px;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.modal-close:hover {
  background: #3182ce;
}

/* 图标样式 */
.icon-home::before { content: '🏠'; }
.icon-help::before { content: '❓'; }

/* 响应式设计 */
@media (max-width: 480px) {
  .login-box {
    padding: 25px;
    margin: 10px;
  }

  h1 {
    font-size: 20px;
  }

  .quick-nav {
    flex-direction: column;
    gap: 10px;
    align-items: center;
  }
}
</style>