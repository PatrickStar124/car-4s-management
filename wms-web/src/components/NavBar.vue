<template>
  <nav class="navbar">
    <div class="nav-left">
      <router-link to="/" class="nav-link">首页</router-link>
      <router-link to="/booklist" class="nav-link">全部图书</router-link>
      <router-link to="/cart" class="nav-link">
        购物车
        <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
      </router-link>
    </div>
    <div class="nav-right">
      <template v-if="user">
        <span class="welcome-text">欢迎，{{ user.name }}</span>
        <router-link to="/usercenter" class="nav-link">个人中心</router-link>
        <button @click="handleLogout" class="logout-btn">退出</button>
      </template>
      <template v-else>
        <router-link to="/login" class="nav-link">登录</router-link>
        <router-link to="/register" class="nav-link">注册</router-link>
      </template>
    </div>
  </nav>
</template>

<script>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/partStock'
import { computed } from 'vue'

export default {
  name: 'NavBar',

  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const cartStore = useCartStore()

    const user = computed(() => userStore.user)
    const cartCount = computed(() => cartStore.cartTotalQuantity || 0)

    const handleLogout = () => {
      if (confirm('确定要退出登录吗？')) {
        userStore.clearUser()
        cartStore.clearCart()
        localStorage.removeItem('user')
        alert('已退出登录')
        router.push('/login')
      }
    }

    return {
      user,
      cartCount,
      handleLogout
    }
  }
}
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background: #1890ff;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-left {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s ease;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-link.router-link-exact-active {
  background: rgba(255, 255, 255, 0.2);
}

.welcome-text {
  margin-right: 10px;
  font-size: 14px;
}

.logout-btn {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
}

.cart-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: #ff4d4f;
  color: white;
  font-size: 12px;
  border-radius: 9px;
  margin-left: 4px;
}
</style>