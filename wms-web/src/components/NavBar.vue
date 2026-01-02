<!-- src/components/Navbar.vue -->
<template>
  <nav class="navbar">
    <!-- 左侧导航 -->
    <div class="nav-left">
      <!-- Logo或网站名称 -->
      <router-link to="/" class="logo">
        <el-icon><Promotion /></el-icon>
        <span class="logo-text">4S店服务平台</span>
      </router-link>

      <!-- 公共导航项 -->
      <router-link to="/" class="nav-link">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </router-link>

      <!-- 根据角色显示不同的导航项 -->
      <template v-if="user">
        <!-- 车主专属菜单 -->
        <template v-if="user.role === 'owner'">
          <router-link to="/user-center" class="nav-link">
            <el-icon><User /></el-icon>
            <span>车主中心</span>
          </router-link>
          <router-link to="/vehicle" class="nav-link">
            <el-icon><Van /></el-icon>
            <span>我的车辆</span>
          </router-link>
          <router-link to="/appointment/list" class="nav-link">
            <el-icon><Calendar /></el-icon>
            <span>我的预约</span>
          </router-link>
        </template>

        <!-- 员工专属菜单 -->
        <template v-else>
          <router-link to="/staff-center" class="nav-link">
            <el-icon><OfficeBuilding /></el-icon>
            <span>员工中心</span>
          </router-link>
          <router-link to="/vehicle" class="nav-link" v-if="hasPermission('vehicle')">
            <el-icon><Van /></el-icon>
            <span>车辆管理</span>
          </router-link>
          <!-- 动态路由示例 -->
          <router-link to="/mechanic/task-list" class="nav-link" v-if="user.role === 'mechanic'">
            <el-icon><Document /></el-icon>
            <span>我的任务</span>
          </router-link>
          <router-link to="/warehouse/stock-management" class="nav-link" v-if="user.role === 'warehouse'">
            <el-icon><Box /></el-icon>
            <span>配件库存</span>
          </router-link>
        </template>
      </template>
    </div>

    <!-- 右侧用户操作 -->
    <div class="nav-right">
      <template v-if="user">
        <!-- 用户信息 -->
        <el-dropdown @command="handleCommand" class="user-dropdown">
          <div class="user-info">
            <el-avatar
                :size="32"
                :src="user.avatar"
                class="user-avatar"
            >
              {{ user.name?.charAt(0) || user.no?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="welcome-text">
              {{ roleText }}：{{ user.name || user.no || '用户' }}
            </span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人资料
              </el-dropdown-item>
              <el-dropdown-item command="changePassword">
                <el-icon><Lock /></el-icon>修改密码
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>

      <template v-else>
        <!-- 未登录状态 -->
        <router-link to="/login" class="nav-link">
          <el-icon><User /></el-icon>
          <span>登录</span>
        </router-link>
        <router-link to="/register" class="nav-link">
          <el-icon><EditPen /></el-icon>
          <span>注册</span>
        </router-link>
      </template>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Promotion, House, User, Van, Calendar, OfficeBuilding,
  Document, Box, ArrowDown, Lock, SwitchButton, EditPen
} from '@element-plus/icons-vue'

const router = useRouter()

// 1. 使用 ref 替代 data
const user = ref(null)

// 2. 使用 computed 替代 computed
const roleText = computed(() => {
  if (!user.value) return ''
  const roleMap = {
    'owner': '车主',
    'service': '服务顾问',
    'mechanic': '维修技师',
    'warehouse': '仓库管理员',
    'admin': '系统管理员'
  }
  return roleMap[user.value.role] || user.value.role
})

// 3. 使用普通函数替代 methods
const hasPermission = (permission) => {
  if (!user.value) return false
  // 示例：只有非车主角色才能访问车辆管理
  if (permission === 'vehicle') {
    return user.value.role !== 'owner'
  }
  return true
}

const loadUserFromLocalStorage = () => {
  try {
    // ✅ 确保使用正确的键名 'user'
    const userStr = localStorage.getItem('user')
    if (userStr) {
      user.value = JSON.parse(userStr)
      console.log('✅ 导航栏已加载用户信息:', user.value)
    }
  } catch (error) {
    console.error('❌ 解析用户信息失败:', error)
    user.value = null
  }
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      if (user.value.role === 'owner') {
        router.push('/user-center')
      } else {
        router.push('/staff-center')
      }
      break
    case 'changePassword':
      ElMessage.info('修改密码功能开发中...')
      break
    case 'logout':
      await handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    // 清除用户信息
    user.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('rememberedUsername')

    ElMessage.success('已成功退出登录')
    router.push('/')

  } catch (error) {
    // 用户取消操作，ElMessageBox会抛出异常
    console.log('用户取消退出登录')
  }
}

// 4. 使用 onMounted 替代 mounted
onMounted(() => {
  loadUserFromLocalStorage()
})

// 5. 增加 watch 监听 localStorage 的变化（例如在其他标签页登录/退出）
watch(
    () => localStorage.getItem('user'),
    () => {
      loadUserFromLocalStorage()
    },
    { immediate: true, deep: true }
)
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 60px;
  background: linear-gradient(135deg, #1890ff 0%, #36cbcb 100%);
  color: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.nav-left, .nav-right {
  display: flex;
  align-items: center;
  gap: 16px; /* 优化间距 */
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: white;
  text-decoration: none;
}

.logo .el-icon {
  font-size: 24px;
}

.logo-text {
  /* 移除了复杂的渐变文字，保持简洁 */
  font-weight: bold;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-size: 14px;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

/* ✅ 优化激活链接样式 */
.nav-link.router-link-active,
.nav-link.router-link-exact-active {
  background: rgba(255, 255, 255, 0.25);
  color: white;
  font-weight: 500;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 20px; /* 改为胶囊形状 */
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
}

.welcome-text {
  font-size: 14px;
  font-weight: 500;
  color: white;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .navbar {
    padding: 0 12px;
  }

  .nav-left {
    gap: 8px;
  }

  .logo-text {
    display: none;
  }

  .nav-link span {
    display: none;
  }

  .nav-link {
    padding: 8px;
  }

  .welcome-text {
    display: none;
  }

  .user-info {
    padding: 4px;
  }
}
</style>