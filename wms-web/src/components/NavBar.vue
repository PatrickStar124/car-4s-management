<template>
  <nav class="navbar">
    <!-- 左侧导航 -->
    <div class="nav-left">
      <!-- Logo或网站名称 -->
      <router-link to="/" class="logo">
        <i class="el-icon-s-promotion"></i>
        <span class="logo-text">4S店服务平台</span>
      </router-link>

      <!-- 公共导航项 -->
      <router-link to="/home" class="nav-link">
        <i class="el-icon-house"></i>
        <span>首页</span>
      </router-link>

      <!-- 根据角色显示不同的导航项 -->
      <template v-if="user">
        <!-- 车主专属菜单 -->
        <template v-if="user.role === 'owner'">
          <router-link to="/user-center" class="nav-link">
            <i class="el-icon-user"></i>
            <span>车主中心</span>
          </router-link>
          <!-- 车主可以查看自己的车辆 -->
          <router-link to="/user-center?tab=vehicles" class="nav-link">
            <i class="el-icon-truck"></i>
            <span>我的车辆</span>
          </router-link>
        </template>

        <!-- 员工专属菜单 -->
        <template v-else>
          <router-link to="/staff-center" class="nav-link">
            <i class="el-icon-office-building"></i>
            <span>员工中心</span>
          </router-link>
          <router-link to="/vehicle" class="nav-link" v-if="hasPermission('vehicle')">
            <i class="el-icon-truck"></i>
            <span>车辆管理</span>
          </router-link>
          <!-- 其他员工功能可以在这里添加 -->
          <router-link to="/placeholder/workorder" class="nav-link">
            <i class="el-icon-document"></i>
            <span>工单管理</span>
          </router-link>
          <router-link to="/placeholder/stock" class="nav-link">
            <i class="el-icon-box"></i>
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
                v-if="user.avatar"
                class="user-avatar"
            >
              {{ user.name?.charAt(0) }}
            </el-avatar>
            <span class="welcome-text">
              {{ user.role === 'owner' ? '车主' : '员工' }}：{{ user.name }}
            </span>
            <i class="el-icon-arrow-down el-icon--right"></i>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <i class="el-icon-user"></i>个人资料
              </el-dropdown-item>
              <el-dropdown-item command="changePassword">
                <i class="el-icon-lock"></i>修改密码
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <i class="el-icon-switch-button"></i>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>

      <template v-else>
        <!-- 未登录状态 -->
        <router-link to="/login" class="nav-link">
          <i class="el-icon-user"></i>
          <span>登录</span>
        </router-link>
        <router-link to="/register" class="nav-link">
          <i class="el-icon-edit"></i>
          <span>注册</span>
        </router-link>
      </template>
    </div>
  </nav>
</template>

<script>
import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'

export default {
  name: 'NavBar',

  setup() {
    const router = useRouter()
    const userStore = useUserStore()

    const user = computed(() => userStore.user)

    // 检查权限（这里可以根据具体角色细化）
    const hasPermission = (permission) => {
      if (!user.value) return false

      // 员工都有车辆管理权限
      if (permission === 'vehicle') {
        return user.value.role !== 'owner'
      }

      return true
    }

    // 处理下拉菜单命令
    const handleCommand = async (command) => {
      switch (command) {
        case 'profile':
          // 跳转到个人资料页面（暂时用占位页）
          if (user.value.role === 'owner') {
            router.push('/user-center?tab=profile')
          } else {
            router.push('/staff-center?tab=profile')
          }
          break

        case 'changePassword':
          router.push('/placeholder/password')
          break

        case 'logout':
          await handleLogout()
          break
      }
    }

    // 处理退出登录
    const handleLogout = async () => {
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        })

        // 清除用户信息
        userStore.clearUser()
        localStorage.removeItem('user')
        localStorage.removeItem('token')

        // 提示并跳转到首页
        router.push('/home')
        setTimeout(() => {
          // 使用Element Plus的消息提示
          const { ElMessage } = require('element-plus')
          ElMessage.success('已成功退出登录')
        }, 100)
      } catch (error) {
        // 用户取消退出
        console.log('取消退出登录')
      }
    }

    return {
      user,
      hasPermission,
      handleCommand
    }
  }
}
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
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 24px;
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

.logo i {
  font-size: 24px;
}

.logo-text {
  background: linear-gradient(to right, #fff 0%, #f0f0f0 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
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

.nav-link.router-link-exact-active {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  background-color: #409EFF;
  color: white;
}

.welcome-text {
  font-size: 14px;
  font-weight: 500;
  color: white;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .navbar {
    padding: 0 12px;
  }

  .nav-left {
    gap: 12px;
  }

  .logo-text {
    display: none;
  }

  .nav-link span {
    display: none;
  }

  .nav-link i {
    margin-right: 0;
  }

  .welcome-text {
    display: none;
  }
}
</style>