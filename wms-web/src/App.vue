<template>
  <div id="app">
    <!-- 导航栏 -->
    <NavBar />

    <!-- 主要内容区域 -->
    <main class="app-main">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="app-footer">
      <div class="footer-content">
        <p>© 2026 汽车4S店数字化服务平台. 版权所有.</p>
        <p>客服热线：400-1234-5678 | 服务时间：周一至周日 8:00-20:00</p>
        <div class="footer-links">
          <a href="#" @click.prevent="goToHome">首页</a>
          <a href="#" @click.prevent="goToHelp">帮助中心</a>
          <a href="#" @click.prevent="goToAbout">关于我们</a>
          <a href="#" @click.prevent="goToContact">联系我们</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()

const goToHome = () => {
  router.push('/')
}

const goToHelp = () => {
  // 暂时跳转到占位页
  router.push('/placeholder/help')
}

const goToAbout = () => {
  router.push('/placeholder/about')
}

const goToContact = () => {
  router.push('/placeholder/contact')
}
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

body {
  margin: 0;
  padding: 0;
  background-color: #f5f7fa;
  font-family: 'Helvetica Neue', Arial, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.app-main {
  flex: 1;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.app-footer {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px 0;
  text-align: center;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-links {
  margin-top: 15px;
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: white;
  text-decoration: underline;
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
</style>