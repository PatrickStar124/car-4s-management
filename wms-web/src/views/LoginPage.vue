<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h2>用户登录</h2>
        <p>欢迎回到图书购物车系统</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="no">账号</label>
          <input
              id="no"
              v-model="form.no"
              type="text"
              required
              placeholder="请输入账号"
              autocomplete="username"
          >
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input
              id="password"
              v-model="form.password"
              type="password"
              required
              placeholder="请输入密码"
              autocomplete="current-password"
          >
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" v-model="rememberMe"> 记住我
          </label>
          <a href="#" class="forgot-password" @click.prevent="showForgotPassword">忘记密码？</a>
        </div>

        <button type="submit" :disabled="loading" class="login-button">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>

        <div class="demo-account" v-if="showDemo">
          <p>测试账号：</p>
          <p>账号：admin 密码：123456</p>
          <p>账号：test 密码：123456</p>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'LoginPage',
  data() {
    return {
      form: {
        no: '',
        password: ''
      },
      rememberMe: false,
      loading: false,
      showDemo: true
    }
  },
  created() {
    // 检查是否有保存的账号
    const savedNo = localStorage.getItem('saved_username')
    if (savedNo) {
      this.form.no = savedNo
      this.rememberMe = true
    }
  },
  methods: {
    async handleLogin() {
      if (!this.form.no || !this.form.password) {
        alert('请填写完整信息')
        return
      }

      this.loading = true
      try {
        const response = await axios.post('http://localhost:8090/user/login', this.form)

        if (response.data.code === 200) {
          const user = response.data.data

          // 保存用户信息
          localStorage.setItem('user', JSON.stringify(user))

          // 如果勾选了记住我，保存用户名
          if (this.rememberMe) {
            localStorage.setItem('saved_username', this.form.no)
          } else {
            localStorage.removeItem('saved_username')
          }

          alert('登录成功！')
          this.$router.push('/')
        } else {
          alert(response.data.msg || '登录失败，请检查账号密码')
        }
      } catch (error) {
        console.error('登录失败:', error)
        if (error.response) {
          alert(`登录失败: ${error.response.data.msg || '服务器错误'}`)
        } else if (error.request) {
          alert('网络错误，请检查网络连接')
        } else {
          alert('登录失败: ' + error.message)
        }
      } finally {
        this.loading = false
      }
    },
    showForgotPassword() {
      alert('请联系管理员重置密码')
    },
    useDemoAccount(account) {
      if (account === 'admin') {
        this.form.no = 'admin'
        this.form.password = '123456'
      } else if (account === 'test') {
        this.form.no = 'test'
        this.form.password = '123456'
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 28px;
  margin-bottom: 8px;
  color: #333;
}

.login-header p {
  color: #666;
  font-size: 14px;
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

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  cursor: pointer;
}

.forgot-password {
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
}

.forgot-password:hover {
  text-decoration: underline;
}

.login-button {
  width: 100%;
  padding: 14px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
  margin-bottom: 20px;
}

.login-button:hover {
  background: #40a9ff;
}

.login-button:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  color: #666;
  margin-bottom: 20px;
}

.register-link a {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}

.demo-account {
  margin-top: 25px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
  border-left: 4px solid #1890ff;
}

.demo-account p {
  margin: 5px 0;
}

.demo-account p:first-child {
  font-weight: bold;
  color: #333;
}
</style>