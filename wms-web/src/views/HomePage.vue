<template>
  <div class="home-page">
    <header class="header">
      <h1>📚 图书商城</h1>
      <div class="header-actions">
        <button @click="goToBookList" class="booklist-btn">全部图书</button>
        <button @click="goToCart" class="cart-btn">
          🛒 购物车
          <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
        </button>
        <div class="user-info" v-if="user">
          <span>欢迎，{{ user.name }}</span>
          <button @click="goToUserCenter" class="user-btn">个人中心</button>
          <button @click="logout" class="logout-btn">退出</button>
        </div>
        <div class="user-info" v-else>
          <button @click="goToLogin" class="login-btn">登录</button>
          <button @click="goToRegister" class="register-btn">注册</button>
        </div>
      </div>
    </header>

    <div class="welcome-section">
      <h2>欢迎来到图书购物车系统</h2>
      <p>这里汇集了各类精品图书，点击"全部图书"浏览更多，或将喜欢的图书加入购物车</p>
    </div>

    <div class="featured-books">
      <h3>🔥 热门推荐</h3>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="books.length === 0" class="empty">
        <div class="empty-icon">📚</div>
        <p>暂无推荐图书</p>
        <button @click="goToBookAdd" v-if="user" class="add-book-btn">添加第一本书</button>
      </div>
      <div v-else class="books-grid">
        <BookCard
            v-for="book in books.slice(0, 4)"
            :key="book.id"
            :book="formatBookData(book)"
            @cart-updated="handleCartUpdate"
        />
      </div>
    </div>

    <div class="quick-links">
      <div class="link-card" @click="goToBookList">
        <div class="link-icon">📖</div>
        <h4>浏览全部图书</h4>
        <p>查看所有可用图书</p>
      </div>
      <div class="link-card" @click="goToCart">
        <div class="link-icon">🛒</div>
        <h4>查看购物车</h4>
        <p>管理您的购物清单</p>
      </div>
      <div class="link-card" @click="user ? goToUserCenter() : goToLogin()">
        <div class="link-icon">👤</div>
        <h4>{{ user ? '个人中心' : '用户登录' }}</h4>
        <p>{{ user ? '查看个人信息' : '登录以使用完整功能' }}</p>
      </div>
    </div>

    <!-- 调试信息 -->
    <div v-if="showDebug" class="debug-info">
      <h4>调试信息</h4>
      <p>用户: {{ user ? user.name : '未登录' }}</p>
      <p>图书数量: {{ books.length }}</p>
      <p>购物车数量: {{ cartCount }}</p>
      <button @click="toggleDebug" class="debug-btn">隐藏调试</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import BookCard from '@/components/BookCard.vue'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import { storeToRefs } from 'pinia'

export default {
  name: 'HomePage',
  components: {
    BookCard
  },
  setup() {
    const cartStore = useCartStore()
    const userStore = useUserStore()
    const { user } = storeToRefs(userStore)
    const { cartTotalQuantity } = storeToRefs(cartStore)

    return {
      cartStore,
      userStore,
      user,
      cartTotalQuantity
    }
  },
  data() {
    return {
      books: [],
      loading: true,
      showDebug: false
    }
  },
  computed: {
    cartCount() {
      return this.cartTotalQuantity || 0
    }
  },
  created() {
    this.fetchBooks()
    // 如果用户已登录，初始化购物车
    if (this.user) {
      this.cartStore.initCartList(this.user.id)
    }
  },
  methods: {
    // 格式化图书数据，确保字段完整
    formatBookData(book) {
      if (!book) return {}

      return {
        id: book.id || 0,
        name: book.name || book.bookName || '未知图书',
        author: book.author || '未知作者',
        price: book.price || 0,
        stock: book.stock || 0,
        imageUrl: book.imageUrl || book.image || book.img || book.cover || '',
        description: book.description || '暂无描述'
      }
    },

    async fetchBooks() {
      this.loading = true
      console.log('首页开始获取图书列表...')

      try {
        const response = await axios.get('http://localhost:8090/book/list')
        console.log('首页获取图书接口返回:', response.data)

        if (response.data.code === 200) {
          // 处理并格式化图书数据
          this.books = response.data.data.map(book => this.formatBookData(book))
          console.log('首页处理后的图书列表:', this.books)
        } else {
          console.error('首页获取图书失败:', response.data.msg)
        }
      } catch (error) {
        console.error('首页获取图书失败:', error)
      } finally {
        this.loading = false
      }
    },

    handleCartUpdate() {
      console.log('购物车已更新')
    },

    goToBookList() {
      this.$router.push('/booklist')
    },

    goToCart() {
      if (!this.user) {
        alert('请先登录')
        this.$router.push('/login')
        return
      }
      this.$router.push('/cart')
    },

    goToLogin() {
      this.$router.push('/login')
    },

    goToRegister() {
      this.$router.push('/register')
    },

    goToUserCenter() {
      this.$router.push('/usercenter')
    },

    goToBookAdd() {
      this.$router.push('/bookadd')
    },

    logout() {
      if (confirm('确定要退出登录吗？')) {
        this.userStore.clearUser()
        this.cartStore.clearCart()
        localStorage.removeItem('user')
        alert('已退出登录')
        this.$router.push('/')
      }
    },

    toggleDebug() {
      this.showDebug = !this.showDebug
    }
  }
}
</script>

<style scoped>
.home-page {
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
  align-items: center;
  position: relative;
}

.user-info {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-left: 10px;
}

.booklist-btn, .cart-btn, .login-btn, .register-btn, .logout-btn, .user-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.booklist-btn {
  background: #1890ff;
  color: white;
}

.booklist-btn:hover {
  background: #40a9ff;
}

.cart-btn {
  background: #faad14;
  color: white;
  position: relative;
}

.cart-btn:hover {
  background: #ffc53d;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ff4d4f;
  color: white;
  font-size: 12px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.login-btn {
  background: #52c41a;
  color: white;
}

.login-btn:hover {
  background: #73d13d;
}

.register-btn {
  background: #722ed1;
  color: white;
}

.register-btn:hover {
  background: #9254de;
}

.logout-btn {
  background: #f5222d;
  color: white;
}

.logout-btn:hover {
  background: #ff4d4f;
}

.user-btn {
  background: #13c2c2;
  color: white;
}

.user-btn:hover {
  background: #36cfc9;
}

.welcome-section {
  text-align: center;
  padding: 40px;
  margin-bottom: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
}

.welcome-section h2 {
  font-size: 28px;
  margin-bottom: 10px;
}

.welcome-section p {
  font-size: 16px;
  opacity: 0.9;
  max-width: 800px;
  margin: 0 auto;
}

.featured-books {
  margin-bottom: 40px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.featured-books h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.loading, .empty {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: #999;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 15px;
  opacity: 0.5;
}

.add-book-btn {
  padding: 8px 16px;
  background: #52c41a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 15px;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.link-card {
  padding: 25px;
  background: white;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.link-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
  border-color: #1890ff;
}

.link-icon {
  font-size: 40px;
  margin-bottom: 15px;
}

.link-card h4 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
}

.link-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

/* 调试信息 */
.debug-info {
  margin-top: 30px;
  padding: 15px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
}

.debug-info h4 {
  margin: 0 0 10px 0;
  color: #495057;
}

.debug-btn {
  padding: 6px 12px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-top: 10px;
}
</style>