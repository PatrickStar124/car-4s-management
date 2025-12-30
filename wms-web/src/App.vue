<template>
  <div id="app">
    <!-- 全局导航栏 -->
    <NavBar v-if="showNavBar" />

    <!-- 路由出口：根据路由动态显示 views/ 目录下的页面 -->
    <router-view />
  </div>
</template>

<script>
import NavBar from '@/components/NavBar.vue'

export default {
  name: 'App',
  components: {
    NavBar
  },
  computed: {
    // 在登录/注册页面不显示导航栏
    showNavBar() {
      return this.$route.path !== '/login' && this.$route.path !== '/register'
    }
  }
}
</script>

<style>
/* 全局基础样式 */
#app {
  height: 100%;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background: #f0f2f5;
  min-height: 100vh;
}

/* 确保页面全屏 */
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
}

/* 全局链接样式 */
a {
  color: #1890ff;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

/* 全局按钮基础样式 */
button {
  cursor: pointer;
  font-family: inherit;
  outline: none;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 全局卡片样式 */
.card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

/* 全局表单样式 */
input, textarea, select {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.3s ease;
}

input:focus, textarea:focus, select:focus {
  border-color: #1890ff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 全局表格样式 */
table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  background: #fafafa;
  font-weight: 600;
  color: #333;
}

tr:hover {
  background: #fafafa;
}
</style>