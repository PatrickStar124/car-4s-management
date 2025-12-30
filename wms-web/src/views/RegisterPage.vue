<template>
  <div class="register-page">
    <div class="register-container">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <!-- 账号：绑定form.no（对应后端no字段） -->
        <div class="form-item">
          <label>账号 *</label>
          <input
              v-model="form.no"
              type="text"
              required
              placeholder="请输入登录账号"
          >
        </div>

        <!-- 密码 -->
        <div class="form-item">
          <label>密码 *</label>
          <input
              v-model="form.password"
              type="password"
              required
              placeholder="请输入密码"
          >
        </div>

        <!-- 确认密码 -->
        <div class="form-item">
          <label>确认密码 *</label>
          <input
              v-model="confirmPassword"
              type="password"
              required
              placeholder="请再次输入密码"
          >
          <div v-if="confirmPassword" class="tip">
            {{ form.password === confirmPassword ? '✓ 密码匹配' : '✗ 密码不匹配' }}
          </div>
        </div>

        <!-- 用户名 -->
        <div class="form-item">
          <label>用户名 *</label>
          <input
              v-model="form.name"
              type="text"
              required
              placeholder="请输入用户名"
          >
        </div>

        <!-- 协议勾选 -->
        <div class="agreement">
          <input type="checkbox" v-model="agreed" required>
          我已阅读并同意《用户协议》
        </div>

        <!-- 按钮区 -->
        <div class="btn-group">
          <button type="button" @click="goToLogin" class="back-btn">返回登录</button>
          <button
              type="submit"
              :disabled="loading || !agreed || form.password !== confirmPassword"
              class="register-btn"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
// 直接导入axios（Vue3推荐局部导入，避免全局污染）
import axios from 'axios'

export default {
  name: 'RegisterPage',
  data() {
    return {
      form: {
        no: '',     // 账号（对应后端no字段）
        password: '',
        name: ''
      },
      confirmPassword: '',
      agreed: false,
      loading: false
    }
  },
  methods: {
    async handleRegister() {
      // 非空验证
      if (!this.form.no || !this.form.password || !this.form.name) {
        this.$message.error('请填写所有必填项！');
        return;
      }

      this.loading = true;
      try {
        const res = await axios.post(
            'http://localhost:8090/user/register',
            this.form,
            {
              headers: { 'Content-Type': 'application/json' },
              withCredentials: true
            }
        );

        if (res.data.success || res.data.code === 200) {
          this.$message.success('注册成功！即将跳转到登录页');
          setTimeout(() => this.$router.push('/login'), 1500);
        } else {
          this.$message.error(res.data.msg || '注册失败');
        }
      } catch (err) {
        console.error('注册请求异常：', err);
        this.$message.error(err.response?.data?.msg || '服务器连接失败');
      } finally {
        this.loading = false;
      }
    },
    goToLogin() {
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
.register-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
  padding: 20px;
  box-sizing: border-box;
}
.register-container {
  width: 100%;
  max-width: 400px;
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  box-sizing: border-box;
}
.register-container h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
  font-size: 20px;
}
.form-item {
  margin-bottom: 20px;
}
.form-item label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}
.form-item input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}
.form-item input:focus {
  outline: none;
  border-color: #1890ff;
}
.tip {
  margin-top: 5px;
  font-size: 12px;
  color: #ff4d4f;
}
.tip:has(✓) {
  color: #52c41a;
}
.agreement {
  margin: 15px 0 20px;
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 5px;
}
.btn-group {
  display: flex;
  gap: 10px;
}
.back-btn {
  flex: 1;
  padding: 10px;
  background: #f5f5f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
}
.register-btn {
  flex: 2;
  padding: 10px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.register-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}
</style>